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

import com.google.appengine.api.NamespaceManager;
import java.util.Date;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author tkv
 */
public abstract class BaseServlet extends HttpServlet
{

    protected URIBuilder addNamespace(URIBuilder builder)
    {
        String namespace = NamespaceManager.get();
        if (builder.getUri().getHost().endsWith(namespace))
        {
            return builder;
        }
        else
        {
            return builder.setQuery(BlogConstants.NamespaceParameter + "=" + NamespaceManager.get());
        }
    }

    protected int getAlpha(Date begin)
    {
        if (begin == null)
        {
            return 0;
        }
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        long x0 = ds.getTrackSeqsBegin().getTime();
        long xn = System.currentTimeMillis();
        int minOpaque = settings.getMinOpaque();
        double span = 255 - minOpaque;
        double c = span / Math.sqrt(xn - x0);
        int age = (int) Math.round(c * Math.sqrt(xn - begin.getTime()));
        return 255 - age;
    }
    
}
