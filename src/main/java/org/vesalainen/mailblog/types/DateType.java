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

import org.vesalainen.web.HTML5Datetime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Timo Vesalainen
 */
public class DateType extends PropertyType<Date> 
{
    public static final HTML5Datetime parser = HTML5Datetime.getInstance();
    public static final String DateFormat = "yyyy-MM-dd'T'HH:mm";
    
    @Override
    public Date newInstance(String value)
    {
        if (value != null && !value.isEmpty())
        {
            return parser.parse(value);
        }
        return null;
    }

    @Override
    public String getString(Object obj)
    {
        Date value = (Date) obj;
        SimpleDateFormat format = new SimpleDateFormat(DateFormat);
        return value != null ? format.format(value) : "";
    }

    @Override
    public String getDefaultInputType()
    {
        return "datetime-local";
    }

}
