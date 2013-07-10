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
    private NMEATrackLevelHandler[] handlers;
    private double latitude;
    private double longitude;
    private boolean updated;

    public NMEATrackHandler(NMEATrackLevelHandler... handlers)
    {
        this.handlers = handlers;
    }
    
    @Override
    public void setLocation(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        updated = true;
    }

    @Override
    public void commit(String reason)
    {
        if (updated)
        {
            long timestamp = clock.getTime();
            for (NMEATrackLevelHandler handler : handlers)
            {
                handler.setWaypoint(latitude, longitude, timestamp);
            }
            updated = false;
        }
    }

}
