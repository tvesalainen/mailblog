/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS.CacheWriter;

/**
 *
 * @author Timo Vesalainen
 */
public class BlogServlet extends HttpServlet
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
        if (!ds.sameETagOrCached(request, response))
        {
            try
            {
                URL base = DS.getBase(request);
                String calendar = request.getParameter(CalendarParameter);
                if (calendar != null)
                {
                    try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                    {
                        ds.getCalendar(cacheWriter);
                    }
                }
                else
                {
                    String keywords = request.getParameter(KeywordsParameter);
                    if (keywords != null)
                    {
                        try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                        {
                            ds.getKeywordSelect(cacheWriter);
                        }
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
                                try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                                {
                                    cacheWriter.setPrivate(true);
                                    ds.getComments(blogKey, user, cacheWriter);
                                }
                            }
                            else
                            {
                                String action = request.getParameter(ActionParameter);
                                if (action != null)
                                {
                                    String auth = request.getParameter(AuthParameter);
                                    try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                                    {
                                        cacheWriter.setPrivate(true)
                                        .setContentType("text/plain");
                                        ds.handleBlogAction(blogKey, action, auth, cacheWriter);
                                    }
                                }
                                else
                                {
                                    try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                                    {
                                        ds.getBlog(blogKey, base, cacheWriter);
                                    }
                                }
                            }
                        }
                        else
                        {
                            String nextBlog = request.getParameter(NextBlogParameter);
                            boolean all = request.getParameter(AllParameter) != null;
                            try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                            {
                                ds.getBlogList(nextBlog, base, all, cacheWriter);
                            }
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
            URL base = DS.getBase(request);
            DS ds = DS.get();
            String search = request.getParameter(SearchParameter);
            if (search != null)
            {
                if (search.isEmpty())
                {
                    // empty string in search field
                    try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                    {
                        ds.getBlogList(null, base, false, cacheWriter);
                    }
                }
                else
                {
                    BlogCursor bc = new BlogCursor()
                            .setSearch(search);
                    try (CacheWriter cacheWriter = ds.createCacheWriter(request, response))
                    {
                        cacheWriter.setPrivate(true);
                        Searches.getBlogListFromSearch(bc, base, cacheWriter);
                    }
                }
            }
            else
            {
                response.setHeader("Cache-Control", "private, max-age=0, no-cache");
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

}
