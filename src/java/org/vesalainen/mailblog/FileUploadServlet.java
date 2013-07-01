/*
 * Copyright (C) 2013 Timo Vesalainen
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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
public class FileUploadServlet extends HttpServlet implements BlogConstants
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
        String namespace = NamespaceManager.get();
        if (namespace == null)
        {
            namespace = "";
        }
        response.setContentType("text/html ;charset=utf-8");
        PrintWriter pw = response.getWriter();
        pw.print("<html>");
        pw.print("<head>");
        pw.print("<title>Upload Test</title>");
        pw.print("</head>");
        pw.print("<body>");
        pw.print("<form action=" + blobstoreService.createUploadUrl("/admin/settings/fileupload?namespace="+namespace) + " method=\"post\" enctype=\"multipart/form-data\">");
        pw.print("<input type=\"text\" name=\"filename\">");
        pw.print("<input type=\"file\" name=\"myFile\">");
        pw.print("<input type=\"submit\" value=\"Submit\">");
        pw.print("</form>");
        pw.print("</body>");
        pw.print("</html>");
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
        String filename = request.getParameter("filename");
        if (filename != null)
        {
            Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
            List<BlobKey> blobKeys = blobs.get("myFile");
            if (blobKeys.size() == 1)
            {
                DS ds = DS.get();
                Entity page = new Entity(PageKind, filename, DS.getRootKey());
                page.setProperty(FileProperty, blobKeys.get(0));
                page.setProperty(TimestampProperty, new Date());
                ds.put(page);
                response.setContentType("text/ascii ;charset=utf-8");
                PrintWriter pw = response.getWriter();
                pw.print("Storing of "+filename+" succeeded!");
            }
        }
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
