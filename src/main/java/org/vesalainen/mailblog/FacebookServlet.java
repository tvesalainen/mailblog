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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.BlogParameter;

/**
 *
 * @author tkv
 */
public class FacebookServlet extends HttpServlet
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
        try
        {
            DS ds = DS.get();
            if (!ds.sameETagOrCached(request, response))
            {
                URL base = DS.getBase(request);
                try (DS.CacheWriter cw = ds.createCacheWriter(request, response))
                {
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
                    cw.cache();
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
