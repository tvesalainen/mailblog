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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.Instant;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS.CacheWriter;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DSTest extends DSHelper
{
    public DSTest()
    {
    }

    @Test
    public void testGMap()
    {
        DS ds = DS.get();
        Key settingsKey = ds.createSettingsKey();
        Entity settings = new Entity(settingsKey);
        settings.setProperty(TrackColorProperty, Long.valueOf(255));
        settings.setProperty(WaypointIconProperty, new Link("waypoing.png"));
        settings.setProperty(DestinationIconProperty, new Link("destination.png"));
        settings.setProperty(AnchoredIconProperty, new Link("anchored.png"));
        ds.put(settings);
        
        ds.addPlacemark(Date.from(Instant.parse("2019-06-20T10:15:30.00Z")), new GeoPt(-16.22F, -145.55F), "Pakokota", "Check-in/OK");
        ds.addPlacemark(Date.from(Instant.parse("2019-05-26T10:15:30.00Z")), new GeoPt(-16.06F, -145.62F), "Rotoava", "Check-in/OK");
        ds.addPlacemark(Date.from(Instant.parse("2019-05-25T10:15:30.00Z")), new GeoPt(-14.93F, -144.7F), "Nuku Hiva - Fakarava 3", "Custom");
        ds.addPlacemark(Date.from(Instant.parse("2019-05-24T10:15:30.00Z")), new GeoPt(-13.9F, -143.9F), "Nuku Hiva - Fakarava 2", "Custom");
        ds.addPlacemark(Date.from(Instant.parse("2019-05-22T10:15:30.00Z")), new GeoPt(-11.95F, -141.8F), "Nuku Hiva - Fakarava 1", "Custom");
        ds.addPlacemark(Date.from(Instant.parse("2018-11-15T10:15:30.00Z")), new GeoPt(-8.9F, -140.1F), "Nuku Hiva", "Check-in/OK");
        
        Entity trackSeq = createTrackSeq(Date.from(Instant.parse("2019-06-19T08:15:30.00Z")), Date.from(Instant.parse("2019-06-19T10:15:30.00Z")), -16.05F, -145.6F, -16.22F, -145.55F);
        ds.put(trackSeq);
        
        
        JSONObject json = ds.mapInit(300, 241);
        assertEquals(-16.219999313354492, json.getDouble("latitude"), 1e-10);
        assertEquals(-145.5500030517578, json.getDouble("longitude"), 1e-10);
        
        GeoPtBoundingBox bb = new GeoPtBoundingBox("-16.237194,-145.567653,-16.21247,-145.547002");   // -16.237194,-145.567653,-16.21247,-145.547002
        System.err.println(bb);
        GeoData gd = new GeoData(ds);
        JSONObject regionKeys = gd.regionKeys(bb);
        JSONArray array = regionKeys.getJSONArray("keys");
        array.forEach((Object k)->
        {
            String strKey = (String) k;
            Key key = KeyFactory.stringToKey(strKey);
            GeoJSON feature = ds.getFeature(key);
            try
            {
                Entity entity = ds.get(key);
                System.err.println(entity);
                System.err.println(feature);
            }
            catch (EntityNotFoundException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        });
    }
    //@Test
    public void testGMap2()
    {
        Instant now = Instant.now();
        DS ds = DS.get();
        Key settingsKey = ds.createSettingsKey();
        Entity settings = new Entity(settingsKey);
        settings.setProperty(TrackColorProperty, Long.valueOf(255));
        settings.setProperty(WaypointIconProperty, new Link("waypoing.png"));
        settings.setProperty(DestinationIconProperty, new Link("destination.png"));
        settings.setProperty(AnchoredIconProperty, new Link("anchored.png"));
        ds.put(settings);
        
        ds.addPlacemark(Date.from(now.plus(1, DAYS)), new GeoPt(-0, 0), "p1", "Check-in/OK");
        
        Entity trackSeq = createTrackSeq(Date.from(now.plus(2, DAYS)), Date.from(now.plus(3, DAYS)), 2, 3, 4, 6);
        ds.put(trackSeq);
        
        ds.addPlacemark(Date.from(now.plus(4, DAYS)), new GeoPt(5, 8), "p2", "Custom");
        ds.addPlacemark(Date.from(now.plus(56, DAYS)), new GeoPt(7, 12), "px", "Custom");
        
        trackSeq = createTrackSeq(Date.from(now.plus(5, DAYS)), Date.from(now.plus(7, DAYS)), 6, 10, 8, 13);
        ds.put(trackSeq);
        
        ds.addPlacemark(Date.from(now.plus(8, DAYS)), new GeoPt(10, 15), "p4", "Custom");
        ds.addPlacemark(Date.from(now.plus(6, DAYS)), new GeoPt(12, 17), "p4", "Destination");
        
        JSONObject json = ds.mapInit(400, 500);
        assertEquals(6, json.getDouble("latitude"), 1e-10);
        assertEquals(8.5, json.getDouble("longitude"), 1e-10);
        
        GeoPtBoundingBox bb = new GeoPtBoundingBox(json);
        System.err.println(bb);
        GeoData gd = new GeoData(ds);
        JSONObject regionKeys = gd.regionKeys(bb);
        JSONArray array = regionKeys.getJSONArray("keys");
        array.forEach((Object k)->
        {
            String strKey = (String) k;
            Key key = KeyFactory.stringToKey(strKey);
            GeoJSON feature = ds.getFeature(key);
            try
            {
                Entity entity = ds.get(key);
                System.err.println(entity);
                System.err.println(feature);
            }
            catch (EntityNotFoundException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        });
    }
    private Entity createTrackSeq(Date begin, Date end, float lat1, float lon1, float lat2, float lon2)
    {
        GeoPt first = new GeoPt(lat1, lon1);
        GeoPt last = new GeoPt(lat2, lon2);
        GeoPtBoundingBox bb = new GeoPtBoundingBox(first, last);
        Entity trackSeq = new Entity(TrackSeqKind, DS.getRootKey());
        trackSeq.setIndexedProperty(BeginProperty, begin);
        trackSeq.setProperty(EndProperty, end);
        trackSeq.setProperty(FirstProperty, first);
        trackSeq.setProperty(LastProperty, last);
        trackSeq.setProperty(SouthWestProperty, bb.getSouthWest());
        trackSeq.setProperty(NorthEastProperty, bb.getNorthEast());
        return trackSeq;
    }
    @Test
    public void testDistance()
    {
        GeoPt l1 = new GeoPt(60, 25);
        GeoPt l2 = new GeoPt(59, 25);
        GeoPt l3 = new GeoPt(60, 24);
        assertEquals(1, DS.getDegreesDistance(l1, l2), Epsilon);
        assertEquals(0.5, DS.getDegreesDistance(l1, l3), Epsilon);
    }
    @Test
    public void testCenter()
    {
        System.out.println("center");
        GeoPt[] location = {new GeoPt(-2,2),new GeoPt(2,2),new GeoPt(2,-2),new GeoPt(-2,-2)};
        GeoPt expResult = new GeoPt(0, -0);
        GeoPt result = DS.center(CollectionHelp.create(location));
        assertEquals(expResult, result);
    }
    @Test
    public void testCenter2()
    {
        System.out.println("center2");
        GeoPt[] location = {new GeoPt(-10,175),new GeoPt(-8,-175)};
        GeoPt expResult = new GeoPt(-9,180);
        GeoPt result = DS.center(CollectionHelp.create(location));
        assertEquals(expResult, result);
    }
    @Test
    public void testCenter3()
    {
        System.out.println("center3");
        GeoPt[] location = {new GeoPt(-10,-160),new GeoPt(-8,-170)};
        GeoPt expResult = new GeoPt(-9,-165);
        GeoPt result = DS.center(CollectionHelp.create(location));
        assertEquals(expResult, result);
    }
    @Test
    public void testCenter4()
    {
        System.out.println("center3");
        GeoPt[] location = {new GeoPt(-10,160),new GeoPt(-8,170)};
        GeoPt expResult = new GeoPt(-9,165);
        GeoPt result = DS.center(CollectionHelp.create(location));
        assertEquals(expResult, result);
    }
    
}
