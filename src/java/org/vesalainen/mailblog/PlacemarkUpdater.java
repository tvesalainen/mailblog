/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import java.util.Date;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import net.opengis.kml.AbstractFeatureType;
import net.opengis.kml.DocumentType;
import net.opengis.kml.FolderType;
import net.opengis.kml.LineStringType;
import net.opengis.kml.LinearRingType;
import net.opengis.kml.PlacemarkType;
import net.opengis.kml.PointType;
import net.opengis.kml.PolygonType;
import net.opengis.kml.TimeSpanType;
import net.opengis.kml.TimeStampType;
import org.vesalainen.kml.FeatureVisitor;
import org.vesalainen.kml.KML;

/**
 * @author Timo Vesalainen
 */
public class PlacemarkUpdater extends FeatureVisitor<Entity> implements BlogConstants
{
    private DS ds;
    private KML kml;

    public PlacemarkUpdater(DS ds, KML kml)
    {
        this.ds = ds;
        this.kml = kml;
    }
    
    @Override
    protected void handleTimeSpan(AbstractFeatureType feature, TimeSpanType timeSpan, Entity ctx)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void handleTimeStamp(AbstractFeatureType feature, TimeStampType timeStamp, Entity ctx)
    {
        if (timeStamp != null)
        {
            String when = timeStamp.getWhen();
            XMLGregorianCalendar cal = kml.getDtFactory().newXMLGregorianCalendar(when);
            Date time = cal.toGregorianCalendar().getTime();
            ctx.setProperty(TimestampProperty, time);
        }
    }

    @Override
    protected void handlePoint(PlacemarkType placemark, PointType point, Entity ctx)
    {
    }

    @Override
    protected void handleLineString(PlacemarkType placemark, LineStringType lineString, Entity ctx)
    {
    }

    @Override
    protected void handleLinearRing(PlacemarkType placemark, LinearRingType linearRing, Entity ctx)
    {
    }

    @Override
    protected void handlePolygon(PlacemarkType placemark, PolygonType polygon, Entity ctx)
    {
    }

    @Override
    protected void handleCoordinates(PlacemarkType placemark, List<String> coordinates, Entity ctx)
    {
        if (coordinates != null && !coordinates.isEmpty())
        {
            String[] ss = coordinates.get(0).split(",");
            if (ss.length >= 2)
            {
                float lon = Float.parseFloat(ss[0]);
                float lat = Float.parseFloat(ss[1]);
                GeoPt location = new GeoPt(lat, lon);
                ctx.setProperty(LocationProperty, location);
            }
        }
    }

    @Override
    protected Entity startOf(DocumentType document, Entity ctx)
    {
        return ctx;
    }

    @Override
    protected void endOf(DocumentType document, Entity ctx)
    {
    }

    @Override
    protected Entity startOf(FolderType folder, Entity ctx)
    {
        return ctx;
    }

    @Override
    protected void endOf(FolderType folder, Entity ctx)
    {
    }

    @Override
    protected Entity startOf(PlacemarkType placemark, Entity ctx)
    {
        ctx = ds.createPlacemark();
        ctx.setProperty(TitleProperty, placemark.getName());
        ctx.setProperty(DescriptionProperty, placemark.getDescription());
        return ctx;
    }

    @Override
    protected void endOf(PlacemarkType placemark, Entity ctx)
    {
        ds.put(ctx);
    }

}
