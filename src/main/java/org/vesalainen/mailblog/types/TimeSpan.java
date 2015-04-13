/*
 * Copyright (C) 2015 tkv
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

import com.google.appengine.api.datastore.Entity;
import java.util.Date;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author tkv
 */
public class TimeSpan
{
    private long begin = Long.MAX_VALUE;
    private long end = Long.MIN_VALUE;

    public TimeSpan()
    {
    }

    public TimeSpan(Entity entity)
    {
        this(
                ((Date)entity.getProperty(BeginProperty)).getTime(),
                ((Date)entity.getProperty(EndProperty)).getTime()
        );
    }
    
    public TimeSpan(Date begin, Date end)
    {
        if (begin != null)
        {
            this.begin = begin.getTime();
        }
        if (end != null)
        {
            this.end = end.getTime();
        }
    }

    public TimeSpan(long begin, long end)
    {
        this.begin = begin;
        this.end = end;
    }

    public void populate(Entity entity)
    {
        if (begin != Long.MAX_VALUE)
        {
            entity.setProperty(BeginProperty, new Date(begin));
        }
        if (end != Long.MIN_VALUE)
        {
            entity.setProperty(EndProperty, new Date(end));
        }
    }
    public void clear()
    {
        begin = Long.MAX_VALUE;
        end = Long.MIN_VALUE;
    }
    public void add(TimeSpan span)
    {
        add(span.begin);
        add(span.end);
    }
    public void add(Date date)
    {
        add(date.getTime());
    }
    public void add(long time)
    {
        begin = Math.min(begin, time);
        end = Math.max(end, time);
    }
    public boolean isInside(TimeSpan ts)
    {
        return isInside(ts.begin) && isInside(ts.end);
    }
    public boolean isInside(Date time)
    {
        return isInside(time.getTime());
    }
    public boolean isInside(long time)
    {
        return time >= begin && time <= end;
    }

    @Override
    public String toString()
    {
        return "{" + begin + " - " + end + '}';
    }
    
}
