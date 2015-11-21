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
public class BaseTrackHandler
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

    public BaseTrackHandler(DS ds)
    {
        this.ds = ds;
    }
    
    public boolean startTrack(String name, String guid)
    {
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

    public void endTrack()
    {
        trackBbox.populate(track);
        trackBbox.clear();
        trackSpan.populate(track);
        trackSpan.clear();
        trackKey = ds.put(track);
        trackKey = null;
    }

    public void startTrackSeq()
    {
        trackSeq = new Entity(TrackSeqKind, trackKey);
        trackSeqKey = ds.put(trackSeq);
    }

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

    public void trackPoint(float latitude, float longitude, long time)
    {
        trackSeqBbox.add(latitude, longitude);
        trackSeqSpan.add(time);
        Entity point = new Entity(TrackPointKind, time, trackSeqKey);
        point.setProperty(LocationProperty, new GeoPt(latitude, longitude));
        trackPoints.add(point);
    }
}
