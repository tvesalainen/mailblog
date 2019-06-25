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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.vesalainen.mailblog.BlogConstants.*;
import static org.vesalainen.mailblog.SpotType.Destination;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoData implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final GeoPtRangeMap<Key> boxList = new GeoPtRangeMap<>();
    private final Map<Key,GeoPt[]> placemarkList = new HashMap<>();
    private final GeoPtLocationMap<Key> locationList = new GeoPtLocationMap<>();

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
        Iterator<Entity> iterator = merge.iterator();
        PathBuilder builder = new PathBuilder(iterator);
        builder.build();
        for (Entity metadata : imageLocationsIterable)
        {
            GeoPt location = (GeoPt) metadata.getProperty(LocationProperty);
            if (location != null)
            {
                locationList.put(location, metadata.getKey());
            }
        }
        for (Entity blog : blogLocationsIterable)
        {
            GeoPt location = (GeoPt) blog.getProperty(LocationProperty);
            if (location != null)
            {
                locationList.put(location, blog.getKey());
            }
        }
    }
    
    public void writeRegionKeys(DS.CacheWriter cw, GeoPtBoundingBox bb)
    {
        System.err.println("writeRegionKeys "+bb);
        JSONObject json = regionKeys(bb);
        json.write(cw);
        cw.cache();
    }
    JSONObject regionKeys(GeoPtBoundingBox bb)
    {
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        json.put("keys", jarray);
        Stream<Key> overlapping = boxList.overlapping(bb);
        overlapping.forEach((k)->
        {
            jarray.put(KeyFactory.keyToString(k));
        });
        Stream<Key> keys = locationList.strickEntries(bb).map((e)->e.getValue());
        keys.forEach((k)->
        {
            jarray.put(KeyFactory.keyToString(k));
        });
        return json;
    }
    public List<GeoPt> getPlacemarkPoints(Key key)
    {
        return CollectionHelp.create(placemarkList.get(key));
    }
    private class PathBuilder
    {
        Iterator<Entity> iterator;
        List<GeoPt> locList = new ArrayList<>();
        Entity prevEntity;
        Entity curEntity;

        public PathBuilder(Iterator<Entity> iterator)
        {
            this.iterator = iterator;
        }
        private void build()
        {
            String state = "start";
            while (true)
            {
                switch (state)
                {
                    case "start":
                        state = next();
                        switch (state)
                        {
                            case PlacemarkKind:
                                startingPlacemark(curEntity);
                                break;
                            case TrackSeqKind:
                                startingTrackSeq(curEntity);
                                break;
                            case "end":
                                break;
                        }
                        break;
                    case PlacemarkKind:
                        state = next();
                        switch (state)
                        {
                            case PlacemarkKind:
                                placemarkToPlacemark(prevEntity, curEntity);
                                break;
                            case TrackSeqKind:
                                placemarkToTrackSeq(prevEntity, curEntity);
                                break;
                            case "end":
                                endingPlacemark(prevEntity);
                                break;
                        }
                        break;
                    case TrackSeqKind:
                        Date end = (Date) curEntity.getProperty(EndProperty);
                        state = next(end);
                        switch (state)
                        {
                            case PlacemarkKind:
                                trackSeqToPlacemark(prevEntity, curEntity);
                                break;
                            case TrackSeqKind:
                                trackSeqToTrackSeq(prevEntity, curEntity);
                                break;
                            case "end":
                                endingTrackSeq(prevEntity);
                                break;
                        }
                        break;
                    case "end":
                        return;
                }
            }
        }
        private String next()
        {
            return next(null);
        }
        private String next(Date end)
        {
            prevEntity = curEntity;
            while (iterator.hasNext())
            {
                curEntity = iterator.next();
                if (notOverlappingPlacemark(end) && notDestination())
                {
                    return curEntity.getKind();
                }
            }
            return "end";
        }

        private boolean notOverlappingPlacemark(Date end)
        {
            if (end != null)
            {
                if (PlacemarkKind.equals(curEntity.getKind()))
                {
                    Date timestamp = (Date) curEntity.getProperty(TimestampProperty);
                    return end.before(timestamp);
                }
            }
            return true;
        }
        private boolean notDestination()
        {
            if (PlacemarkKind.equals(curEntity.getKind()))
            {
                String description = (String) curEntity.getProperty(DescriptionProperty);
                SpotType st = SpotType.getSpotType(description);
                return SpotType.Destination != st;
            }
            return true;
        }

        private void startingPlacemark(Entity placemark)
        {
        }

        private void startingTrackSeq(Entity trackSeq)
        {
            addTrackSeq(trackSeq);
        }

        private void placemarkToPlacemark(Entity prevPlacemark, Entity curPlacemark)
        {
            GeoPt curLoc = (GeoPt) curPlacemark.getProperty(LocationProperty);
            addPlacemark(prevPlacemark, curLoc);
        }

        private void placemarkToTrackSeq(Entity placemark, Entity trackSeq)
        {
            GeoPt firstLoc = (GeoPt) trackSeq.getProperty(FirstProperty);
            addPlacemark(placemark, firstLoc);
        }

        private void trackSeqToPlacemark(Entity trackSeq, Entity placemark)
        {
            addTrackSeq(trackSeq);
            GeoPt lastLoc = (GeoPt) trackSeq.getProperty(LastProperty);
            locList.add(lastLoc);
        }

        private void trackSeqToTrackSeq(Entity prevTrackSeq, Entity curTrackSeq)
        {
            addTrackSeq(prevTrackSeq);
        }

        private void endingTrackSeq(Entity trackSeq)
        {
            addTrackSeq(trackSeq);
        }

        private void endingPlacemark(Entity placemark)
        {
            addPlacemark(placemark, null);
        }

        private void addTrackSeq(Entity trackSeq)
        {
            GeoPtBoundingBox bb = GeoPtBoundingBox.getInstance(trackSeq);
            boxList.put(bb, trackSeq.getKey());
        }

        private void addPlacemark(Entity placemark, GeoPt nextLoc)
        {
            GeoPt loc = (GeoPt) placemark.getProperty(LocationProperty);
            Key key = placemark.getKey();
            locList.add(loc);
            if (nextLoc != null)
            {
                locList.add(nextLoc);
            }
            placemarkList.put(key, locList.toArray(new GeoPt[locList.size()]));
            GeoPtBoundingBox bb = new GeoPtBoundingBox();
            bb.add(locList);
            boxList.put(bb, key);
            locList.clear();
        }

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
