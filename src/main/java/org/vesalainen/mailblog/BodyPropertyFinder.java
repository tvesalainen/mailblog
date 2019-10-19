/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.bean.ExpressionParser;
import org.vesalainen.util.logging.JavaLogging;

/**
 * BodyPropertyFinder finds ${key=value} properties in message body and replaces
 * them with empty string.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class BodyPropertyFinder extends JavaLogging
{
    protected ExpressionParser parser  = new ExpressionParser(this::handle);

    public BodyPropertyFinder(Class<? extends BodyPropertyFinder> cls)
    {
        super(cls);
    }
    
    public String replace(String text)
    {
        return parser.replace(text);
    }
    protected String handle(String text)
    {
        String[] split = text.split("=");
        if (split.length != 2)
        {
            warning("illegal property %s", text);
            return "";
        }
        else
        {
            String key = split[0].trim();
            String value = split[1].trim();
            return handle(key, value);
        }
    }
    protected abstract String handle(String key, String value);
}
