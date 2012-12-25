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

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.Text;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Timo Vesalainen
 */
public abstract class EntityServlet extends HttpServlet
{
    public static final String Key = "key";
    public static final String Select = "select";
    public static final String Delete = "delete";
    
    protected String kind;
    protected List<Property> properties = new ArrayList<Property>();

    public EntityServlet(String kind)
    {
        this.kind = kind;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String content = null;
        String list = req.getParameter(Select);
        if (list != null)
        {
            content = createSelect();
        }
        else
        {
            Key key = getKey(req);
            if (key != null)
            {
                DB db = DB.DB;
                try
                {
                    Entity entity = db.get(key);
                    content = getInputTable(entity);
                }
                catch (EntityNotFoundException ex)
                {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            else
            {
                content = getInputTable(null);
            }
        }
        resp.getWriter().write(content);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String delete = req.getParameter(Delete);
        if (delete != null)
        {
            Key key = KeyFactory.stringToKey(delete);
            log("delete "+key);
            DB db = DB.DB;
            db.delete(key);
        }
        else
        {
            Key key = getKey(req);
            if (key != null)
            {
                DB db = DB.DB;
                Entity entity = null;
                try
                {
                    entity = db.get(key);
                }
                catch (EntityNotFoundException ex)
                {
                    entity = new Entity(key);
                }
                for (Property property : properties)
                {
                    Object value = property.newInstance(req.getParameter(property.getName()));
                    log(property.getName()+"="+value);
                    if (value != null)
                    {
                        if (property.isIndexed())
                        {
                            entity.setProperty(property.getName(), value);
                        }
                        else
                        {
                            entity.setUnindexedProperty(property.getName(), value);
                        }
                    }
                    else
                    {
                        entity.removeProperty(property.getName());
                    }
                }
                db.putAndCache(entity);
            }
            else
            {
                resp.sendError(HttpServletResponse.SC_CONFLICT);
            }
        }
    }

    protected String getInputTable(Entity entity)
    {
        StringBuilder sb = new StringBuilder();
        if (entity != null)
        {
            sb.append("<input type=\"hidden\" name=\""+Key+"\" value=\""+KeyFactory.keyToString(entity.getKey())+"\"/>");
        }
        sb.append("<table>");
        for (Property property : properties)
        {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(property.getName());
            sb.append("</td>");
            sb.append("<td>");
            sb.append(property.getInputElement(entity));
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }
    protected void addProperty(String name, Class<?> type, boolean indexed)
    {
        properties.add(new Property(name, type, indexed));
    }
    protected Key getKey(HttpServletRequest req)
    {
        String keyString = req.getParameter(Key);
        if (keyString != null)
        {
            return KeyFactory.stringToKey(keyString);
        }
        else
        {
            return createKey(req);
        }
    }

    protected abstract Key createKey(HttpServletRequest req);
    protected abstract String getTitle(Entity entity);

    protected String createSelect()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<select>");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(kind);
        PreparedQuery prepared = datastore.prepare(query);
        for (Entity entity : prepared.asIterable())
        {
            sb.append("<option value=\""+KeyFactory.keyToString(entity.getKey())+"\">"+getTitle(entity)+"</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }
    
    protected class Property
    {
        private String name;
        private Class<?> type;
        private boolean indexed;
        private Constructor constructor;

        protected Property(String name, Class<?> type, boolean indexed)
        {
            this.name = name;
            this.type = type;
            this.indexed = indexed;
            try
            {
                this.constructor = type.getConstructor(String.class);
            }
            catch (NoSuchMethodException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            catch (SecurityException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }

        protected Object newInstance(String str)
        {
            if (type.equals(Boolean.class))
            {
                if (str == null)
                {
                    return Boolean.FALSE;
                }
                else
                {
                    return Boolean.TRUE;
                }
            }
            if (str == null || str.isEmpty())
            {
                return null;
            }
            try
            {
                return constructor.newInstance(str);
            }
            catch (InstantiationException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            catch (IllegalArgumentException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        protected String getName()
        {
            return name;
        }

        protected Class<?> getType()
        {
            return type;
        }

        public boolean isIndexed()
        {
            return indexed;
        }
        
        protected String getInputElement(Entity entity)
        {
            Object value = entity.getProperty(name);
            if (type.equals(String.class))
            {
                String val = (String) value;
                String str = value != null ? val : "";
                return "<input type=\"text\" name=\""+name+"\" value=\""+str+"\" size=\"40\"/>";
            }
            if (type.equals(Text.class))
            {
                Text val = (Text) value;
                String str = value != null ? val.getValue() : "";
                return "<textarea name=\""+name+"\" rows=\"20\" cols=\"80\">"+str+"</textarea>";
            }
            if (type.equals(Long.class))
            {
                Long val = (Long) value;
                String str = value != null ? val.toString() : "";
                return "<input type=\"number\" min=\"1\" max=\"10\" name=\""+name+"\" value=\""+str+"\"/>";
            }
            if (type.equals(Rating.class))
            {
                Rating val = (Rating) value;
                String str = value != null ? String.valueOf(val.getRating()) : "";
                return "<input type=\"number\" min=\""+Rating.MIN_VALUE+"\" max=\""+Rating.MAX_VALUE+"\" name=\""+name+"\" value=\""+str+"\"/>";
            }
            if (type.equals(Email.class))
            {
                Email val = (Email) value;
                String str = value != null ? val.getEmail() : "";
                return "<input type=\"email\" name=\""+name+"\" value=\""+str+"\" size=\"40\"/>";
            }
            if (type.equals(Link.class))
            {
                Link val = (Link) value;
                String str = value != null ? val.getValue() : "";
                return "<input type=\"url\" name=\""+name+"\" value=\""+str+"\" size=\"100\"/>";
            }
            if (type.equals(PhoneNumber.class))
            {
                PhoneNumber val = (PhoneNumber) value;
                String str = value != null ? val.getNumber() : "";
                return "<input type=\"tel\" name=\""+name+"\" value=\""+str+"\" size=\"40\"/>";
            }
            if (type.equals(Category.class))
            {
                Category val = (Category) value;
                String str = value != null ? val.getCategory() : "";
                return "<input type=\"text\" name=\""+name+"\" value=\""+str+"\"/>";
            }
            /*
            if (type.equals(Date.class))
            {
                Date val = (Date) value;
                
                String str = value != null ? dateParser.formatISO8601(val) : "";
                return "<input type=\"datetime\" name=\""+name+"\" value=\""+str+"\"/>";
            }
            */
            if (type.equals(Boolean.class))
            {
                Boolean val = (Boolean) value;
                boolean checked = value != null ? val.booleanValue() : false;
                if (checked)
                {
                    return "<input type=\"checkbox\" name=\""+name+"\" checked=\"checked\" value=\""+name+"\"/>";
                }
                else
                {
                    return "<input type=\"checkbox\" name=\""+name+"\" value=\""+name+"\"/>";
                }
            }
            throw new IllegalArgumentException("unsupported type "+type);
        }
    }
}
