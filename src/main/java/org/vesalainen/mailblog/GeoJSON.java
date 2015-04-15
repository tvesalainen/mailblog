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
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author tkv
 */
public class GeoJSON
{
    protected final JSONObject json = new JSONObject();
    protected BoundingBox bbox = new BoundingBox();
    
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
        if (bbox != null)
        {
            JSONArray ba = new JSONArray();
            ba.put(bbox.getWest());
            ba.put(bbox.getSouth());
            ba.put(bbox.getEast());
            ba.put(bbox.getNorth());
            json.put("bbox", ba);
        }
        json.write(writer);
    }
    
    public static class GeometryCollection extends GeoJSON
    {
        JSONArray geometries = new JSONArray();
        public GeometryCollection()
        {
            super("GeometryCollection");
            json.put("geometries", geometries);
        }
        /**
         * Sets a bounding box. JSON object is created just before writing. 
         * Therefore it is possible to update this bounding box after setting.
         * @param bbox 
         */
        public void setBbox(BoundingBox bbox)
        {
            this.bbox = bbox;
        }
        public MultiLineString addMultiLineString(Collection<GeoPt> points)
        {
            return (MultiLineString) addGeometry(new MultiLineString(points));
        }
        public MultiPoint addMultiPoint(Collection<GeoPt> points)
        {
            return (MultiPoint) addGeometry(new MultiPoint(points));
        }
        public Polygon addPolygon(Collection<GeoPt> points)
        {
            return (Polygon) addGeometry(new Polygon(points));
        }
        public LineString addLineString(Collection<GeoPt> points)
        {
            return (LineString) addGeometry(new LineString(points));
        }
        public Point addPoint(GeoPt point)
        {
            return (Point) addGeometry(new Point(point));
        }
        public Geometry addGeometry(Geometry geometry)
        {
            geometries.put(geometry.json);
            return geometry;
        }
    }
    public static class FeatureCollection extends GeoJSON
    {
        JSONArray features = new JSONArray();
        public FeatureCollection()
        {
            super("FeatureCollection");
            json.put("features", features);
        }
        /**
         * Sets a bounding box. JSON object is created just before writing. 
         * Therefore it is possible to update this bounding box after setting.
         * @param bbox 
         */
        public void setBbox(BoundingBox bbox)
        {
            this.bbox = bbox;
        }
        public Feature addMultiLineString(Collection<GeoPt> points)
        {
            return (Feature) addGeometry(new MultiLineString(points));
        }
        public Feature addMultiPoint(Collection<GeoPt> points)
        {
            return (Feature) addGeometry(new MultiPoint(points));
        }
        public Feature addPolygon(Collection<GeoPt> points)
        {
            return (Feature) addGeometry(new Polygon(points));
        }
        public Feature addLineString(Collection<GeoPt> points)
        {
            return (Feature) addGeometry(new LineString(points));
        }
        public Feature addPoint(GeoPt point)
        {
            return (Feature) addGeometry(new Point(point));
        }
        public Feature addGeometry(Geometry geometry)
        {
            return addFeature(new Feature(geometry));
        }
        protected Feature addFeature(Feature feature)
        {
            features.put(feature.json);
            return feature;
        }
    }
    public static class Feature extends GeoJSON
    {
        public Feature(Geometry geometry)
        {
            super("Feature");
            json.put("geometry", geometry.json);
            json.put("properties", new JSONObject());
        }
        /**
         * Sets a bounding box. JSON object is created just before writing. 
         * Therefore it is possible to update this bounding box after setting.
         * @param bbox 
         */
        public void setBbox(BoundingBox bbox)
        {
            this.bbox = bbox;
        }
        public void setProperty(String name, Object value)
        {
            JSONObject props = json.getJSONObject("properties");
            props.put(name, value);
        }
        public Object getProperty(String name)
        {
            JSONObject props = json.getJSONObject("properties");
            return props.get(name);
        }
        public void setId(Object value)
        {
            json.put("id", value);
        }
        public Object getId()
        {
            return json.get("id");
        }
    }
    protected static class Geometry extends GeoJSON
    {
        public Geometry(String type)
        {
            super(type);
        }
    }
    protected static class Geometry1D extends Geometry
    {
        protected JSONArray coordinates = new JSONArray();

        protected Geometry1D(String type, GeoPt location)
        {
            super(type);
            set(location);
            json.put("coordinates", coordinates);
        }
        public final void set(GeoPt location)
        {
            int length = coordinates.length();
            for (int ii=length-1;ii>0;ii--)
            {
                coordinates.remove(ii);
            }
            coordinates.put(location.getLongitude());
            coordinates.put(location.getLatitude());
        }
        
    }
    public static class Point extends Geometry1D
    {
        public Point(GeoPt location)
        {
            super("Point", location);
        }
    }
    protected static class Geometry2D extends Geometry
    {
        protected JSONArray coordinates = new JSONArray();

        protected Geometry2D(String type, Collection<GeoPt> locations)
        {
            this(type);
            add(locations);
        }
        protected Geometry2D(String type)
        {
            super(type);
            json.put("coordinates", coordinates);
        }
        public final void add(Collection<GeoPt> locations)
        {
            for (GeoPt location : locations)
            {
                add(location);
            }
        }
        public final void add(GeoPt location)
        {
            JSONArray point = new JSONArray();
            coordinates.put(point);
            point.put(location.getLongitude());
            point.put(location.getLatitude());
        }
        
    }
    public static class LineString extends Geometry2D
    {
        public LineString()
        {
            super("LineString");
        }
        public LineString(Collection<GeoPt> locations)
        {
            super("LineString", locations);
        }
    }
    public static class MultiPoint extends Geometry2D
    {
        public MultiPoint(Collection<GeoPt> locations)
        {
            super("MultiPoint", locations);
        }
    }
    protected static class Geometry3D extends Geometry
    {
        protected JSONArray coordinates = new JSONArray();
        protected JSONArray array;

        protected Geometry3D(String type, Collection<GeoPt> locations)
        {
            super(type);
            json.put("coordinates", coordinates);
            add(locations);
        }
        public final void add(Collection<GeoPt> locations)
        {
            array = new JSONArray();
            coordinates.put(array);
            for (GeoPt location : locations)
            {
                add(location);
            }
        }
        public final void add(GeoPt location)
        {
            JSONArray point = new JSONArray();
            array.put(point);
            point.put(location.getLongitude());
            point.put(location.getLatitude());
        }
        
    }
    public static class Polygon extends Geometry3D
    {
        public Polygon(Collection<GeoPt> locations)
        {
            super("Polygon", locations);
        }
    }
    public static class MultiLineString extends Geometry3D
    {
        public MultiLineString(Collection<GeoPt> locations)
        {
            super("MultiLineString", locations);
        }
    }
}
