/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.mailblog.admin;

import com.google.appengine.api.datastore.KeyFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.mailblog.BlogConstants;
import org.vesalainen.mailblog.DS;
import org.vesalainen.mailblog.HttpException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class FieldSettingsServlet extends EntityServlet
{

    public FieldSettingsServlet(String kind)
    {
        super(kind);
    }

    @Override
    protected com.google.appengine.api.datastore.Key getKey(HttpServletRequest req) throws HttpException
    {
        DS ds = DS.get();
        com.google.appengine.api.datastore.Key key = KeyFactory.createKey(DS.getRootKey(), kind, BlogConstants.BaseKey);
        String keyString = req.getParameter(Key);
        if (keyString != null)
        {
            com.google.appengine.api.datastore.Key requestKey = KeyFactory.stringToKey(keyString);
            if (!key.equals(requestKey))
            {
                throw new HttpException(HttpServletResponse.SC_CONFLICT, key + " and request key " + requestKey + " differs");
            }
        }
        return key;
    }
    
}
