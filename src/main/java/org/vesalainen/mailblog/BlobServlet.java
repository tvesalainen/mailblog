/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen
 */
public class BlobServlet extends HttpServlet
{

    /**
     * Handles the HTTP <code>GET</code> method.
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
        response.setHeader("Cache-Control", "public, max-age=86400");
        String sha1;
        String original;
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.toLowerCase().endsWith(".jpg"))
        {
            sha1 = pathInfo.substring(1, pathInfo.length() - 4);
            original = "true";
        }
        else
        {
            sha1 = request.getParameter(Sha1Parameter);
            original = request.getParameter(OriginalParameter);
        }
        log("sha1=" + sha1);
        if (sha1 != null)
        {
            String ifNoneMatch = request.getHeader("If-None-Match");
            if (ifNoneMatch != null)
            {
                // sha1 named content newer changes
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            DS ds = DS.get();
            Entity metadata = ds.getMetadata(sha1);
            if (metadata != null)
            {
                String filename = (String) metadata.getProperty(FilenameProperty);
                Date timestamp = (Date) metadata.getProperty(TimestampProperty);
                if (timestamp == null)
                {
                    timestamp = new Date(0);
                }
                String eTag = String.valueOf(timestamp.getTime());
                response.setHeader("ETag", eTag);
                if (original != null)
                {
                    BlobKey originalBlobKey = (BlobKey) metadata.getProperty(OriginalSizeProperty);
                    if (originalBlobKey != null)
                    {
                        serve(originalBlobKey, response);
                        return;
                    }
                    else
                    {
                        serve(IMG_ORIG+sha1+filename, response);
                        return;
                    }
                }
                BlobKey webBlobKey = (BlobKey) metadata.getProperty(WebSizeProperty);
                if (webBlobKey != null)
                {
                    serve(webBlobKey, response);
                    return;
                }
                else
                {
                    serve(IMG_WEB+sha1+filename, response);
                    return;
                }
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void serve(BlobKey webBlobKey, HttpServletResponse response) throws IOException
    {
        try
        {
            DS ds = DS.get();
            String filename = ds.getMigratedGCSFilename(webBlobKey);
            serve(filename, response);
        }
        catch (EntityNotFoundException ex)
        {
            throw new IOException(ex);
        }
    }

    private void serve(String filename, HttpServletResponse response) throws IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        String bucketName = settings.getGCBucketName();
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get(bucketName);
        if (bucket == null)
        {
            throw new IOException(bucketName + " bucket not found");
        }
        Blob blob = bucket.get(filename);
        if (blob == null)
        {
            throw new IOException(filename + " file not found");
        }
        response.setContentType(blob.getContentType());
        ServletOutputStream outputStream = response.getOutputStream();
        blob.downloadTo(outputStream);
        response.flushBuffer();
    }

}
