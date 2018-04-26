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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoData implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final MapList<BoundingBox, Key> boxList = new HashMapList<>();
    private final Map<Key,List<GeoPt>> placemarkList = new HashMap<>();
    private final MapList<GeoPt,Key> locationList = new HashMapList<>();

    public GeoData()
    {
        this(DS.get());
    }

    public GeoData(DS ds)
    {
        System.err.println("init GeoData");
        Iterable<Entity> trackSeqIterable = ds.fetchTrackSeqs();
        Iterable<Entity> placemarksIterable = ds.fetchPlacemarks();
        Iterable<Entity> imageLocationsIterable = ds.fetchImageLocations();
        Iterable<Entity> blogLocationsIterable = ds.fetchBlogLocations();

        Iterable<Entity> merge = Merger.merge(new Comp(), trackSeqIterable, placemarksIterable);
        BoundingBox bb;
        BoundingBox pb = null;
        Entity prev = null;
        Date lastTrackEnd = null;
        List<GeoPt> locList = null;
        List<GeoPt> prevList = null;
        for (Entity entity : merge)
        {
            bb = BoundingBox.getInstance(entity);
            Key key = entity.getKey();
            switch (entity.getKind())
            {
                case PlacemarkKind:
                    GeoPt location = (GeoPt) entity.getProperty(LocationProperty);
                    Date timestamp = (Date) entity.getProperty(TimestampProperty);
                    if (prev == null)
                    {
                        boxList.add(bb, key);
                        locList = new ArrayList<>();
                        locList.add(location);
                        placemarkList.put(key, locList);
                    }
                    else
                    {
                        locList = new ArrayList<>();
                        if (lastTrackEnd == null || timestamp.after(lastTrackEnd))
                        {
                            switch (prev.getKind())
                            {
                                case PlacemarkKind:
                                    prevList.add(location);  // prev
                                    pb.add(location);       // prev
                                    boxList.add(bb, key);
                                    locList.add(location);
                                    placemarkList.put(key, locList);
                                    break;
                                case TrackSeqKind:
                                    GeoPt last = (GeoPt) prev.getProperty(LastProperty);
                                    boxList.add(bb, key);
                                    locList.add(last);
                                    locList.add(location);
                                    placemarkList.put(key, locList);
                                    break;
                                default:
                                    throw new UnsupportedOperationException(entity.getKind()+" not supported");
                            }
                        }
                    }
                    break;
                case TrackSeqKind:
                    if (prev == null)
                    {
                        boxList.add(bb, key);
                    }
                    else
                    {
                        GeoPt first = (GeoPt) entity.getProperty(FirstProperty);
                        lastTrackEnd = (Date) entity.getProperty(EndProperty);
                        switch (prev.getKind())
                        {
                            case PlacemarkKind:
                                prevList.add(first);  // prev
                                pb.add(first);       // prev
                                boxList.add(bb, key);
                                break;
                            case TrackSeqKind:
                                boxList.add(bb, key);
                                break;
                            default:
                                throw new UnsupportedOperationException(entity.getKind()+" not supported");
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(entity.getKind()+" not supported");
            }
            prev = entity;
            pb = bb;
            prevList = locList;
        }
        for (Entity metadata : imageLocationsIterable)
        {
            GeoPt location = (GeoPt) metadata.getProperty(LocationProperty);
            if (location != null)
            {
                locationList.add(location, metadata.getKey());
            }
        }
        for (Entity blog : blogLocationsIterable)
        {
            GeoPt location = (GeoPt) blog.getProperty(LocationProperty);
            if (location != null)
            {
                locationList.add(location, blog.getKey());
            }
        }
    }
    
    public void writeRegionKeys(DS.CacheWriter cw, BoundingBox bb)
    {
        System.err.println("writeRegionKeys "+bb);
        JSONObject json = regionKeys(bb);
        json.write(cw);
        cw.cache();
    }
    JSONObject regionKeys(BoundingBox bb)
    {
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        json.put("keys", jarray);
        boxList.forEach((b, l)->
        {
            if (bb.isIntersecting(b))
            {
                for (Key k : l)
                {
                    jarray.put(KeyFactory.keyToString(k));
                }
            }
        });
        locationList.forEach((g, l)->
        {
            if (bb.isInside(g))
            {
                for (Key k : l)
                {
                    jarray.put(KeyFactory.keyToString(k));
                }
            }
        });
        return json;
    }
    public Collection<GeoPt> getPlacemarkPoints(Key key)
    {
        return placemarkList.get(key);
    }
    private static class Comp implements Comparator<Entity>
    {

        @Override
        public int compare(Entity o1, Entity o2)
        {
            return date(o1).compareTo(date(o2));
        }
        private Date date(Entity entity)
        {
            switch (entity.getKind())
            {
                case PlacemarkKind:
                    return (Date) entity.getProperty(TimestampProperty);
                case TrackSeqKind:
                    return (Date) entity.getProperty(BeginProperty);
                default:
                    throw new UnsupportedOperationException(entity.getKind()+" not supported");
            }
        }
    }
}
