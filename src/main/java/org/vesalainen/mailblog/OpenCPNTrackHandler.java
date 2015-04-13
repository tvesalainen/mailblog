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
import java.util.List;
import org.vesalainen.gpx.TrackHandler;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.types.TimeSpan;
import org.w3c.dom.Element;

/**
 * @author Timo Vesalainen
 */
public class OpenCPNTrackHandler implements TrackHandler
{
    private final DS ds;
    private Key trackKey;
    private Key trackSeqKey;
    private final List<Entity> trackPoints = new ArrayList<>();
    private final BoundingBox trackBbox = new BoundingBox();
    private final TimeSpan trackSpan = new TimeSpan();
    private final BoundingBox trackSeqBbox = new BoundingBox();
    private final TimeSpan trackSeqSpan = new TimeSpan();
    private Entity track;
    private Entity trackSeq;

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
            track = new Entity(trackKey);
            if (name != null)
            {
                track.setProperty(NameProperty, name);
            }
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
        trackBbox.populate(track);
        trackBbox.clear();
        trackSpan.populate(track);
        trackSpan.clear();
        trackKey = ds.put(track);
        trackKey = null;
    }

    @Override
    public void startTrackSeq()
    {
        trackSeq = new Entity(TrackSeqKind, trackKey);
        trackSeqKey = ds.put(trackSeq);
    }

    @Override
    public void endTrackSeq()
    {
        trackBbox.add(trackSeqBbox);
        trackSpan.add(trackSeqSpan);
        trackSeqBbox.populate(trackSeq);
        trackSeqBbox.clear();
        trackSeqSpan.populate(trackSeq);
        trackSeqSpan.clear();
        ds.put(trackSeq);
        ds.put(trackPoints);
        trackPoints.clear();
        trackSeq = null;
    }

    @Override
    public void trackPoint(double latitude, double longitude, long time)
    {
        trackSeqBbox.add(latitude, longitude);
        trackSeqSpan.add(time);
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
