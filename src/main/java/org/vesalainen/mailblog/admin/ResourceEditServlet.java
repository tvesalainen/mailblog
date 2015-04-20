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
package org.vesalainen.mailblog.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS;
import org.vesalainen.mailblog.RunInNamespace;

/**
 *
 * @author tkv
 */
public class ResourceEditServlet extends FieldSettingsServlet
{

    public ResourceEditServlet()
    {
        super(ResourceKind);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        clearProperties();
        RunInNamespace rin = new RunInNamespace()
        {
            @Override
            protected Object run()
            {
                Key key = KeyFactory.createKey(DS.getRootKey(), kind, BaseKey);
                try
                {
                    DS ds = DS.get();
                    Entity entity = ds.get(key);
                    Map<String,Object> map = new TreeMap<>();
                    map.putAll(entity.getProperties());
                    for (String name : map.keySet())
                    {
                        Object value = map.get(name);
                        addProperty(name)
                                .setType(value.getClass());
                    }
                }
                catch (EntityNotFoundException ex)
                {
                    log(ex.getMessage(), ex);
                }
                return null;
            }
        };
        rin.doIt(null); // read empty namespace settings
        super.doGet(req, resp);
    }

    @Override
    public Object getDefault(final String name)
    {
        log(name);
        RunInNamespace rin = new RunInNamespace()
        {
            @Override
            protected Object run()
            {
                Key key = KeyFactory.createKey(DS.getRootKey(), kind, BaseKey);
                try
                {
                    DS ds = DS.get();
                    Entity entity = ds.get(key);
                    return entity.getProperty(name);
                }
                catch (EntityNotFoundException ex)
                {
                    log(ex.getMessage(), ex);
                }
                return null;
            }
        };
        return rin.doIt(null);
    }
}
