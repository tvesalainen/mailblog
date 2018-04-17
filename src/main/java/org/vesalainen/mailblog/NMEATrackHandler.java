package org.vesalainen.mailblog;


import org.vesalainen.parsers.nmea.AbstractNMEAObserver;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Timo Vesalainen
 */
public class NMEATrackHandler extends AbstractNMEAObserver
{
    private final NMEATrackLevelHandler[] handlers;
    private double latitude;
    private double longitude;
    private boolean updated;

    public NMEATrackHandler(NMEATrackLevelHandler... handlers)
    {
        this.handlers = handlers;
    }

    @Override
    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
        updated = true;
    }

    @Override
    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
        updated = true;
    }
    
    @Override
    public void commit(String reason)
    {
        if (updated)
        {
            long timestamp = clock.millis();
            for (NMEATrackLevelHandler handler : handlers)
            {
                handler.setWaypoint(latitude, longitude, timestamp);
            }
            updated = false;
        }
    }

}
