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
import java.io.Writer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author tkv
 */
public class GeoJSON
{
    protected final JSONObject json = new JSONObject();

    protected GeoJSON(String type)
    {
        json.put("type", type);
    }

    public JSONObject getJson()
    {
        return json;
    }

    public void write(Writer writer)
    {
        json.write(writer);
    }
    
    public static class FeatureCollection extends GeoJSON
    {
        JSONArray features = new JSONArray();
        public FeatureCollection()
        {
            super("FeatureCollection");
            json.put("features", features);
        }
        public void addPolygon(GeoPt... points)
        {
            addGeometry(new Polygon(points));
        }
        public void addLineString(GeoPt... points)
        {
            addGeometry(new LineString(points));
        }
        public void addPoint(GeoPt point)
        {
            addGeometry(new Point(point));
        }
        public void addGeometry(Geometry geometry)
        {
            addFeature(new Feature(geometry));
        }
        public void addFeature(Feature feature)
        {
            features.put(feature.json);
        }
    }
    public static class Feature extends GeoJSON
    {
        public Feature(Geometry geometry)
        {
            super("Feature");
            json.put("geometry", geometry.json);
            json.put("properties", JSONObject.NULL);
        }
    }
    protected static void addGroup(JSONArray coordinates, GeoPt... locations)
    {
        JSONArray group = new JSONArray();
        coordinates.put(group);
        addSingles(coordinates, locations);
    }
    protected static void addSingles(JSONArray coordinates, GeoPt... locations)
    {
        for (GeoPt pt : locations)
        {
            addSingle(coordinates, pt);
        }
    }
    protected static void addSingle(JSONArray coordinates, GeoPt location)
    {
        JSONArray point = new JSONArray();
        coordinates.put(point);
        point.put(location.getLongitude());
        point.put(location.getLatitude());
    }
    protected static class Geometry extends GeoJSON
    {
        protected final JSONArray coordinates = new JSONArray();

        protected Geometry(String type)
        {
            super(type);
            json.put("coordinates", coordinates);
        }
        
    }
    public static class Point extends Geometry
    {
        public Point(GeoPt location)
        {
            super("Point");
            coordinates.put(location.getLongitude());
            coordinates.put(location.getLatitude());
        }
    }
    public static class LineString extends Geometry
    {
        public LineString(GeoPt... locations)
        {
            super("LineString");
            addSingles(coordinates, locations);
        }
        public void add(GeoPt... locations)
        {
            addSingles(coordinates, locations);
        }
    }
    public static class Polygon extends Geometry
    {
        public Polygon(GeoPt... locations)
        {
            super("Polygon");
            addGroup(coordinates, locations);
        }
    }
}
