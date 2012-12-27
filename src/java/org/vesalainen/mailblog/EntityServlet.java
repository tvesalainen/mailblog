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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    public static final String New = "new";
    protected String kind;
    protected List<Property> properties = new ArrayList<Property>();

    public EntityServlet(String kind)
    {
        this.kind = kind;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
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
                if (key == null)
                {
                    throw new ServletException("key == null");
                }
                Entity entity = getEntity(key);
                content = getInputTable(entity);
            }
            resp.setContentType("text/html ;charset=utf-8");
            resp.getWriter().write(content);
        }
        catch (HttpException ex)
        {
            log(ex.getMessage(), ex);
            ex.sendError(resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        log(req.getParameterMap().toString());
        try
        {
            String delete = req.getParameter(Delete);
            if (delete != null)
            {
                Key key = KeyFactory.stringToKey(delete);
                delete(key);
            }
            else
            {
                update(req);
            }
        }
        catch (HttpException ex)
        {
            log(ex.getMessage(), ex);
            ex.sendError(resp);
        }
    }

    protected String getInputTable(Entity entity)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<input type=\"hidden\" name=\"" + Key + "\" value=\"" + KeyFactory.keyToString(entity.getKey()) + "\"/>");
        sb.append("<div><table>");
        Key key = entity.getKey();
        if (key.getName() != null)
        {
            sb.append("<tr><th>Name</th><td>" + key.getName() + "</td></tr>");
        }
        else
        {
            sb.append("<tr><th>Id</th><td>" + key.getId() + "</td></tr>");

        }
        for (Property property : properties)
        {
            sb.append("<tr>");
            sb.append("<th>");
            sb.append(property.getName());
            sb.append("</th>");
            sb.append("<td>");
            sb.append(property.getInputElement(entity));
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table></div>");
        return sb.toString();
    }

    protected Property addProperty(String name)
    {
        Property property = new Property(name);
        properties.add(property);
        return property;
    }

    protected Key getKey(HttpServletRequest req) throws HttpException
    {
        String keyString = req.getParameter(Key);
        if (keyString != null)
        {
            return KeyFactory.stringToKey(keyString);
        }
        else
        {
            Key key = createNewKey(req);
            DB db = DB.DB;
            try
            {
                db.get(key);
                throw new HttpException(HttpServletResponse.SC_CONFLICT, key+" exists");
            }
            catch (EntityNotFoundException ex)
            {
                return key;
            }
        }
    }

    protected Entity getEntity(Key key) throws HttpException
    {
        DB db = DB.DB;
        try
        {
            return db.get(key);
        }
        catch (EntityNotFoundException ex)
        {
            return new Entity(key);
        }
    }

    protected Key createNewKey(HttpServletRequest req) throws HttpException
    {
        String name = req.getParameter(New);
        if (name == null)
        {
            throw new HttpException(HttpServletResponse.SC_NOT_FOUND, New+" not found");
        }
        Key parent = getParent();
        if (parent != null)
        {
            return KeyFactory.createKey(parent, kind, name);
        }
        else
        {
            return KeyFactory.createKey(kind, name);
        }
    }

    protected String getTitle(Entity entity)
    {
        return entity.getKey().getName();
    }

    protected String createSelect()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<select class=\"entitySelect\">");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(kind);
        PreparedQuery prepared = datastore.prepare(query);
        for (Entity entity : prepared.asIterable())
        {
            sb.append("<option value=\"" + KeyFactory.keyToString(entity.getKey()) + "\">" + getTitle(entity) + "</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

    protected void delete(Key key)
    {
        log("delete " + key);
        DB db = DB.DB;
        db.delete(key);
    }

    protected void update(HttpServletRequest req) throws HttpException
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
            putEntity(entity);
        }
        else
        {
            throw new HttpException(HttpServletResponse.SC_CONFLICT, "key == null");
        }
    }

    protected void putEntity(Entity entity)
    {
        DB db = DB.DB;
        db.putAndCache(entity);
    }

    protected Key getParent()
    {
        return null;
    }

    protected class Property
    {

        private String name;
        private Class<?> type;
        private String inputType;
        private boolean indexed;
        private boolean mandatory;
        private Constructor constructor;
        private Map<String, String> attributes = new HashMap<String, String>();

        protected Property(String name)
        {
            this.name = name;
            setType(String.class);
        }

        public Property setType(Class<?> type)
        {
            this.type = type;
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
            return this;
        }

        public Property setInputType(String inputType)
        {
            if (inputType != null)
            {
                this.inputType = inputType;
            }
            else
            {
                this.inputType = "text";
                if (type.equals(Long.class)
                        || type.equals(Rating.class))
                {
                    this.inputType = "number";
                }
                if (type.equals(Email.class))
                {
                    this.inputType = "email";
                }
                if (type.equals(Link.class))
                {
                    this.inputType = "url";
                }
                if (type.equals(PhoneNumber.class))
                {
                    this.inputType = "tel";
                }
                if (type.equals(Date.class))
                {
                    this.inputType = "date";
                }
            }
            return this;
        }

        public Property setIndexed(boolean indexed)
        {
            this.indexed = indexed;
            return this;
        }

        public Property setMandatory(boolean mandatory)
        {
            this.mandatory = mandatory;
            return this;
        }

        public Property setAttribute(String name, String value)
        {
            attributes.put(name, value);
            return this;
        }

        public Property setAttribute(String name, boolean value)
        {
            if (value)
            {
                attributes.put(name, name);
            }
            else
            {
                attributes.remove(name);
            }
            return this;
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
            String attrs = "";
            if (mandatory)
            {
                attrs = "class=\"mandatory\"";
            }
            for (Entry<String, String> entry : attributes.entrySet())
            {
                attrs = attrs + " " + entry.getKey() + "=\"" + entry.getValue() + "\"";
            }
            Object value = entity.getProperty(name);
            if (type.equals(String.class))
            {
                String val = (String) value;
                String str = value != null ? val : "";
                return "<input " + attrs + " type=\"" + inputType + "\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            if (type.equals(Text.class))
            {
                Text val = (Text) value;
                String str = value != null ? val.getValue() : "";
                return "<textarea " + attrs + " name=\"" + name + "\">" + str + "</textarea>";
            }
            if (type.equals(Long.class))
            {
                Long val = (Long) value;
                String str = value != null ? val.toString() : "";
                return "<input " + attrs + " type=\"" + inputType + "\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            if (type.equals(Rating.class))
            {
                Rating val = (Rating) value;
                String str = value != null ? String.valueOf(val.getRating()) : "";
                return "<input " + attrs + " type=\"number\" min=\"" + Rating.MIN_VALUE + "\" max=\"" + Rating.MAX_VALUE + "\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            if (type.equals(Email.class))
            {
                Email val = (Email) value;
                String str = value != null ? val.getEmail() : "";
                return "<input " + attrs + " type=\"" + inputType + "\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            if (type.equals(Link.class))
            {
                Link val = (Link) value;
                String str = value != null ? val.getValue() : "";
                return "<input " + attrs + " type=\"" + inputType + "\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            if (type.equals(PhoneNumber.class))
            {
                PhoneNumber val = (PhoneNumber) value;
                String str = value != null ? val.getNumber() : "";
                return "<input " + attrs + " type=\"" + inputType + "\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            if (type.equals(Category.class))
            {
                Category val = (Category) value;
                String str = value != null ? val.getCategory() : "";
                return "<input " + attrs + " type=\"text\" name=\"" + name + "\" value=\"" + str + "\"/>";
            }
            /*
             if (type.equals(Date.class))
             {
             Date val = (Date) value;
                
             String str = value != null ? dateParser.formatISO8601(val) : "";
             return "<input "+classAttr+" type=\""+inputType+"\" name=\""+name+"\" value=\""+str+"\"/>";
             }
             */
            if (type.equals(Boolean.class))
            {
                Boolean val = (Boolean) value;
                boolean checked = value != null ? val.booleanValue() : false;
                if (checked)
                {
                    return "<input type=\"checkbox\" name=\"" + name + "\" checked=\"checked\" value=\"" + name + "\"/>";
                }
                else
                {
                    return "<input type=\"checkbox\" name=\"" + name + "\" value=\"" + name + "\"/>";
                }
            }
            throw new IllegalArgumentException("unsupported type " + type);
        }
    }
}
