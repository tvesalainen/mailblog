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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.types.PropertyType;

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
        DS ds = DS.get();
        StringBuilder sb = new StringBuilder();
        sb.append("<input type=\"hidden\" name=\"" + Key + "\" value=\"" + KeyFactory.keyToString(entity.getKey()) + "\"/>");
        sb.append("<div><table>");
        Key key = entity.getKey();
        if (key.getName() != null)
        {
            sb.append("<tr><th>Name</th><td class=\"entityId\">" + key.getName() + "</td></tr>");
        }
        else
        {
            sb.append("<tr><th>Id</th><td class=\"entityId\">" + key.getId() + "</td></tr>");

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

    protected void clearProperties()
    {
        properties.clear();
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
            DatastoreService datastore = DS.get();
            try
            {
                datastore.get(key);
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
        DatastoreService datastore = DS.get();
        try
        {
            return datastore.get(key);
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
            DS ds = DS.get();
            return KeyFactory.createKey(DS.getRootKey(), kind, name);
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
        DatastoreService datastore = DS.get();
        Query query = new Query(kind);
        modifySelectQuery(query);
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
        DS ds = DS.get();
        ds.deleteWithChilds(key);
    }

    protected void update(HttpServletRequest req) throws HttpException
    {
        Key key = getKey(req);
        if (key != null)
        {
            DatastoreService datastore = DS.get();
            Entity entity = null;
            try
            {
                entity = datastore.get(key);
            }
            catch (EntityNotFoundException ex)
            {
                entity = new Entity(key);
            }
            for (Property property : properties)
            {
                if (!property.is("disabled"))
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
            }
            entity.setProperty(TimestampProperty, new Date());
            datastore.put(entity);
        }
        else
        {
            throw new HttpException(HttpServletResponse.SC_CONFLICT, "key == null");
        }
    }

    protected Key getParent()
    {
        return null;
    }

    protected void modifySelectQuery(Query query)
    {
    }

    protected class Property
    {
        private PropertyType type;
        private boolean indexed;
        private Map<String, String> attributes = new HashMap<>();

        protected Property(String name)
        {
            setAttribute("name", name);
            setType(String.class);
        }

        public Property setType(Class<? extends Collection> type, Class<?> component)
        {
            this.type = PropertyType.getInstance(type, component);
            return this;
        }
        public Property setType(Class<?> type)
        {
            this.type = PropertyType.getInstance(type);
            return this;
        }
        /**
         * Sets HTML5 input type. Note! Use after setType method, because setType
         * sets a default type for input.
         * @param inputType
         * @return 
         */
        public Property setInputType(String inputType)
        {
            setAttribute("type", inputType);
            return this;
        }

        public Property setIndexed(boolean indexed)
        {
            this.indexed = indexed;
            return this;
        }

        public Property setMandatory()
        {
            return addClass("mandatory");
        }

        public boolean is(String attribute)
        {
            return attributes.containsKey(attribute);
        }
        
        public Property addClass(String classname)
        {
            String classes = attributes.get("class");
            if (classes == null)
            {
                attributes.put("class", classname);
            }
            else
            {
                attributes.put("class", classes+" "+classname);
            }
            return this;
        }
        public Property setTooltip(String text)
        {
            return setAttribute("title", text);
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

        protected Object newInstance(String str) throws HttpException
        {
            return type.newInstance(str);
        }

        public boolean isIndexed()
        {
            return indexed;
        }

        public String getName()
        {
            return attributes.get("name");
        }
        protected String getInputElement(Entity entity)
        {
            Object value = entity.getProperty(attributes.get("name"));
            return type.getHtmlInput(attributes, value);
        }
    }
}
