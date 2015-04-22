/*
 * Copyright (C) 2015 tkv
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

import com.google.appengine.api.datastore.GeoPt;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.mailblog.GeoJSON.Feature;
import org.vesalainen.mailblog.GeoJSON.FeatureCollection;
import org.vesalainen.mailblog.GeoJSON.GeometryCollection;
import org.vesalainen.mailblog.GeoJSON.LineString;
import org.vesalainen.mailblog.GeoJSON.MultiLineString;
import org.vesalainen.mailblog.GeoJSON.MultiPoint;
import org.vesalainen.mailblog.GeoJSON.Point;
import org.vesalainen.mailblog.GeoJSON.Polygon;

/**
 *
 * @author tkv
 */
public class GeoJSONTest
{
    static final double Epsilon = 1e-8;
    GeoPt p1 = new GeoPt(1F, 2F);
    GeoPt p2 = new GeoPt(3F, 4F);
    GeoPt p3 = new GeoPt(5F, 6F);
    GeoPt p4 = new GeoPt(7F, 8F);
    GeoPt p5 = new GeoPt(9F, 10F);
    GeoPt p6 = new GeoPt(11F, 12F);
    List<GeoPt> list1 = new ArrayList<>();
    List<GeoPt> list2 = new ArrayList<>();
    public GeoJSONTest()
    {
        list1.add(p1);
        list1.add(p2);
        list1.add(p3);
        list1.add(p4);
        
        list2.add(p4);
        list2.add(p5);
        list2.add(p6);
    }

    @Test
    public void test0()
    {
        StringWriter sw = new StringWriter();
        FeatureCollection fc = new FeatureCollection();
        assertEquals("FeatureCollection", fc.getJson().get("type"));
        fc.addPoint(p1);
        fc.write(sw);
    }
    
    @Test
    public void testPoint()
    {
        Point o = new Point(p1);
        JSONObject json = o.getJson();
        assertEquals("Point", json.get("type"));
        JSONArray ar = json.getJSONArray("coordinates");
        assertNotNull(ar);
        assertEquals(2, ar.length());
        assertEquals(p1.getLongitude(), ar.getDouble(0), Epsilon);
        assertEquals(p1.getLatitude(), ar.getDouble(1), Epsilon);
    }
    @Test
    public void testLineString()
    {
        LineString o = new LineString(list1);
        JSONObject json = o.getJson();
        assertEquals("LineString", json.get("type"));
        JSONArray ar = json.getJSONArray("coordinates");
        assertNotNull(ar);
        assertEquals(4, ar.length());
        JSONArray ar2 = ar.getJSONArray(0);
        assertNotNull(ar2);
        assertEquals(2, ar2.length());
        assertEquals(p1.getLongitude(), ar2.getDouble(0), Epsilon);
        assertEquals(p1.getLatitude(), ar2.getDouble(1), Epsilon);
    }
    @Test
    public void testPolygon()
    {
        Polygon o = new Polygon(list1);
        JSONObject json = o.getJson();
        assertEquals("Polygon", json.get("type"));
        JSONArray ar = json.getJSONArray("coordinates");
        assertNotNull(ar);
        assertEquals(1, ar.length());
        JSONArray ar2 = ar.getJSONArray(0);
        assertNotNull(ar2);
        assertEquals(4, ar2.length());
        JSONArray ar3 = ar2.getJSONArray(0);
        assertNotNull(ar3);
        assertEquals(2, ar3.length());
        assertEquals(p1.getLongitude(), ar3.getDouble(0), Epsilon);
        assertEquals(p1.getLatitude(), ar3.getDouble(1), Epsilon);
    }
    @Test
    public void testMultiPoint()
    {
        MultiPoint o = new MultiPoint(list1);
        JSONObject json = o.getJson();
        assertEquals("MultiPoint", json.get("type"));
        JSONArray ar = json.getJSONArray("coordinates");
        assertNotNull(ar);
        assertEquals(4, ar.length());
        JSONArray ar2 = ar.getJSONArray(0);
        assertNotNull(ar2);
        assertEquals(2, ar2.length());
        assertEquals(p1.getLongitude(), ar2.getDouble(0), Epsilon);
        assertEquals(p1.getLatitude(), ar2.getDouble(1), Epsilon);
    }
    @Test
    public void testMultiLineString()
    {
        MultiLineString o = new MultiLineString(list1);
        JSONObject json = o.getJson();
        assertEquals("MultiLineString", json.get("type"));
        JSONArray ar1 = json.getJSONArray("coordinates");
        assertNotNull(ar1);
        assertEquals(1, ar1.length());
        JSONArray ar2 = ar1.getJSONArray(0);
        assertNotNull(ar2);
        assertEquals(4, ar2.length());
        JSONArray ar3 = ar2.getJSONArray(0);
        assertNotNull(ar3);
        assertEquals(2, ar3.length());
        assertEquals(p1.getLongitude(), ar3.getDouble(0), Epsilon);
        assertEquals(p1.getLatitude(), ar3.getDouble(1), Epsilon);
    }
    @Test
    public void testGeometryCollection()
    {
        GeometryCollection o = new GeometryCollection();
        JSONObject json = o.getJson();
        assertEquals("GeometryCollection", json.get("type"));
        MultiLineString mls = o.addMultiLineString(list1);
        mls.add(list2);
        JSONArray ar1 = json.getJSONArray("geometries");
        assertNotNull(ar1);
        assertEquals(1, ar1.length());
        JSONObject jo = ar1.getJSONObject(0);
        assertNotNull(jo);
        assertEquals("MultiLineString", jo.get("type"));
        JSONArray joar1 = jo.getJSONArray("coordinates");
        assertNotNull(joar1);
        assertEquals(2, joar1.length());
        BoundingBox box = new BoundingBox();
        box.add(list1);
        box.add(list2);
        o.setBbox(box);
        StringWriter sw = new StringWriter();
        o.write(sw);
        JSONArray ba = json.getJSONArray("bbox");
        assertNotNull(ba);
        assertEquals(4, ba.length());
        assertEquals(2, ba.getDouble(0), Epsilon);
        assertEquals(1, ba.getDouble(1), Epsilon);
        assertEquals(12, ba.getDouble(2), Epsilon);
        assertEquals(11, ba.getDouble(3), Epsilon);
    }
    @Test
    public void testFeatureCollection()
    {
        FeatureCollection o = new FeatureCollection();
        JSONObject json = o.getJson();
        assertEquals("FeatureCollection", json.get("type"));
        MultiLineString mls = new MultiLineString(list1);
        mls.add(list2);
        Feature f = o.addGeometry(mls);
        assertNotNull(f);
        f.setId("1234");
        assertEquals("1234", f.getId());
        f.setProperty("color", "red");
        f.setProperty("opaque", 0.6);
        assertEquals("red", f.getProperty("color"));
        assertEquals(0.6, f.getProperty("opaque"));
        JSONArray ar1 = json.getJSONArray("features");
        assertNotNull(ar1);
        assertEquals(1, ar1.length());
        JSONObject jo = ar1.getJSONObject(0);
        assertNotNull(jo);
        assertEquals("Feature", jo.get("type"));
        JSONObject job = jo.getJSONObject("geometry");
        assertNotNull(job);
        assertEquals("MultiLineString", job.get("type"));
        BoundingBox box = new BoundingBox();
        box.add(list1);
        box.add(list2);
        o.setBbox(box);
        StringWriter sw = new StringWriter();
        o.write(sw);
        JSONArray ba = json.getJSONArray("bbox");
        assertNotNull(ba);
        assertEquals(4, ba.length());
        assertEquals(2, ba.getDouble(0), Epsilon);
        assertEquals(1, ba.getDouble(1), Epsilon);
        assertEquals(12, ba.getDouble(2), Epsilon);
        assertEquals(11, ba.getDouble(3), Epsilon);
    }
}
