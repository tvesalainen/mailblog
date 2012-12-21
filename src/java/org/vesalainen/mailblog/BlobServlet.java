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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;
import com.google.apphosting.api.ApiProxy;
import java.io.IOException;
import java.util.ConcurrentModificationException;
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
        String sha1 = request.getParameter(Sha1Parameter);
        if (sha1 != null)
        {
            DB db = DB.DB;
            Entity metadata = db.getMetadata(sha1);
            if (metadata != null)
            {
                BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
                String original = request.getParameter(OriginalParameter);
                if (original != null)
                {
                    BlobKey originalBlobKey = (BlobKey) metadata.getProperty(OriginalSizeProperty);
                    if (originalBlobKey != null)
                    {
                        blobstore.serve(originalBlobKey, response);
                        return;
                    }
                }
                BlobKey webBlobKey = (BlobKey) metadata.getProperty(WebSizeProperty);
                if (webBlobKey != null)
                {
                    blobstore.serve(webBlobKey, response);
                    return;
                }
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
        String namespace = request.getParameter(NamespaceParameter);
        NamespaceManager.set(namespace);
        log("namespace = "+namespace);
        String sizeString = request.getParameter(SizeParameter);
        if (sizeString != null)
        {
            addBlobs(request, sizeString);
        }
        else
        {
            
        }
    }

    private void addBlobs(HttpServletRequest request, String sizeString) throws ServletException
    {
        while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > 1000)
        {
            try
            {
                tryAddBlobs(request, sizeString);
                return;
            }
            catch (ConcurrentModificationException ex)
            {
                log("retry add blob "+sizeString);
            }
        }
        log("giving up "+sizeString);
    }
    private void tryAddBlobs(HttpServletRequest request, String sizeString) throws ServletException
    {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        DB db = DB.DB;
        Transaction tr = db.beginTransaction();
        try
        {
            Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
            for (Map.Entry<String, List<BlobKey>> entry : blobs.entrySet())
            {
                String sha1 = entry.getKey();
                log("sha1="+sha1);
                Entity metadata = db.getMetadata(sha1);
                BlobKey blobKey = entry.getValue().get(0);
                metadata.setUnindexedProperty(sizeString, blobKey);
                db.putAndCache(metadata);
            }
            tr.commit();
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