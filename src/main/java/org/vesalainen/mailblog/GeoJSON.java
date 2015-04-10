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
import java.io.Serializable;
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
    protected AbstractGeometryCollection parent;
    
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
    
    public static abstract class AbstractGeometryCollection extends GeoJSON
    {
        protected final LatLonAltBox bbox = new LatLonAltBox();
        public AbstractGeometryCollection(String type)
        {
            super(type);
        }
        protected void addBbox(GeoPt location)
        {
            bbox.add(location);
        }

        @Override
        public void write(Writer writer)
        {
            JSONArray ba = new JSONArray();
            ba.put(bbox.getWest());
            ba.put(bbox.getSouth());
            ba.put(bbox.getEast());
            ba.put(bbox.getNorth());
            json.put("bbox", ba);
            super.write(writer);
        }
        
        public MultiLineString addMultiLineString(Collection<GeoPt> points)
        {
            return (MultiLineString) addGeometry(new MultiLineString(this, points));
        }
        public MultiPoint addMultiPoint(Collection<GeoPt> points)
        {
            return (MultiPoint) addGeometry(new MultiPoint(this, points));
        }
        public Polygon addPolygon(Collection<GeoPt> points)
        {
            return (Polygon) addGeometry(new Polygon(this, points));
        }
        public LineString addLineString(Collection<GeoPt> points)
        {
            return (LineString) addGeometry(new LineString(this, points));
        }
        public Point addPoint(GeoPt point)
        {
            return (Point) addGeometry(new Point(this, point));
        }
        public abstract Geometry addGeometry(Geometry geometry);
    }
    public static class GeometryCollection extends AbstractGeometryCollection
    {
        JSONArray geometries = new JSONArray();
        public GeometryCollection()
        {
            super("GeometryCollection");
            json.put("geometries", geometries);
        }
        @Override
        public Geometry addGeometry(Geometry geometry)
        {
            geometries.put(geometry.json);
            return geometry;
        }
    }
    public static class FeatureCollection extends AbstractGeometryCollection
    {
        JSONArray features = new JSONArray();
        public FeatureCollection()
        {
            super("FeatureCollection");
            json.put("features", features);
        }
        @Override
        public Geometry addGeometry(Geometry geometry)
        {
            addFeature(new Feature(geometry));
            return geometry;
        }
        protected void addFeature(Feature feature)
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
    protected static class Geometry extends GeoJSON
    {
        public Geometry(AbstractGeometryCollection parent, String type)
        {
            super(type);
            this.parent = parent;
        }
    }
    protected static class Geometry1D extends Geometry
    {
        protected JSONArray coordinates = new JSONArray();

        protected Geometry1D(AbstractGeometryCollection parent, String type, GeoPt location)
        {
            super(parent, type);
            set(location);
            json.put("coordinates", coordinates);
        }
        public final void set(GeoPt location)
        {
            if (parent != null)
            {
                parent.addBbox(location);
            }
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
            this(null, location);
        }
        public Point(AbstractGeometryCollection parent, GeoPt location)
        {
            super(parent, "Point", location);
        }
    }
    protected static class Geometry2D extends Geometry
    {
        protected JSONArray coordinates = new JSONArray();

        protected Geometry2D(AbstractGeometryCollection parent, String type, Collection<GeoPt> locations)
        {
            super(parent, type);
            add(locations);
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
            if (parent != null)
            {
                parent.addBbox(location);
            }
            JSONArray point = new JSONArray();
            coordinates.put(point);
            point.put(location.getLongitude());
            point.put(location.getLatitude());
        }
        
    }
    public static class LineString extends Geometry2D
    {
        public LineString(Collection<GeoPt> locations)
        {
            this(null, locations);
        }
        public LineString(AbstractGeometryCollection parent, Collection<GeoPt> locations)
        {
            super(parent, "LineString", locations);
        }
    }
    public static class MultiPoint extends Geometry2D
    {
        public MultiPoint(Collection<GeoPt> locations)
        {
            this(null, locations);
        }
        public MultiPoint(AbstractGeometryCollection parent, Collection<GeoPt> locations)
        {
            super(parent, "MultiPoint", locations);
        }
    }
    protected static class Geometry3D extends Geometry
    {
        protected JSONArray coordinates = new JSONArray();
        protected JSONArray array;

        protected Geometry3D(AbstractGeometryCollection parent, String type, Collection<GeoPt> locations)
        {
            super(parent, type);
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
            if (parent != null)
            {
                parent.addBbox(location);
            }
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
            this(null, locations);
        }
        public Polygon(AbstractGeometryCollection parent, Collection<GeoPt> locations)
        {
            super(parent, "Polygon", locations);
        }
    }
    public static class MultiLineString extends Geometry3D
    {
        public MultiLineString(Collection<GeoPt> locations)
        {
            this(null, locations);
        }
        public MultiLineString(AbstractGeometryCollection parent, Collection<GeoPt> locations)
        {
            super(parent, "MultiLineString", locations);
        }
    }
}
