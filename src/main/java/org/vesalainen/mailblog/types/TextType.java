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

package org.vesalainen.mailblog.types;

import com.google.appengine.api.datastore.Text;
import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class TextType extends PropertyType<Text> 
{

    @Override
    public String getHtmlInput(Map<String, String> attributes, Object value)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<textarea");
        appendAttributes(sb, attributes);
        sb.append(">");
        String text = getString(value);
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        sb.append(text);
        sb.append("</textarea>");
        
        return sb.toString();
    }

    @Override
    public Text newInstance(String value)
    {
        if (value != null && !value.isEmpty())
        {
            value = value.replace("&lt;", "<");
            value = value.replace("&gt;", ">");
            return new Text(value);
        }
        return null;
    }

    @Override
    public String getString(Object obj)
    {
        Text value = (Text) obj;
        return value != null ? value.getValue() : "";
    }

}
