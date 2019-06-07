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
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
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
        ds.addPlacemark(new Date(119, 5, 1), new GeoPt(-8, -140), "heippa", "Check-in/OK");
        ds.addPlacemark(new Date(119, 5, 2), new GeoPt(-9, -141), "heippa", "Custom");
        ds.addPlacemark(new Date(119, 5, 3), new GeoPt(-10, -142), "heippa", "Destination");
        
        JSONObject json = ds.mapInit(400, 500);
        assertEquals(-9, json.getDouble("latitude"), 1e-10);
        assertEquals(-141, json.getDouble("longitude"), 1e-10);
        
        GeoPtBoundingBox bb = new GeoPtBoundingBox(json);
        GeoData gd = new GeoData(ds);
        JSONObject regionKeys = gd.regionKeys(bb);
        JSONArray array = regionKeys.getJSONArray("keys");
        array.forEach((Object k)->
        {
            String strKey = (String) k;
            Key key = KeyFactory.stringToKey(strKey);
            GeoJSON feature = ds.getFeature(key);
            System.err.println(feature);
        });
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
