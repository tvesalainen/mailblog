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

import com.google.appengine.api.datastore.Cursor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timo Vesalainen
 */
public class BlogCursor extends WebSafe
{
    protected Cursor cursor;
    protected Date begin;
    protected Date end;
    protected String keyword;

    public BlogCursor()
    {
    }

    public BlogCursor(Cursor cursor, Date begin, Date end, String keyword)
    {
        this.cursor = cursor;
        this.begin = begin;
        this.end = end;
        this.keyword = keyword;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public Cursor getCursor()
    {
        return cursor;
    }

    public Date getBegin()
    {
        return begin;
    }

    public Date getEnd()
    {
        return end;
    }

    public BlogCursor(String webSafe) throws IOException
    {
        try
        {
            WebSafeReader reader = new WebSafeReader(webSafe);
            String fieldname = reader.getFieldname();
            while (fieldname != null)
            {
                Field field = getClass().getDeclaredField(fieldname);
                String ws = reader.getWebsafe();
                if (!ws.isEmpty())
                {
                    Object value = getValue(field.getType(), ws);
                    field.set(this, value);
                }
                fieldname = reader.getFieldname();
            }
        }
        catch (NoSuchFieldException ex)
        {
            Logger.getLogger(BlogCursor.class.getName()).log(Level.SEVERE, null, ex);
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
    
    @Override
    protected String getWebSafe(Class<?> type, Object value)
    {
        if (Date.class.equals(type))
        {
            Date date = (Date) value;
            if (date != null)
            {
                return Long.toHexString(date.getTime());
            }
        }
        if (Cursor.class.equals(type))
        {
            Cursor cursor = (Cursor) value;
            return cursor.toWebSafeString();
        }
        throw new UnsupportedOperationException(type+" not supported yet.");
    }

    @Override
    protected Object getValue(Class<?> type, String websafe)
    {
        if (Date.class.equals(type))
        {
            long l = Long.parseLong(websafe, 16);
            return new Date(l);
        }
        if (Cursor.class.equals(type))
        {
            return Cursor.fromWebSafeString(websafe);
        }
        throw new UnsupportedOperationException(type+" not supported yet.");
    }

}
