package org.vesalainen.mailblog;


import java.io.FileInputStream;
import org.vesalainen.navi.Navis;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.ais.AbstractAISObserver;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Timo Vesalainen
 */
public abstract class NMEATrackLevelHandler
{
    private double distanceLimit;
    private double bearingLimit;
    
    private double lastLatitude = Double.NaN;
    private double lastLongitude = Double.NaN;
    private double lastBearing = Double.NaN;
    private long lastTimestamp = -1L;
    /**
     * 
     * @param distanceLimit in NM
     * @param bearingLimit  in degrees
     */
    public NMEATrackLevelHandler(double distanceLimit, double bearingLimit)
    {
        this.distanceLimit = distanceLimit;
        this.bearingLimit = bearingLimit;
    }

    public void setWaypoint(double latitude, double longitude, long timestamp)
    {
        if (Double.isNaN(lastLatitude))
        {
            lastLatitude = latitude;
            lastLongitude = longitude;
            lastTimestamp = timestamp;
            storeWaypoint(latitude, longitude, timestamp);
        }
        else
        {
            double distance = Navis.distance(lastLatitude, lastLongitude, latitude, longitude);
            if (distance > distanceLimit)
            {
                double bearing = Navis.bearing(lastLatitude, lastLongitude, latitude, longitude);
                if (Double.isNaN(lastBearing))
                {
                    lastBearing = bearing;
                }
                else
                {
                    if (Math.abs(lastBearing - bearing) > bearingLimit)
                    {
                        storeWaypoint(latitude, longitude, timestamp);
                        storeLeg(lastLatitude, lastLongitude, lastTimestamp, latitude, longitude, timestamp);
                        lastLatitude = latitude;
                        lastLongitude = longitude;
                        lastTimestamp = timestamp;
                        lastBearing = bearing;
                    }
                }
            }
        }
    }

    protected abstract void storeWaypoint(double latitude, double longitude, long timestamp);
    protected abstract void storeLeg(double latitude1, double longitude1, long timestamp1, double latitude2, double longitude2, long timestamp2);

}
