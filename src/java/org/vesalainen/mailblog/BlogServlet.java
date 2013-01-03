/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    private DS db;
    private BlobstoreService blobstore;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        db = new DS();
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
        URL base = getBase(request);
        response.setHeader("Cache-Control", "private, max-age=0, no-cache");
        DS ds = DS.get();
        try
        {
            
            String calendar = request.getParameter(CalendarParameter);
            if (calendar != null)
            {
                String calendarString = ds.getCalendar();
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().write(calendarString);
            }
            else
            {
                String keywords = request.getParameter(KeywordsParameter);
                if (keywords != null)
                {
                    response.setContentType("text/html; charset=UTF-8");
                    response.getWriter().write(ds.getKeywordSelect());
                }
                else
                {
                    String blogKeyString = request.getParameter(BlogParameter);
                    if (blogKeyString != null)
                    {
                        Key blogKey = KeyFactory.stringToKey(blogKeyString);
                        String comments = request.getParameter(CommentsParameter);
                        if (comments != null)
                        {
                            response.setContentType("text/html; charset=UTF-8");
                            response.getWriter().write(ds.getComments(blogKey));
                        }
                        else
                        {
                            response.setContentType("text/html; charset=UTF-8");
                            response.getWriter().write(ds.getBlog(blogKey, base));
                        }
                    }
                    else
                    {
                        String blogCursor = request.getParameter(CursorParameter);
                        response.setContentType("text/html; charset=UTF-8");
                        response.getWriter().write(ds.getBlogList(blogCursor, base));
                    }
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
        URL base = getBase(request);
        response.setHeader("Cache-Control", "private, max-age=0, no-cache");
        DS ds = DS.get();
        String search = request.getParameter(SearchParameter);
        if (search != null)
        {
            try
            {
                log("search="+search);
                BlogCursor bc = new BlogCursor()
                        .setSearch(search);
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().write(ds.getBlogList(bc.getWebSafe(), base));
            }
            catch (EntityNotFoundException ex)
            {
                log("", ex);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        else
        {
            String blogKeyString = request.getParameter(BlogParameter);
            if (blogKeyString != null)
            {
                Key blogKey = KeyFactory.stringToKey(blogKeyString);
                String comment = request.getParameter(CommentParameter);
                if (comment != null)
                {
                    UserService userService = UserServiceFactory.getUserService();
                    if (userService.isUserLoggedIn())
                    {
                        ds.addComment(blogKey, userService.getCurrentUser(), comment);
                    }
                    else
                    {
                        String loginURL = userService.createLoginURL("");
                        response.getWriter().write(loginURL);
                    }
                }
            }
        }
    }

    private URL getBase(HttpServletRequest request) throws IOException
    {
        try
        {
            URI uri = new URI(request.getRequestURL().toString());
            return uri.resolve("/").toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new IOException(ex);
        }
        catch (URISyntaxException ex)
        {
            throw new IOException(ex);
        }
    }

}
