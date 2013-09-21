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
import java.io.IOException;
import java.io.PrintWriter;
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
public class LastPositionServlet extends HttpServlet
{

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
        Settings settings = ds.getSettings();
        if (settings.isCommonPlacemarks())
        {
            String namespace = NamespaceManager.get();
            try
            {
                NamespaceManager.set(null);
                if (!ds.serveFromCache(request, response))
                {
                    try (DS.CacheWriter cacheWriter = ds.createCacheWriter(request, response, "text/html", "utf-8", false))
                    {
                        ds.writeLastPosition(cacheWriter, settings);
                    }
                }
            }
            finally
            {
                NamespaceManager.set(namespace);
            }
        }
        else
        {
            if (!ds.serveFromCache(request, response))
            {
                try (CacheWriter cacheWriter = ds.createCacheWriter(request, response, "text/html", "utf-8", false))
                {
                    ds.writeLastPosition(cacheWriter, settings);
                }
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
    }

}
