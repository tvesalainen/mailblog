/*
 * Copyright (C) 2012 Timo Vesalainen
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

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timo Vesalainen
 */
public abstract class WebSafe
{

    public WebSafe()
    {
    }

    public static WebSafe newInstance(Class<? extends WebSafe> cls, String webSafe) throws IOException
    {
        try
        {
            WebSafe newInstance = cls.newInstance();
            WebSafeReader reader = new WebSafeReader(webSafe);
            String fieldname = reader.getFieldname();
            while (fieldname != null)
            {
                Field field = cls.getDeclaredField(fieldname);
                Object value = null;
                String ws = reader.getWebsafe();
                if (!ws.isEmpty())
                {
                    value = newInstance.getValue(field.getType(), ws);
                    field.set(newInstance, value);
                }
                fieldname = reader.getFieldname();
            }
            return newInstance;
        }
        catch (NoSuchFieldException ex)
        {
            throw new IOException(ex);
        }
        catch (SecurityException ex)
        {
            throw new IOException(ex);
        }
        catch (InstantiationException ex)
        {
            throw new IOException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
    }

    public String getWebSafe() throws IOException
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            for (Field field : getClass().getDeclaredFields())
            {
                String name = field.getName();
                if (name.length() > 0xf)
                {
                    throw new IllegalArgumentException("field "+name+" name is too long (>0xf)");
                }
                sb.append(String.format("%x", name.length()));
                sb.append(name);
                String ws = "";
                Object value = field.get(this);
                if (value != null)
                {
                    ws = getWebSafe(field.getType(), value);
                }
                if (name.length() > 0xff)
                {
                    throw new IllegalArgumentException("fields "+name+" websafe content is too long (>0xff)");
                }
                if (
                        ws.indexOf("<") != -1 ||
                        ws.indexOf(">") != -1 ||
                        ws.indexOf(" ") != -1
                        )
                {
                    throw new IllegalArgumentException("fields "+name+" websafe content is not websafe");
                }
                sb.append(String.format("%02x", ws.length()));
                sb.append(ws);
            }
            return sb.toString();
        }
        catch (SecurityException ex)
        {
            throw new IOException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
    }
    
    protected abstract String getWebSafe(Class<?> type, Object value);
    
    protected abstract Object getValue(Class<?> type, String websafe);
}
