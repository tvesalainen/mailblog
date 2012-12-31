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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Timo Vesalainen
 */
public class DateType extends PropertyType<Date> 
{
    public static final String DateFormat = "yyyy-MM-dd"; // TODO in java 6 only supported format.
    // TODO in java 6 only supported format.
    // TODO in java 7 bring Parsers ISO8601 parser

    @Override
    public Date newInstance(String value)
    {
        SimpleDateFormat format = new SimpleDateFormat(DateFormat);
        try
        {
            return format.parse(value);
        }
        catch (ParseException ex)
        {
            throw new IllegalArgumentException(ex);
        }
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
        return "date";
    }

}
