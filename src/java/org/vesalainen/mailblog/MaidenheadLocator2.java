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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Timo Vesalainen
 * @see <a href="http://en.wikipedia.org/wiki/Maidenhead_Locator_System">Maidenhead Locator System</a>
 */
public class MaidenheadLocator2 extends MaidenheadLocator implements BlogConstants
{
    private static final int LIMIT = 30;

    public MaidenheadLocator2(GeoPt location)
    {
        super(location.getLatitude(), location.getLongitude());
    }

    public MaidenheadLocator2(double latitude, double longitude)
    {
        super(latitude, longitude);
    }
    /**
     * Set Location property to entity. Maidenhead properties are added with following rules.
     * If level is Field: Field, Square and Subsquare.
     * If level is Square: Square and Subsquare.
     * If level is Subsquare: Subsquare.
     * @param entity
     * @param location
     * @param level 
     */
    public static void setLocation(Entity entity, GeoPt location, LocatorLevel level)
    {
        setLocation(entity, location, level.ordinal());
    }
    public static void setLocation(Entity entity, GeoPt location, int level)
    {
        MaidenheadLocator2 ml = new MaidenheadLocator2(location);
        if (level <= LocatorLevel.Field.ordinal())
        {
            entity.setProperty(FieldProperty, ml.getField());
        }
        if (level <= LocatorLevel.Square.ordinal())
        {
            entity.setProperty(SquareProperty, ml.getSquare());
        }
        if (level <= LocatorLevel.Subsquare.ordinal())
        {
            entity.setProperty(SubsquareProperty, ml.getSubsquare());
        }
    }
    
    public static String getCacheKey(HttpServletRequest request)
    {
        MaidenheadLocator2[] bb = getBoundingBox(request);
        if (bb != null)
        {
            assert bb.length == 2;
            return bb[0].getField()+bb[1].getField();
        }
        else
        {
            return null;
        }
    }
    public static MaidenheadLocator2[] getBoundingBox(HttpServletRequest request)
    {
        String bbox = request.getParameter(BoundingBoxParameter);
        if (bbox != null)
        {
            String[] ss = bbox.split(",");
            float west = Float.parseFloat(ss[0]);
            float south = Float.parseFloat(ss[1]);
            float east = Float.parseFloat(ss[2]);
            float north = Float.parseFloat(ss[3]);
            MaidenheadLocator2 sw = new MaidenheadLocator2(south, west);
            MaidenheadLocator2 ne = new MaidenheadLocator2(north, east);
            return new MaidenheadLocator2[] {sw, ne};
        }
        else
        {
            return null;
        }
    }
    public static void addFilters(List<Query.Filter> filters, MaidenheadLocator2[] bb)
    {
        int subsquareCountBetween = subsquareCountBetween(bb);
        if (subsquareCountBetween < LIMIT)
        {
            Set<String> subsquaresBetween = subsquaresBetween(bb);
            filters.add(new Query.FilterPredicate(SubsquareProperty, Query.FilterOperator.IN, subsquaresBetween));
        }
        else
        {
            int squareCountBetween = squareCountBetween(bb);
            if (squareCountBetween < LIMIT)
            {
                Set<String> squaresBetween = squaresBetween(bb);
                filters.add(new Query.FilterPredicate(SquareProperty, Query.FilterOperator.IN, squaresBetween));
            }
            else
            {
                int fieldCountBetween = fieldCountBetween(bb);
                if (fieldCountBetween < LIMIT)
                {
                    Set<String> fieldsBetween = fieldsBetween(bb);
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.IN, fieldsBetween));
                }
                else
                {
                    MaidenheadLocator2 sw = bb[0];
                    MaidenheadLocator2 ne = bb[1];
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, sw.getField()));
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, ne.getField()));
                }
            }
        }
    }

}
