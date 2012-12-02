/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Timo Vesalainen
 */
public class BlogServlet extends HttpServlet implements BlogConstants
{
    private DB db;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        db = new DB();
    }


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
        try
        {
            
            String blobKey = request.getParameter(BlobParameter);
            if (blobKey != null)
            {
                log("blob="+blobKey);
                Entity blobEntity = db.get(blobKey);
                String contentType = (String) blobEntity.getProperty(ContentTypeProperty);
                log(contentType);
                Blob blob = (Blob) blobEntity.getProperty(BlobProperty);
                Date timestamp = (Date) blobEntity.getProperty(TimestampProperty);
                response.setDateHeader("Last-Modified", timestamp.getTime());
                response.setContentType(contentType);
                byte[] bytes = blob.getBytes();
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(bytes);
            }
            else
            {
                String blogKey = Util.getRefererParameter(request, BlogParameter);
                if (blogKey != null)
                {
                    log("blog="+blogKey);
                    Entity blog = db.get(blogKey);
                    response.setContentType("application/json");
                    JSONObject json = new JSONObject();
                    Text body = (Text) blog.getProperty(HtmlProperty);
                    Date timestamp = (Date) blog.getProperty(TimestampProperty);
                    response.setDateHeader("Last-Modified", timestamp.getTime());
                    json.put("content", body.getValue());
                    json.write(response.getWriter());
                }
                else
                {
                    log("list");
                    response.setContentType("application/json");
                    JSONObject json = new JSONObject();
                    Locale locale = Util.getLocale(request);
                    log(locale.toString());
                    String list = db.getBlogList(locale);
                    json.put("content", list);
                    json.write(response.getWriter());
                }
            }
        }
        catch (EntityNotFoundException ex)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        catch (JSONException ex)
        {
            throw new ServletException(ex);
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
    }// </editor-fold>
}
