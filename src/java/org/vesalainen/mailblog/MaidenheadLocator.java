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
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.GeoPt;

/**
 * @author Timo Vesalainen
 * @see <a href="http://en.wikipedia.org/wiki/Maidenhead_Locator_System">Maidenhead Locator System</a>
 */
public class MaidenheadLocator
{
    private String value;

    public MaidenheadLocator(GeoPt location)
    {
        this(location.getLatitude(), location.getLongitude());
    }

    public MaidenheadLocator(double latitude, double longitude)
    {
        latitude += 90;
        char latField = (char) ('A' + (latitude / 10));
        char latSquare = (char) ('0' + latitude % 10);
        char latSubsquare = (char) ('A' + (latitude % 1) * 24);
        
        longitude += 180;
        longitude /= 2;
        char lonField = (char) ('A' + (longitude / 10));
        char lonSquare = (char) ('0' + longitude % 10);
        char lonSubsquare = (char) ('A' + (longitude % 1) * 24);
        
        value = new String(new char[] {lonField, latField, lonSquare, latSquare, lonSubsquare, latSubsquare});
        
    }

    public String getField()
    {
        return value.substring(0, 2);
    }
    
    public String getSquare()
    {
        return value.substring(0, 4);
    }
    
    public String getSubsquare()
    {
        return value;
    }
    
    @Override
    public String toString()
    {
        return value;
    }
    
}
