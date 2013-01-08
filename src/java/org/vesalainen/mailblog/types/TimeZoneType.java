/*
 * Copyright (C) 2013 Timo Vesalainen
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Timo Vesalainen
 */
public class TimeZoneType extends PropertyType<String>
{

    @Override
    public String newInstance(String value)
    {
        if (value != null && !value.isEmpty())
        {
            return value;
        }
        return null;
    }

    @Override
    public String getHtmlInput(Map attributes, Object value)
    {
        TimeZone selectTz = null;
        String id = (String) value;
        if (id != null)
        {
            selectTz = TimeZone.getTimeZone(id);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<select");
        appendAttributes(sb, attributes);
        sb.append(">");
        List<TimeZone> availableTzs = new ArrayList<TimeZone>();
        for (String aid : TimeZone.getAvailableIDs())
        {
            availableTzs.add(TimeZone.getTimeZone(aid));
        }
        Collections.sort(availableTzs, new TimeZoneComp());
        
        for (TimeZone tz : availableTzs)
        {
            sb.append("<option");
            appendAttribute(sb, "value", tz.getID());
            if (tz.equals(selectTz))
            {
                appendAttribute(sb, "selected", true);
            }
            sb.append(">");
            sb.append(tz.getDisplayName()+" "+tz.getID());
            sb.append("</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

    @Override
    public String getString(Object obj)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class TimeZoneComp implements Comparator<TimeZone>
    {

        @Override
        public int compare(TimeZone o1, TimeZone o2)
        {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
        
    }
}
