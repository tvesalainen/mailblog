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

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.GeoPt;
import java.io.Serializable;
import java.util.Collection;
import org.vesalainen.repacked.net.opengis.kml.LatLonAltBoxType;

/**
 * @author Timo Vesalainen
 */
public class LatLonAltBox implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private static final double HalfCircle = 180;
    private static final double FullCircle = 360;
    private boolean init;
    private double north;
    private double south;
    private double west;
    private double east;

    public LatLonAltBox()
    {
    }

    public LatLonAltBox(GeoPt northEast, GeoPt southWest)
    {
        this(northEast.getLatitude(), northEast.getLongitude(), southWest.getLatitude(), southWest.getLongitude());
    }

    public LatLonAltBox(double north, double east, double south, double west)
    {
        this.north = north;
        this.east = east;
        this.south = south;
        this.west = west;
        init = true;
    }
    
    /**
     * Creates a LatLonAltBox from a string "south,west,north,east"
     * @param str 
     * @return  
     */
    public static LatLonAltBox getSouthWestNorthEastInstance(String str)
    {
        String[] split = str.split(",");
        if (split.length != 4)
        {
            throw new IllegalArgumentException(str);
        }
        return new LatLonAltBox(
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3]),
                Double.parseDouble(split[0]),
                Double.parseDouble(split[1])
        );
    }
    
    /**
     * 
     * @param center Center location
     * @param dia diameter in NM
     */
    public LatLonAltBox(GeoPt center, double dia)
    {
        this(center.getLatitude(), center.getLongitude(), dia);
    }
    /**
     * 
     * @param latitude
     * @param longitude
     * @param dia diameter in NM
     */
    public LatLonAltBox(double latitude, double longitude, double dia)
    {
        dia = dia / 60;    // 60 NM
        north = normalize(latitude+dia);
        south = normalize(latitude-dia);
        west = normalize(longitude-dia);
        east = normalize(longitude+dia);
        init = true;
    }

    public void add(Collection<GeoPt> locations)
    {
        for (GeoPt location : locations)
        {
            add(location);
        }
    }
    public void add(GeoPt location)
    {
        add(location.getLatitude(), location.getLongitude());
    }
    /**
     * @param latitude
     * @param longitude 
     */
    public void add(double latitude, double longitude)
    {
        assert latitude >= -90;
        assert latitude <= 90;
        assert longitude >= -180;
        assert longitude <= 180;
        if (init)
        {
            north = Math.max(north, latitude);
            south = Math.min(south, latitude);

            east = isWestToEast(east, longitude) ? longitude : east;
            west = !isWestToEast(west, longitude) ? longitude : west;
        }
        else
        {
            north = latitude;
            south = latitude;
            west = longitude;
            east = longitude;
            init = true;
        }
    }
    public boolean isIntersecting(LatLonAltBox o)
    {
        return
                (overlapLat(o.north) || overlapLat(o.south) || o.overlapLat(north) || o.overlapLat(south)) &&
                (overlapLon(o.west) || overlapLon(o.east) || o.overlapLon(west) || o.overlapLon(east));
    }
    private boolean overlapLat(double latitude)
    {
        return latitude <= north &&
                latitude >= south;
    }
    private boolean overlapLon(double longitude)
    {
        return  isWestToEast(longitude, east) &&
                isWestToEast(west, longitude);
    }
    public boolean isInside(GeoPt pt)
    {
        return isInside(pt.getLatitude(), pt.getLongitude());
    }
    public boolean isInside(double latitude, double longitude)
    {
        return overlapLat(latitude) && overlapLon(longitude);
    }
    public void clear()
    {
        north = 0;
        south = 0;
        west = 0;
        east = 0;
        init = false;
    }
    /**
     * Return area in square degrees
     * @return 
     */
    public double getArea()
    {
        double abs = Math.abs(east-west);
        if (abs > HalfCircle)
        {
            abs = FullCircle - abs;
        }
        return abs*(north-south)*Math.cos(Math.toRadians((north+south)/2));
    }
    public GeoPt getSouthWest()
    {
        return new GeoPt((float)south, (float)west);
    }
    public GeoPt getNorthEast()
    {
        return new GeoPt((float)north, (float)east);
    }

    public double getNorth()
    {
        return north;
    }

    public double getSouth()
    {
        return south;
    }

    public double getWest()
    {
        return west;
    }

    public double getEast()
    {
        return east;
    }
    
    private static final double normalize(double val)
    {
        return ((val + HalfCircle + FullCircle) % FullCircle) - HalfCircle;
    }
    /**
     * Return true if a2 right of a1
     * @param west
     * @param east
     * @return 
     */
    private static final boolean isWestToEast(double west, double east)
    {
        west += HalfCircle;
        east += HalfCircle;
        double d = east-west;
        if (Math.abs(d) <= HalfCircle)
        {
            return d >= 0;
        }
        else
        {
            return d < 0;
        }
    }
    public void populate(LatLonAltBoxType box)
    {
        box.setNorth(north);
        box.setSouth(south);
        box.setWest(west);
        box.setEast(east);
    }

}
