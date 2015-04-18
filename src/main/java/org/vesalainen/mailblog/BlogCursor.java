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

import com.google.appengine.api.datastore.Cursor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author Timo Vesalainen
 */
public class BlogCursor extends WebSafe
{
    protected com.google.appengine.api.datastore.Cursor datastoreCursor;
    protected Date begin;
    protected Date end;
    protected String search;
    protected com.google.appengine.api.search.Cursor searchCursor;

    public BlogCursor()
    {
    }

    public boolean isSearch()
    {
        return search != null;
    }
    
    public BlogCursor setDatastoreCursor(Cursor datastoreCursor)
    {
        this.datastoreCursor = datastoreCursor;
        return this;
    }

    public BlogCursor setBegin(Date begin)
    {
        this.begin = begin;
        return this;
    }

    public BlogCursor setEnd(Date end)
    {
        this.end = end;
        return this;
    }

    public BlogCursor setSearch(String search)
    {
        this.search = search;
        return this;
    }

    public BlogCursor setSearchCursor(com.google.appengine.api.search.Cursor searchCursor)
    {
        this.searchCursor = searchCursor;
        return this;
    }

    public com.google.appengine.api.datastore.Cursor getDatastoreCursor()
    {
        return datastoreCursor;
    }

    public Date getBegin()
    {
        return begin;
    }

    public Date getEnd()
    {
        return end;
    }

    public String getSearch()
    {
        return search;
    }

    public com.google.appengine.api.search.Cursor getSearchCursor()
    {
        return searchCursor;
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
        catch (NoSuchFieldException | SecurityException | IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected String getWebSafe(Class<?> type, Object value)
    {
        if (String.class.equals(type))
        {
            String str = (String) value;
            return Hex.convertToHex(str);
        }
        if (Date.class.equals(type))
        {
            Date date = (Date) value;
            if (date != null)
            {
                return Long.toHexString(date.getTime());
            }
        }
        if (com.google.appengine.api.datastore.Cursor.class.equals(type))
        {
            com.google.appengine.api.datastore.Cursor cursor = (com.google.appengine.api.datastore.Cursor) value;
            return cursor.toWebSafeString();
        }
        if (com.google.appengine.api.search.Cursor.class.equals(type))
        {
            com.google.appengine.api.search.Cursor cursor = (com.google.appengine.api.search.Cursor) value;
            return cursor.toWebSafeString();
        }
        throw new UnsupportedOperationException(type+" not supported yet.");
    }

    @Override
    protected Object getValue(Class<?> type, String websafe)
    {
        if (String.class.equals(type))
        {
            return Hex.convertFromHex(websafe);
        }
        if (Date.class.equals(type))
        {
            long l = Long.parseLong(websafe, 16);
            return new Date(l);
        }
        if (com.google.appengine.api.datastore.Cursor.class.equals(type))
        {
            return com.google.appengine.api.datastore.Cursor.fromWebSafeString(websafe);
        }
        if (com.google.appengine.api.search.Cursor.class.equals(type))
        {
            return com.google.appengine.api.search.Cursor.newBuilder().build(websafe);
        }
        throw new UnsupportedOperationException(type+" not supported yet.");
    }

}
