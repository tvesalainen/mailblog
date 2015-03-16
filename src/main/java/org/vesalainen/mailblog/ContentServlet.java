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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.servlet.ServletContext;
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
public class ContentServlet extends HttpServlet
{
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
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
        String pathInfo = request.getPathInfo();
        log("pathInfo="+pathInfo);
        if (pathInfo != null)
        {
            if ("/".equals(pathInfo))
            {
                pathInfo = "/index.html";
            }
            ServletContext servletContext = getServletContext();
            String mimeType = servletContext.getMimeType(pathInfo);
            if (mimeType == null)
            {
                log("mimetype not found "+pathInfo);
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                return;
            }
            DS ds = DS.get();
            Entity page = ds.getPageEntity(pathInfo.substring(1));
            if (page != null)
            {
                Date timestamp = (Date) page.getProperty(TimestampProperty);
                if (timestamp == null)
                {
                    timestamp = new Date(0);
                }
                String eTag = String.valueOf(timestamp.getTime());
                String ifNoneMatch = request.getHeader("If-None-Match");
                if (eTag.equals(ifNoneMatch))
                {
                    response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                response.setHeader("ETag", eTag);
                response.setContentType(mimeType);
                Text text = (Text) page.getProperty(PageProperty);
                if (text != null)
                {
                    String content = text.getValue();
                    if (content == null)
                    {
                        log("Text found but page is empty"+pathInfo);
                        response.sendError(HttpServletResponse.SC_CONFLICT);
                        return;
                    }
                    response.getWriter().write(content);
                }
                else
                {
                    BlobKey blobKey = (BlobKey) page.getProperty(FileProperty);
                    if (blobKey != null)
                    {
                        blobstoreService.serve(blobKey, response);
                        return;
                    }
                    else
                    {
                        log("Text or BlobKey not found "+pathInfo);
                        response.sendError(HttpServletResponse.SC_CONFLICT);
                        return;
                    }
                }
            }
            else
            {
                InputStream is = getServletContext().getResourceAsStream(pathInfo);
                if (is != null)
                {
                    ServletOutputStream os = response.getOutputStream();
                    byte[] buf = new byte[4096];
                    int rc = is.read(buf);
                    while (rc != -1)
                    {
                        os.write(buf, 0, rc);
                        rc = is.read(buf);
                    }
                }
                else
                {
                    log("Not found from file resources "+pathInfo);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            return;
        }
        else
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }
}
