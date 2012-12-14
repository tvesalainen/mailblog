/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
    /*
    private DB db;
    private BlobstoreService blobstore;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        db = new DB();
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
    }
    */


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
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        DB db = DB.DB;
        try
        {
            
            String blobKey = request.getParameter(BlobParameter);
            if (blobKey != null)
            {
                log("blob="+blobKey);
                BlobKey bk = new BlobKey(blobKey);
                String original = request.getParameter(OriginalParameter);
                if (original != null)
                {
                    BlobInfoFactory bif = new BlobInfoFactory();
                    BlobInfo loadBlobInfo = bif.loadBlobInfo(bk);
                    Iterator<BlobInfo> iterator = bif.queryBlobInfos();
                    while (iterator.hasNext())
                    {
                        BlobInfo bi = iterator.next();
                        if (    bi.getFilename().equals(loadBlobInfo.getFilename()) && 
                                bi.getSize() > loadBlobInfo.getSize()
                                )
                        {
                            bk = bi.getBlobKey();
                        }
                    }
                }
                blobstore.serve(bk, response);
            }
            else
            {
                String calendar = request.getParameter(CalendarParameter);
                if (calendar != null)
                {
                    log("calendar");
                    response.setContentType("application/json");
                    JSONObject json = new JSONObject();
                    String calendarString = db.getCalendar();
                    log(calendarString);
                    json.put("calendar", calendarString);
                    json.write(response.getWriter());
                }
                else
                {
                    String blogKey = request.getParameter(BlogParameter);
                    if (blogKey != null)
                    {
                        log("selected");
                        response.setContentType("text/html");
                        response.getWriter().write(db.getBlog(blogKey));
                    }
                    else
                    {
                        log("latest");
                        response.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        json.put("blog", db.getBlogList());
                        json.write(response.getWriter());
                    }
                }
            }
        }
        catch (EntityNotFoundException ex)
        {
            log("", ex);
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
