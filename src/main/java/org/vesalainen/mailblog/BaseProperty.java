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
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class BaseProperty implements Serializable
{

    protected static final long serialVersionUID = 2L;
    protected Map<String, Object> map = new HashMap<>();

    public BaseProperty(final String kind, final DS db, Entity entity)
    {
        assert kind.equals(entity.getKind());
        RunInNamespace rin = new RunInNamespace()
        {
            @Override
            protected Object run()
            {
                Key key = KeyFactory.createKey(DS.getRootKey(), kind, BaseKey);
                try
                {
                    Entity entity = db.get(key);
                    putAll(entity.getProperties());
                }
                catch (EntityNotFoundException ex)
                {
                }
                return null;
            }
        };
        rin.doIt(null); // read empty namespace settings

        populate(db, entity);
    }

    protected final void populate(DS db, Entity entity)
    {
        Key parent = entity.getParent();
        if (parent != null && entity.getKind().equals(parent.getKind()))
        {
            Entity ent;
            try
            {
                ent = db.get(parent);
            }
            catch (EntityNotFoundException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            populate(db, ent);
        }
        putAll(entity.getProperties());
    }

    protected void putAll(Map<String, Object> m)
    {
        for (Map.Entry<String, Object> e : m.entrySet())
        {
            if (e.getValue() != null)
            {
                map.put(e.getKey(), e.getValue());
            }
        }
    }

    public Map<String, Object> getMap()
    {
        return map;
    }

    public int getIntProperty(String property)
    {
        return getIntProperty(property, 0);
    }

    public int getIntProperty(String property, int def)
    {
        Long l = (Long) map.get(property);
        if (l != null)
        {
            return l.intValue();
        }
        else
        {
            return def;
        }
    }

    public double getDoubleProperty(String property)
    {
        return getDoubleProperty(property, 0.0);
    }
    public double getDoubleProperty(String property, double def)
    {
        Double d = (Double) map.get(property);
        if (d != null)
        {
            return d;
        }
        else
        {
            return def;
        }
    }

}
