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

import com.google.appengine.api.datastore.Rating;
import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class RatingType extends PropertyType<Rating> 
{

    @Override
    public String getHtmlInput(Map<String,String> attributes, Object value)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<input");
        appendAttributes(sb, attributes);
        if (!attributes.containsKey("type"))
        {
            appendAttribute(sb, "type", getDefaultInputType());
        }
        if (!attributes.containsKey("min"))
        {
            appendAttribute(sb, "min", Rating.MIN_VALUE);
        }
        if (!attributes.containsKey("max"))
        {
            appendAttribute(sb, "max", Rating.MAX_VALUE);
        }
        appendAttribute(sb, "value", getString(value));
        
        sb.append("/>");
        return sb.toString();
    }
    @Override
    public Rating newInstance(String value)
    {
        return new Rating(Integer.parseInt(value));
    }

    @Override
    public String getString(Object obj)
    {
        Rating value = (Rating) obj;
        return value != null ? String.valueOf(value.getRating()) : "";
    }
    
    @Override
    public String getDefaultInputType()
    {
        return "number";
    }

}
