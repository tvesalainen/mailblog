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

/**
 * @author Timo Vesalainen
 */
public class LongType extends PropertyType<Long> 
{

    @Override
    public Long newInstance(String value)
    {
        if (value != null && !value.isEmpty())
        {
            return new Long(value);
        }
        return null;
    }

    @Override
    public String getString(Object obj)
    {
        Long value = (Long) obj;
        return value != null ? value.toString() : "";
    }

    @Override
    public String getDefaultInputType()
    {
        return "number";
    }

}
