/*
 * Copyright (C) 2015 tkv
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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import java.net.URL;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.BlogParameter;

/**
 *
 * @author tkv
 */
public class FacebookFilter implements Filter
{
    private FilterConfig filterConfig;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String userAgent = request.getHeader("User-Agent");
        String pathInfo = request.getPathInfo();
        boolean jpg = pathInfo != null && pathInfo.toLowerCase().endsWith(".jpg");
        boolean fbCrawler = userAgent != null && userAgent.startsWith("facebookexternalhit/1.1");
        if (fbCrawler && !jpg)
        {
            URL base = DS.getBase(request);
            DS ds = DS.get();
            try (DS.CacheWriter cw = ds.createCacheWriter(request, response))
            {
                cw.setPrivate(true);
                cw.append("<!DOCTYPE html>\n");
                cw.append("<html>\n");
                cw.append("<head>\n");
                String blogKeyString = request.getParameter(BlogParameter);
                if (blogKeyString != null)
                {
                    Key blogKey = KeyFactory.stringToKey(blogKeyString);
                    ds.writeOpenGraph(blogKey, base, cw);
                }
                else
                {
                    ds.writeOpenGraph(base, cw);
                }
                cw.append("</head>\n");
                cw.append("</html>\n");
                cw.cache();
                return;
            }
            catch (HttpException ex)
            {
                ex.sendError(response);
                return;
            }
        }
        fc.doFilter(req, res);
    }

    /**
     * Destroy method for this filter
     */
    @Override
    public void destroy()
    {
    }

    public void log(String msg)
    {
        filterConfig.getServletContext().log(msg);
    }

    @Override
    public void init(FilterConfig fc) throws ServletException
    {
        filterConfig = fc;
    }
}
