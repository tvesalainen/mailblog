/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
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
import org.vesalainen.mailblog.DS.CacheWriter;

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
        DS ds = DS.get();
        if (!ds.serveFromCache(request, response))
        {
            try
            {
                URL base = getBase(request);
                String calendar = request.getParameter(CalendarParameter);
                if (calendar != null)
                {
                    CacheWriter cacheWriter = ds.createCacheWriter(request, response);
                    ds.getCalendar(cacheWriter);
                }
                else
                {
                    String keywords = request.getParameter(KeywordsParameter);
                    if (keywords != null)
                    {
                        CacheWriter cacheWriter = ds.createCacheWriter(request, response);
                        ds.getKeywordSelect(cacheWriter);
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
                                User user = null;
                                UserService userService = UserServiceFactory.getUserService();
                                if (userService.isUserLoggedIn())
                                {
                                    user = userService.getCurrentUser();
                                }
                                CacheWriter cacheWriter = ds.createCacheWriter(request, response, "text/html", "utf-8", true);
                                ds.getComments(blogKey, user, cacheWriter);
                            }
                            else
                            {
                                String action = request.getParameter(ActionParameter);
                                if (action != null)
                                {
                                    String auth = request.getParameter(AuthParameter);
                                    CacheWriter cacheWriter = ds.createCacheWriter(request, response, "text/plain", "utf-8", true);
                                    ds.handleBlogAction(blogKey, action, auth, cacheWriter);
                                }
                                else
                                {
                                    CacheWriter cacheWriter = ds.createCacheWriter(request, response);
                                    ds.getBlog(blogKey, base, cacheWriter);
                                }
                            }
                        }
                        else
                        {
                            String blogCursor = request.getParameter(CursorParameter);
                            boolean all = request.getParameter(AllParameter) != null;
                            CacheWriter cacheWriter = ds.createCacheWriter(request, response);
                            ds.getBlogList(blogCursor, base, all, cacheWriter);
                        }
                    }
                }
            }
            catch (HttpException ex)
            {
                log("", ex);
                ex.sendError(response);
            }
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
        try
        {
            URL base = getBase(request);
            response.setHeader("Cache-Control", "private, max-age=0, no-cache");
            DS ds = DS.get();
            String search = request.getParameter(SearchParameter);
            if (search != null)
            {
                if (!ds.serveFromCache(request, response))
                {
                    BlogCursor bc = new BlogCursor()
                            .setSearch(search);
                    CacheWriter cacheWriter = ds.createCacheWriter(request, response);
                    ds.getBlogList(bc.getWebSafe(), base, false, cacheWriter);
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
                            log(loginURL);
                            response.getWriter().write(loginURL);
                        }
                    }
                }
                else
                {
                    String removeComment = request.getParameter(RemoveCommentParameter);
                    if (removeComment != null)
                    {
                        Key commentKey = KeyFactory.stringToKey(removeComment);
                        UserService userService = UserServiceFactory.getUserService();
                        if (userService.isUserLoggedIn())
                        {
                            ds.removeComment(commentKey, userService.getCurrentUser());
                        }
                        else
                        {
                            String loginURL = userService.createLoginURL("");
                            log(loginURL);
                            response.getWriter().write(loginURL);
                        }
                    }
                }
            }
        }
        catch (HttpException ex)
        {
            log(ex.getMessage(), ex);
            ex.sendError(response);
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
