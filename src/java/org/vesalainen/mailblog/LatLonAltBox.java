/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.GeoPt;
import net.opengis.kml.LatLonAltBoxType;

/**
 * @author Timo Vesalainen
 */
public class LatLonAltBox
{
    private double north = -90.0;
    private double south = 90.0;
    private double west = 180.0;
    private double east = -180.0;

    public void add(GeoPt location)
    {
        add(location.getLatitude(), location.getLongitude());
    }
    /**
     * TODO longitude 180???
     * @param latitude
     * @param longitude 
     */
    public void add(double latitude, double longitude)
    {
        assert latitude >= -90;
        assert latitude <= 90;
        assert longitude >= -180;
        assert longitude <= 180;
        north = Math.max(north, latitude);
        south = Math.min(south, latitude);
        east = Math.max(east, longitude);
        west = Math.min(west, longitude);
    }

    public void populate(LatLonAltBoxType box)
    {
        box.setNorth(north);
        box.setSouth(south);
        box.setWest(west);
        box.setEast(east);
    }
}
