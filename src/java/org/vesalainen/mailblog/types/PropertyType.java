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

package org.vesalainen.mailblog.types;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.Text;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * @author Timo Vesalainen
 */
public abstract class PropertyType<T> 
{
    public abstract T newInstance(String value);
    public abstract String getString(Object obj);
    public String getDefaultInputType()
    {
        return "text";
    }
    public String getHtmlInput(Map<String,String> attributes, Object value)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<input");
        appendAttributes(sb, attributes);
        if (!attributes.containsKey("type"))
        {
            appendAttribute(sb, "type", getDefaultInputType());
        }
        appendAttribute(sb, "value", getString(value));
        
        sb.append("/>");
        return sb.toString();
    }
    protected void appendAttribute(StringBuilder sb, String name, int value)
    {
        appendAttribute(sb, name, String.valueOf(value));
    }
    protected void appendAttribute(StringBuilder sb, String name, boolean value)
    {
        if (value)
        {
            appendAttribute(sb, name, name);
        }
    }
    protected void appendAttribute(StringBuilder sb, String name, String value)
    {
        sb.append(" ");
        sb.append(name);
        sb.append("=\"");
        sb.append(value);
        sb.append("\"");
    }
    protected void appendAttributes(StringBuilder sb, Map<String,String> attributes)
    {
        for (Entry<String,String> entry : attributes.entrySet())
        {
            sb.append(" ");
            sb.append(entry.getKey());
            sb.append("=\"");
            sb.append(entry.getValue());
            sb.append("\"");
        }
    }
    public static PropertyType getInstance(Class<? extends Collection> collectionType, Class<?> componentType)
    {
        return new CollectionType(collectionType, componentType);
    }
    public static PropertyType getInstance(Class<?> type)
    {
        if (String.class.isAssignableFrom(type))
        {
            return new StringType();
        }
        if (Text.class.isAssignableFrom(type))
        {
            return new TextType();
        }
        if (Long.class.isAssignableFrom(type))
        {
            return new LongType();
        }
        if (Double.class.isAssignableFrom(type))
        {
            return new DoubleType();
        }
        if (Rating.class.isAssignableFrom(type))
        {
            return new RatingType();
        }
        if (Email.class.isAssignableFrom(type))
        {
            return new EmailType();
        }
        if (Link.class.isAssignableFrom(type))
        {
            return new LinkType();
        }
        if (PhoneNumber.class.isAssignableFrom(type))
        {
            return new PhoneNumberType();
        }
        if (Category.class.isAssignableFrom(type))
        {
            return new CategoryType();
        }
        if (GeoPt.class.isAssignableFrom(type))
        {
            return new GeoPtType();
        }
        if (Date.class.isAssignableFrom(type))
        {
            return new DateType();
        }
        if (Boolean.class.isAssignableFrom(type))
        {
            return new BooleanType();
        }
        if (Locale.class.isAssignableFrom(type))
        {
            return new LocaleType();
        }
        if (TimeZone.class.isAssignableFrom(type))
        {
            return new TimeZoneType();
        }
        throw new IllegalArgumentException("unsupported type "+type);
    }
}
