/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.vesalainen.gpx.TrackHandler;
import static org.vesalainen.mailblog.BlogConstants.BeginProperty;
import static org.vesalainen.mailblog.BlogConstants.NorthEastProperty;
import static org.vesalainen.mailblog.BlogConstants.SouthWestProperty;
import org.w3c.dom.Element;

/**
 * @author Timo Vesalainen
 */
public class OpenCPNTrackHandler implements TrackHandler, BlogConstants
{
    private DS ds;
    private Key trackKey;
    private Key trackSeqKey;
    private List<Entity> trackPoints = new ArrayList<>();
    private LatLonAltBox box = new LatLonAltBox();
    private long begin = Long.MAX_VALUE;
    private long end = Long.MIN_VALUE;

    public OpenCPNTrackHandler(DS ds)
    {
        this.ds = ds;
    }
    
    @Override
    public boolean startTrack(String name, Collection<Object> extensions)
    {
        String guid = getGuid(extensions);
        if (guid != null)
        {
            trackKey = ds.getTrackKey(guid);
            try
            {
                ds.get(trackKey);
                ds.deleteWithChilds(trackKey);
            }
            catch (EntityNotFoundException ex)
            {
            }
            Entity track = new Entity(trackKey);
            if (name != null)
            {
                track.setProperty(NameProperty, name);
            }
            trackKey = ds.put(track);
            return true;
        }
        else
        {
            System.err.println("no guid! Skipping track...");
            return false;
        }
    }

    @Override
    public void endTrack()
    {
    }

    @Override
    public void startTrackSeq()
    {
        Entity trackSeq = new Entity(TrackSeqKind, trackKey);
        trackSeqKey = ds.put(trackSeq);
    }

    @Override
    public void endTrackSeq()
    {
        Entity trackSeq;
        try
        {
            trackSeq = ds.get(trackSeqKey);
        }
        catch (EntityNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        trackSeq.setProperty(SouthWestProperty, box.getSouthWest());
        trackSeq.setProperty(NorthEastProperty, box.getNorthEast());
        box.clear();
        trackSeq.setProperty(BeginProperty, new Date(begin));
        trackSeq.setProperty(EndProperty, new Date(end));
        begin = Long.MAX_VALUE;
        end = Long.MIN_VALUE;
        ds.put(trackSeq);
        ds.put(trackPoints);
        trackPoints.clear();
        trackSeq = null;
    }

    @Override
    public void trackPoint(double latitude, double longitude, long time)
    {
        box.add(latitude, longitude);
        begin = Math.min(begin, time);
        end = Math.max(end, time);
        Entity point = new Entity(TrackPointKind, time, trackSeqKey);
        point.setProperty(LocationProperty, new GeoPt((float)latitude, (float)longitude));
        trackPoints.add(point);
    }
    public static String getGuid(Collection<Object> extensions)
    {
        for (Object ob : extensions)
        {
            if (ob instanceof Element)
            {
                Element el = (Element) ob;
                if (
                        "http://www.opencpn.org".equals(el.getNamespaceURI()) &&
                        "guid".equals(el.getLocalName())
                        )
                {
                    return el.getTextContent();
                }
            }
        }
        return null;
    }

}
