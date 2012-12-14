/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.apphosting.api.ApiProxy;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Timo Vesalainen
 */
public class BlobServlet extends HttpServlet implements BlogConstants
{

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String blobKeyString = request.getParameter(AddParameter);
        if (blobKeyString != null)
        {
            addBlobs(request, blobKeyString, request.getParameter("metadata"));
        }
        else
        {
            
        }
    }

    private void addBlobs(HttpServletRequest request, String blogKeyString, String metadataKeyString) throws ServletException
    {
        while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > 1000)
        {
            try
            {
                tryAddBlobs(request, blogKeyString, metadataKeyString);
                return;
            }
            catch (ConcurrentModificationException ex)
            {
                log("retry add blob "+blogKeyString);
            }
        }
        log("giving up "+blogKeyString);
    }
    private void tryAddBlobs(HttpServletRequest request, String blogKeyString, String metadataKeyString) throws ServletException
    {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        DB db = DB.DB;
        Transaction tr = db.beginTransaction();
        try
        {
            Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
            Key key = KeyFactory.stringToKey(blogKeyString);
            Entity blog = db.get(key);
            Text text = (Text) blog.getProperty(HtmlProperty);
            if (text == null)
            {
                throw new ServletException(HtmlProperty+" property not found in "+blog);
            }
            String html = text.getValue();
            Collection<String> cidSet = (Collection<String>) blog.getProperty(CidsProperty);
            if (cidSet == null)
            {
                cidSet = new HashSet<String>();
            }
            Collection<BlobKey> blobKeys = (Collection<BlobKey>) blog.getProperty(BlobsProperty);
            if (blobKeys == null)
            {
                blobKeys = new ArrayList<BlobKey>();
            }
            for (Map.Entry<String, List<BlobKey>> entry : blobs.entrySet())
            {
                String cid = entry.getKey();
                log(cid);
                if (cid.startsWith("<") && cid.endsWith(">"))
                {
                    cid = cid.substring(1, cid.length()-1);
                }
                String blobKey = entry.getValue().get(0).getKeyString();
                html = html.replace("cid:"+cid, "/blog?blob-key="+blobKey);
                cidSet.add(cid);
                blobKeys.addAll(entry.getValue());
            }
            blog.setUnindexedProperty(HtmlProperty, new Text(html));
            blog.setUnindexedProperty(CidsProperty, cidSet);
            blog.setUnindexedProperty(BlobsProperty, blobKeys);
            log(blog.toString());
            db.putAndCache(blog);
            Key metadataKey = KeyFactory.stringToKey(metadataKeyString);
            Entity metadata = db.get(metadataKey);
            blobKeys = (Collection<BlobKey>) metadata.getProperty(BlobsProperty);
            if (blobKeys == null)
            {
                blobKeys = new ArrayList<BlobKey>();
            }
            for (Map.Entry<String, List<BlobKey>> entry : blobs.entrySet())
            {
                blobKeys.addAll(entry.getValue());
            }
            metadata.setUnindexedProperty(BlobsProperty, blobKeys);
            tr.commit();
        }
        catch (EntityNotFoundException ex)
        {
            throw new ServletException(ex);
        }
        finally
        {
            if (tr.isActive())
            {
                tr.rollback();
            }
        }
    }

}
