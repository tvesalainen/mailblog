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

import org.vesalainen.util.navi.AbstractBoundingBox;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import java.io.Serializable;
import org.json.JSONObject;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.repacked.net.opengis.kml.LatLonAltBoxType;

/**
 * @author Timo Vesalainen
 */
public class GeoPtBoundingBox extends AbstractBoundingBox<GeoPt> implements Serializable
{
    protected static final long serialVersionUID = 1L;

    public GeoPtBoundingBox()
    {
        super(GeoPtSupport.LOCATION_SUPPORT);
    }

    public GeoPtBoundingBox(GeoPt point)
    {
        super(GeoPtSupport.LOCATION_SUPPORT, point);
    }

    public GeoPtBoundingBox(GeoPt northEast, GeoPt southWest)
    {
        super(GeoPtSupport.LOCATION_SUPPORT, northEast, southWest);
    }

    public GeoPtBoundingBox(double latitude, double longitude, double dia)
    {
        super(GeoPtSupport.LOCATION_SUPPORT, latitude, longitude, dia);
    }

    public GeoPtBoundingBox(double north, double east, double south, double west)
    {
        super(GeoPtSupport.LOCATION_SUPPORT, north, east, south, west);
    }

    public GeoPtBoundingBox(String southWestNorthEast)
    {
        super(GeoPtSupport.LOCATION_SUPPORT, southWestNorthEast);
    }

    public GeoPtBoundingBox(JSONObject json)
    {
        this(json.getDouble("north"), json.getDouble("east"), json.getDouble("south"), json.getDouble("west"));
    }

    
    public static GeoPtBoundingBox getInstance(Entity entity)
    {
            switch (entity.getKind())
            {
                case PlacemarkKind:
                    return new GeoPtBoundingBox((GeoPt) entity.getProperty(LocationProperty));
                case TrackKind:
                case TrackSeqKind:
                    return new GeoPtBoundingBox((GeoPt) entity.getProperty(NorthEastProperty), (GeoPt) entity.getProperty(SouthWestProperty));
                default:
                    throw new UnsupportedOperationException(entity.getKind()+" not supported");
            }
    }
    public static boolean isPopulated(Entity entity)
    {
        return entity.hasProperty(SouthWestProperty) && entity.hasProperty(NorthEastProperty);
    }
    public void populate(JSONObject json)
    {
        json.put("north", north);
        json.put("south", south);
        json.put("east", east);
        json.put("west", west);
    }
    public void populate(Entity entity)
    {
        entity.setProperty(SouthWestProperty, getSouthWest());
        entity.setProperty(NorthEastProperty, getNorthEast());
    }
    public void populate(LatLonAltBoxType box)
    {
        box.setNorth(north);
        box.setSouth(south);
        box.setWest(west);
        box.setEast(east);
    }


}
