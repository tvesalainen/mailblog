/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.gpx.TrackHandler;
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

    public OpenCPNTrackHandler(DS ds)
    {
        this.ds = ds;
    }
    
    @Override
    public boolean startTrack(Collection<Object> extensions)
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
        ds.put(trackSeq);
        ds.put(trackPoints);
        trackPoints.clear();
        trackSeq = null;
    }

    @Override
    public void trackPoint(double latitude, double longitude, long time)
    {
        box.add(latitude, longitude);
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
