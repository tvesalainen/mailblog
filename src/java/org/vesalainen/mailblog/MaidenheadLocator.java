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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Timo Vesalainen
 * @see <a href="http://en.wikipedia.org/wiki/Maidenhead_Locator_System">Maidenhead Locator System</a>
 */
public class MaidenheadLocator implements BlogConstants
{
    private static final float FieldStepLon = 360 / 18;
    private static final float SquareStepLon = FieldStepLon / 10;
    private static final float SubsquareStepLon = SquareStepLon / 24;
    private static final float FieldStepLat = 180 / 18;
    private static final float SquareStepLat = FieldStepLat / 10;
    private static final float SubsquareStepLat = SquareStepLat / 24;
    private static final int LIMIT = 30;
    private String value;
    public enum LocatorLevel {Field, Square, Subsquare };

    private MaidenheadLocator(String value)
    {
        this.value = value;
    }
    
    public MaidenheadLocator(GeoPt location)
    {
        this(location.getLatitude(), location.getLongitude());
    }

    public MaidenheadLocator(double latitude, double longitude)
    {
        value = getMaidenheadLocator(latitude, longitude);
        
    }

    public static String getMaidenheadLocator(double latitude, double longitude)
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
        
        return new String(new char[] {lonField, latField, lonSquare, latSquare, lonSubsquare, latSubsquare});
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
        MaidenheadLocator ml = new MaidenheadLocator(location);
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
                    MaidenheadLocator sw = new MaidenheadLocator(south, west);
                    MaidenheadLocator ne = new MaidenheadLocator(north, east);
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, sw.getField()));
                    filters.add(new Query.FilterPredicate(FieldProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, ne.getField()));
                }
            }
        }
    }

    public static int fieldCountBetween(float west, float south, float east, float north)
    {
        return fieldCountBetweenLon(west,east)*fieldCountBetweenLat(south,north);
    }
    public static int fieldCountBetweenLon(float c1, float c2)
    {
        return (int) ((Math.abs(c1-c2)/FieldStepLon)+1);
    }
    public static int fieldCountBetweenLat(float c1, float c2)
    {
        return (int) ((Math.abs(c1-c2)/FieldStepLat)+1);
    }
    public static int squareCountBetween(float west, float south, float east, float north)
    {
        return squareCountBetweenLon(west,east)*squareCountBetweenLat(south,north);
    }
    public static int squareCountBetweenLon(float c1, float c2)
    {
        return (int) ((Math.abs(c1-c2)/SquareStepLon)+1);
    }
    public static int squareCountBetweenLat(float c1, float c2)
    {
        return (int) ((Math.abs(c1-c2)/SquareStepLat)+1);
    }
    public static int subsquareCountBetween(float west, float south, float east, float north)
    {
        return subsquareCountBetweenLon(west,east)*subsquareCountBetweenLat(south,north);
    }
    public static int subsquareCountBetweenLon(float c1, float c2)
    {
        return (int) ((Math.abs(c1-c2)/SubsquareStepLon)+1);
    }
    public static int subsquareCountBetweenLat(float c1, float c2)
    {
        return (int) ((Math.abs(c1-c2)/SubsquareStepLat)+1);
    }
    public static Set<String> fieldsBetween(float west, float south, float east, float north)
    {
        if (west > east || south > north)
        {
            throw new IllegalArgumentException(west+" > "+east+" || "+south+" > "+north);
        }
        Set<String> set = new TreeSet<>();
        for (float lon = west;lon<=east;lon +=FieldStepLon)
        {
            for (float lat = south;lat <= north;lat += FieldStepLat)
            {
                set.add(getMaidenheadLocator(lat, lon).substring(0, 2));
            }
        }
        return set;
    }
    public static Set<String> squaresBetween(float west, float south, float east, float north)
    {
        if (west > east || south > north)
        {
            throw new IllegalArgumentException(west+" > "+east+" || "+south+" > "+north);
        }
        Set<String> set = new TreeSet<>();
        for (float lon = west;lon<=east;lon +=SquareStepLon)
        {
            for (float lat = south;lat <= north;lat += SquareStepLat)
            {
                set.add(getMaidenheadLocator(lat, lon).substring(0, 4));
            }
        }
        return set;
    }
    public static Set<String> subsquaresBetween(float west, float south, float east, float north)
    {
        if (west > east || south > north)
        {
            throw new IllegalArgumentException(west+" > "+east+" || "+south+" > "+north);
        }
        Set<String> set = new TreeSet<>();
        for (float lon = west;lon<=east;lon +=SubsquareStepLon)
        {
            for (float lat = south;lat <= north;lat += SubsquareStepLat)
            {
                set.add(getMaidenheadLocator(lat, lon));
            }
        }
        return set;
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

    public static void main(String[] args)
    {
        try
        {
            System.err.println(MaidenheadLocator.fieldCountBetween(-23.64533645010307F,21.60106994204968F,-9.760809816680036F,32.5324557504879F));
            Set<String> fieldsBetween = MaidenheadLocator.fieldsBetween(-23.64533645010307F,21.60106994204968F,-9.760809816680036F,32.5324557504879F);
            System.err.println(fieldsBetween.size());
            System.err.println(fieldsBetween);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
