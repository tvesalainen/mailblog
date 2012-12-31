/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.EntityNotFoundException;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        response.setHeader("Cache-Control", "private, max-age=0, no-cache");
        DB db = DB.DB;
        try
        {
            
            String calendar = request.getParameter(CalendarParameter);
            if (calendar != null)
            {
                String calendarString = db.getCalendar();
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().write(calendarString);
            }
            else
            {
                String blogKey = request.getParameter(BlogParameter);
                if (blogKey != null)
                {
                    response.setContentType("text/html; charset=UTF-8");
                    response.getWriter().write(db.getBlog(blogKey));
                }
                else
                {
                    String blogCursor = request.getParameter(CursorParameter);
                    response.setContentType("text/html; charset=UTF-8");
                    response.getWriter().write(db.getBlogList(blogCursor));
                }
            }
        }
        catch (EntityNotFoundException ex)
        {
            log("", ex);
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

}
