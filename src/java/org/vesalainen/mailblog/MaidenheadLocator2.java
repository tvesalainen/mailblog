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
    
    public static void addFilters(List<Query.Filter> filters, float west, float south, float east, float north)
    {
        int subsquareCountBetween = subsquareCountBetween(west, south, east, north);
        if (subsquareCountBetween < LIMIT)
        {
            Set<String> subsquaresBetween = subsquaresBetween(west, south, east, north);
            filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.IN, subsquaresBetween));
        }
        else
        {
            int squareCountBetween = squareCountBetween(west, south, east, north);
            if (squareCountBetween < LIMIT)
            {
                Set<String> squaresBetween = squaresBetween(west, south, east, north);
                filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.IN, squaresBetween));
            }
            else
            {
                int fieldCountBetween = fieldCountBetween(west, south, east, north);
                if (fieldCountBetween < LIMIT)
                {
                    Set<String> fieldsBetween = fieldsBetween(west, south, east, north);
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.IN, fieldsBetween));
                }
                else
                {
                    MaidenheadLocator2 sw = new MaidenheadLocator2(south, west);
                    MaidenheadLocator2 ne = new MaidenheadLocator2(north, east);
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, sw.getField()));
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, ne.getField()));
                }
            }
        }
    }

}
