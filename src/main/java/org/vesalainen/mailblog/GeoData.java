/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.Pair;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoData implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final Map<BoundingBox, List<Pair<BoundingBox,Key>>> boxList;
    private final List<Pair<GeoPt,Key>> locationList;
    private Key firstPlacemarkKey;
    private final BoundingBox placemarkBoundingBox;

    public GeoData(DS ds)
    {
        Iterable<Entity> tracksIterable = ds.fetchTracks();
        Iterable<Entity> imageLocationsIterable = ds.fetchImageLocations();
        Iterable<Entity> blogLocationsIterable = ds.fetchBlogLocations();
        
        boxList = new HashMap<>();  // TODO fix after java 8
        Date lastBegin = null;
        for (Entity track : tracksIterable)
        {
            BoundingBox bbTrack = new BoundingBox(track);
            for (Entity trackSeq : ds.fetchTrackSeqs(track.getKey()))
            {
                BoundingBox bbTrackSeq = new BoundingBox(trackSeq);
                List<Pair<BoundingBox, Key>> list = boxList.get(bbTrack);
                if (list == null)
                {
                    list = new ArrayList<>();
                    boxList.put(bbTrack, list);
                }
                list.add(new Pair<BoundingBox,Key>(bbTrackSeq, trackSeq.getKey()));
                Date begin = (Date) trackSeq.getProperty(BeginProperty);
                if (lastBegin == null || lastBegin.before(begin))
                {
                    lastBegin = begin;
                }
            }
        }
        Iterable<Entity> placemarksIterable;
        if (lastBegin != null)
        {
            placemarksIterable = ds.fetchPlacemarks(lastBegin);
        }
        else
        {
            placemarksIterable = ds.fetchPlacemarks();
        }
        firstPlacemarkKey = null;
        placemarkBoundingBox = new BoundingBox();
        for (Entity placemark : placemarksIterable)
        {
            if (firstPlacemarkKey == null)
            {
                firstPlacemarkKey = placemark.getKey();
            }
            GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
            placemarkBoundingBox.add(location);
        }
        locationList = new ArrayList<>();
        for (Entity metadata : imageLocationsIterable)
        {
            GeoPt location = (GeoPt) metadata.getProperty(LocationProperty);
            if (location != null)
            {
                locationList.add(new Pair<>(location, metadata.getKey()));
            }
        }
        for (Entity blog : blogLocationsIterable)
        {
            GeoPt location = (GeoPt) blog.getProperty(LocationProperty);
            if (location != null)
            {
                locationList.add(new Pair<>(location, blog.getKey()));
            }
        }
    }
    
    public void writeRegionKeys(DS.CacheWriter cw, BoundingBox bb)
    {
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        json.put("keys", jarray);
        for (Map.Entry<BoundingBox,List<Pair<BoundingBox, Key>>> entry : boxList.entrySet())
        {
            if (bb.isIntersecting(entry.getKey()))
            {
                for (Pair<BoundingBox, Key> pair : entry.getValue())
                {
                    if (bb.isIntersecting(pair.getFirst()))
                    {
                        jarray.put(KeyFactory.keyToString(pair.getSecond()));
                    }
                }
            }
        }
        for (Pair<GeoPt,Key> pair : locationList)
        {
            if (bb.isInside(pair.getFirst()))
            {
                jarray.put(KeyFactory.keyToString(pair.getSecond()));
            }
        }
        if (firstPlacemarkKey != null && bb.isIntersecting(placemarkBoundingBox))
        {
            jarray.put(KeyFactory.keyToString(firstPlacemarkKey));
        }
        json.write(cw);
        cw.cache();
    }

    public BoundingBox getPlacemarkBoundingBox()
    {
        return placemarkBoundingBox;
    }

    private class SerializableHashMapList<K,V> extends HashMapList<K,V> implements Serializable
    {
        private static final long serialVersionUID = 1L;

    }
}
