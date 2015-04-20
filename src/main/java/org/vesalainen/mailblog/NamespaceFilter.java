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

import com.google.appengine.api.NamespaceManager;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Timo Vesalainen
 */
public class NamespaceFilter implements Filter
{

    private FilterConfig filterConfig = null;

    public NamespaceFilter()
    {
    }

    /**
     * Init method for this filter
     */
    @Override
    public void init(FilterConfig filterConfig)
    {
        this.filterConfig = filterConfig;
        if (filterConfig != null)
        {
        }
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String namespace = "sailfaraway.net";  //request.getParameter("namespace"); FOR TESTING ONLY
        if (namespace != null)
        {
            NamespaceManager.set(namespace);
            log("namespace set to " + NamespaceManager.get() + " from parameter");
        }
        else
        {
            namespace = getNamespaceFromReferer(request);
            if (namespace != null)
            {
                NamespaceManager.set(namespace);
                log("namespace set to " + NamespaceManager.get() + " from referer");
            }
            else
            {
                if (NamespaceManager.get() == null)
                {
                    NamespaceManager.set(NamespaceManager.getGoogleAppsNamespace());
                    log("namespace set to " + NamespaceManager.get() + " from domain");
                }
                else
                {
                    log("namespace was " + NamespaceManager.get());
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String getNamespaceFromReferer(HttpServletRequest request)
    {
        String referer = request.getHeader("referer");
        if (referer != null)
        {
            int i1 = referer.indexOf("namespace=");
            if (i1 != -1)
            {
                int i2 = referer.indexOf("&", i1);
                if (i2 != -1)
                {
                    return referer.substring(i1 + 10, i2);
                }
                else
                {
                    return referer.substring(i1 + 10);
                }
            }
        }
        return null;
    }
    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig()
    {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig)
    {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy()
    {
    }

    public void log(String msg)
    {
        filterConfig.getServletContext().log(msg);
    }
}
