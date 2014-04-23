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

import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class BooleanType extends PropertyType<Boolean> 
{

    @Override
    public String getHtmlInput(Map<String,String> attributes, Object obj)
    {
        Boolean value = (Boolean) obj;
        StringBuilder sb = new StringBuilder();
        sb.append("<input");
        appendAttributes(sb, attributes);
        appendAttribute(sb, "type", "checkbox");
        boolean val = value != null ? value.booleanValue() : false;
        appendAttribute(sb, "checked", val);
        appendAttribute(sb, "value", attributes.get("name"));
        
        sb.append("/>");
        return sb.toString();
    }
    @Override
    public Boolean newInstance(String value)
    {
        if (value != null)
        {
            return Boolean.TRUE;
        }
        else
        {
            return Boolean.FALSE;
        }
    }

    @Override
    public String getString(Object obj)
    {
        Boolean value = (Boolean) obj;
        return value != null ? value.toString() : "";
    }

}
