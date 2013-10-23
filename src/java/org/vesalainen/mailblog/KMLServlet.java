/*
 * Copyright (C) 2013 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.mailblog;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import net.opengis.kml.AbstractFeatureType;
import net.opengis.kml.AbstractStyleSelectorType;
import net.opengis.kml.BalloonStyleType;
import net.opengis.kml.DocumentType;
import net.opengis.kml.IconStyleType;
import net.opengis.kml.LatLonAltBoxType;
import net.opengis.kml.LineStringType;
import net.opengis.kml.LineStyleType;
import net.opengis.kml.LinkType;
import net.opengis.kml.LodType;
import net.opengis.kml.LookAtType;
import net.opengis.kml.NetworkLinkType;
import net.opengis.kml.ObjectFactory;
import net.opengis.kml.PlacemarkType;
import net.opengis.kml.PointType;
import net.opengis.kml.RegionType;
import net.opengis.kml.StyleType;
import net.opengis.kml.ViewRefreshModeEnumType;
import org.vesalainen.kml.KMZ;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS.CacheOutputStream;

/**
 * @author Timo Vesalainen
 */
public class KMLServlet extends HttpServlet
{
    private static final String PathStyleId = "path-style";
    private static final String ImageStyleId = "image-style";
    private static final String BlogStyleId = "blog-style";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        DS ds = DS.get();
        URL base = getBase(request);
        if (!ds.serveFromCache(request, response))
        {
            String keyString = request.getParameter("key");
            if (keyString != null)
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response, "application/vnd.google-earth.kmz", "utf-8", false))
                {
                    Key key = KeyFactory.stringToKey(keyString);
                    log("writeDetails "+key);
                    switch (key.getKind())
                    {
                        case "Placemarks":
                            writePlacemark(key, cos);
                            break;
                        case "TrackSeq":
                            writeTrackSeq(key, request, cos);
                            break;
                    }
                    cos.cache();
                }
            }
            else
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response, "application/vnd.google-earth.kmz", "utf-8", false))
                {
                    writeRegions(request, cos);
                    log("writeRegions");
                    cos.cache();
                }
            }
        }
    }

    private void writePlacemark(Key key, CacheOutputStream out) throws IOException
    {
        DS ds = DS.get();
        Entity placemark = null;
        try
        {
            placemark = ds.get(key);
        }
        catch (EntityNotFoundException ex)
        {
            throw new IOException(ex);
        }
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // styles
        setStyles(documentType, factory, settings);
        
        PlacemarkType placemarkType = factory.createPlacemarkType();
        JAXBElement<PlacemarkType> aPlacemark = factory.createPlacemark(placemarkType);
        abstractFeatureGroup.add(aPlacemark);
        
        String description = (String) placemark.getProperty(DescriptionProperty);
        SpotType spotType = SpotType.getSpotType(description);
        placemarkType.setStyleUrl("#"+SpotType.getSpotStyleId(spotType));
        String title = (String) placemark.getProperty(TitleProperty);
        //placemarkType.setName(title);
        //placemarkType.setDescription(description);
        String id = KeyFactory.keyToString(placemark.getKey());

        placemarkType.setId(id);

        GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
        if (location != null)
        {
            PointType pointType = factory.createPointType();
            pointType.getCoordinates().add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
            placemarkType.setAbstractGeometryGroup(factory.createPoint(pointType));
        }
        // write
        kmz.write(out);
    }

    private void writeTrackSeq(Key trackSeqKey, HttpServletRequest request, CacheOutputStream out) throws IOException
    {
        DS ds = DS.get();
        Iterable<Entity> trackPointIterator = ds.fetchTrackPoints(trackSeqKey);
        Entity trackSeq = null;
        try
        {
            trackSeq = ds.get(trackSeqKey);
        }
        catch (EntityNotFoundException ex)
        {
            throw new IOException(ex);
        }
        Date begin = (Date) trackSeq.getProperty(BeginProperty);
        Date end = (Date) trackSeq.getProperty(EndProperty);
        if (begin == null || end == null)
        {
            return;
        }
        Iterable<Entity> imageMetadataIterable = ds.fetchImageMetadata(begin, end);

        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // styles
        setStyles(documentType, factory, settings);
        // metadata
        Iterator<Entity> imageMetadataIterator = imageMetadataIterable.iterator();
        Entity imageMetadata = null;
        if (imageMetadataIterator.hasNext())
        {
            imageMetadata = imageMetadataIterator.next();
        }
        // trackPoints
        PlacemarkType trackPointPlacemarkType = factory.createPlacemarkType();
        JAXBElement<PlacemarkType> trackPointPlacemark = factory.createPlacemark(trackPointPlacemarkType);
        abstractFeatureGroup.add(trackPointPlacemark);
        trackPointPlacemarkType.setStyleUrl('#'+ImageStyleId);
        LineStringType trackPointLineStringType = factory.createLineStringType();
        JAXBElement<LineStringType> trackPointLineString = factory.createLineString(trackPointLineStringType);
        trackPointPlacemarkType.setAbstractGeometryGroup(trackPointLineString);
        List<String> trackPointCoordinates = trackPointLineStringType.getCoordinates();
        for (Entity trackPoint : trackPointIterator)
        {
            GeoPt location = (GeoPt) trackPoint.getProperty(LocationProperty);
            if (location != null)
            {
                // image
                if (imageMetadata != null)
                {
                    Date trackDate = new Date(trackPoint.getKey().getId());
                    Date imageDate = (Date) imageMetadata.getProperty("DateTimeOriginal");
                    if (trackDate.after(imageDate))
                    {
                        String sha1 = imageMetadata.getKey().getName();
                        PlacemarkType imagePlacemarkType = factory.createPlacemarkType();
                        JAXBElement<PlacemarkType> imagePlacemark = factory.createPlacemark(imagePlacemarkType);
                        abstractFeatureGroup.add(imagePlacemark);
                        imagePlacemarkType.setStyleUrl("#"+ImageStyleId);
                        imagePlacemarkType.setDescription("<img src=\""+getBase(request)+"/blob?sha1="+sha1+"\" alt=\"loading...\"></img>");
                        PointType pointType = factory.createPointType();
                        JAXBElement<PointType> point = factory.createPoint(pointType);
                        imagePlacemarkType.setAbstractGeometryGroup(point);
                        pointType.getCoordinates().add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
                        if (imageMetadataIterator.hasNext())
                        {
                            imageMetadata = imageMetadataIterator.next();
                        }
                        else
                        {
                            imageMetadata = null;
                        }
                    }
                }
               // linestring
                trackPointCoordinates.add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
            }
        }
        // write
        kmz.write(out);
    }

    private void writeRegions(HttpServletRequest request, CacheOutputStream out) throws IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // styles
        setStyles(documentType, factory, settings);
        // placemarks
        Entity prevPlacemark = null;
        Entity lastPlacemark = null;
        LatLonAltBox overallBox = new LatLonAltBox();
        Iterable<Entity> placemarkIterator = ds.fetchPlacemarks();
        Iterable<Entity> trackSeqIterator = ds.fetchTrackSeqs();
        for (Entity placemark : placemarkIterator)
        {
            GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
            if (location != null)
            {
                if (prevPlacemark != null)
                {
                    GeoPt prevLocation = (GeoPt) prevPlacemark.getProperty(LocationProperty);
                    // overall placemark
                    PlacemarkType overallPlacemarkType = factory.createPlacemarkType();
                    JAXBElement<PlacemarkType> overallPlacemark = factory.createPlacemark(overallPlacemarkType);
                    abstractFeatureGroup.add(overallPlacemark);
                    // linestring
                    overallPlacemarkType.setStyleUrl('#'+PathStyleId);
                    LineStringType overallLineStringType = factory.createLineStringType();
                    JAXBElement<LineStringType> overallLineString = factory.createLineString(overallLineStringType);
                    overallPlacemarkType.setAbstractGeometryGroup(overallLineString);
                    List<String> overallCoordinates = overallLineStringType.getCoordinates();
                    // region
                    RegionType overallRegionType = factory.createRegionType();
                    overallPlacemarkType.setRegion(overallRegionType);
                    // latlonaltbox
                    LatLonAltBoxType overallLatLonAltBoxType = factory.createLatLonAltBoxType();
                    overallRegionType.setLatLonAltBox(overallLatLonAltBoxType);
                    overallBox.clear();
                    overallBox.add(prevLocation);
                    overallBox.add(location);
                    overallBox.populate(overallLatLonAltBoxType);
                    overallCoordinates.add(String.format(Locale.US, "%1$f,%2$f,0", prevLocation.getLongitude(), prevLocation.getLatitude()));
                    overallCoordinates.add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
                    // lod
                    LodType overallLodType = factory.createLodType();
                    overallLodType.setMinLodPixels(-1.0);
                    double area = overallBox.getArea()/settings.getTrackMinimumDistance();
                    overallLodType.setMaxLodPixels(area);
                    overallLodType.setMaxFadeExtent(area*0.5);
                    overallRegionType.setLod(overallLodType);
                }
                // network link
                NetworkLinkType networkLinkType = factory.createNetworkLinkType();
                // region
                RegionType regionType = factory.createRegionType();
                networkLinkType.setRegion(regionType);
                // latlonalt
                LatLonAltBoxType latLonAltBoxType = factory.createLatLonAltBoxType();
                LatLonAltBox box = new LatLonAltBox(location, 1.0);
                box.populate(latLonAltBoxType);
                regionType.setLatLonAltBox(latLonAltBoxType);
                // lod
                LodType lodType = factory.createLodType();
                lodType.setMinLodPixels(10.0);
                lodType.setMaxLodPixels(-1.0);
                regionType.setLod(lodType);
                // link
                LinkType linkType = factory.createLinkType();
                linkType.setHref(getRequestUrl(request));
                linkType.setHttpQuery("key="+KeyFactory.keyToString(placemark.getKey()));
                linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
                networkLinkType.setLink(linkType);
                JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                abstractFeatureGroup.add(networkLink);
                prevPlacemark = placemark;
                lastPlacemark = placemark;
            }
        }
        if (lastPlacemark != null)
        {
            // lookAt
            GeoPt location = (GeoPt) lastPlacemark.getProperty(LocationProperty);
            LookAtType lookAtType = factory.createLookAtType();
            lookAtType.setLatitude((double)location.getLatitude());
            lookAtType.setLongitude((double)location.getLongitude());
            lookAtType.setRange(2000.0);
            JAXBElement<LookAtType> lookAt = factory.createLookAt(lookAtType);
            documentType.setAbstractViewGroup(lookAt);
        }
        for (Entity trackSeq : trackSeqIterator)
        {
            GeoPt sw = (GeoPt) trackSeq.getProperty(SouthWestProperty);
            GeoPt ne = (GeoPt) trackSeq.getProperty(NorthEastProperty);
            if (sw != null && ne != null)
            {
                // network link
                NetworkLinkType networkLinkType = factory.createNetworkLinkType();
                // region
                RegionType regionType = factory.createRegionType();
                networkLinkType.setRegion(regionType);
                // latlonalt
                LatLonAltBoxType latLonAltBoxType = factory.createLatLonAltBoxType();
                LatLonAltBox box = new LatLonAltBox();
                box.add(sw);
                box.add(ne);
                box.populate(latLonAltBoxType);
                regionType.setLatLonAltBox(latLonAltBoxType);
                // lod
                LodType lodType = factory.createLodType();
                double area = box.getArea()/settings.getTrackMinimumDistance();
                lodType.setMinLodPixels(area);
                lodType.setMinFadeExtent(area*0.5);
                lodType.setMaxLodPixels(-1.0);
                regionType.setLod(lodType);
                // link
                LinkType linkType = factory.createLinkType();
                linkType.setHref(getRequestUrl(request));
                linkType.setHttpQuery("key="+KeyFactory.keyToString(trackSeq.getKey()));
                linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
                networkLinkType.setLink(linkType);
                JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                abstractFeatureGroup.add(networkLink);
            }
        }
        // write
        kmz.write(out);

    }
    private void setStyles(DocumentType documentType, ObjectFactory factory, Settings settings)
    {
        List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroup = documentType.getAbstractStyleSelectorGroup();
        // path style
        StyleType pathStyleType = factory.createStyleType();
        pathStyleType.setId(PathStyleId);
        LineStyleType pathLineStyleType = factory.createLineStyleType();
        pathLineStyleType.setColor(settings.getPathColor());
        pathStyleType.setLineStyle(pathLineStyleType);
        JAXBElement<StyleType> pathStyle = factory.createStyle(pathStyleType);
        abstractStyleSelectorGroup.add(pathStyle);
        // track style
        StyleType trackStyleType = factory.createStyleType();
        trackStyleType.setId(PathStyleId);
        LineStyleType trackLineStyleType = factory.createLineStyleType();
        trackLineStyleType.setColor(settings.getTrackColor());
        trackStyleType.setLineStyle(trackLineStyleType);
        JAXBElement<StyleType> trackStyle = factory.createStyle(trackStyleType);
        abstractStyleSelectorGroup.add(trackStyle);
        // imagestyle
        StyleType imageStyleType = factory.createStyleType();
        JAXBElement<StyleType> imageStyle = factory.createStyle(imageStyleType);
        abstractStyleSelectorGroup.add(imageStyle);
        imageStyleType.setId(ImageStyleId);

        BalloonStyleType imageBalloonStyle = factory.createBalloonStyleType();
        imageBalloonStyle.setText("$[name]<div>$[description]</div>");
        imageStyleType.setBalloonStyle(imageBalloonStyle);

        LinkType imageIcon = factory.createLinkType();
        imageIcon.setHref(settings.getImageIcon());
        IconStyleType imageIconStyle = factory.createIconStyleType();
        imageIconStyle.setIcon(imageIcon);
        imageStyleType.setIconStyle(imageIconStyle);
        
        // blog style
        StyleType blogStyleType = factory.createStyleType();
        JAXBElement<StyleType> blogStyle = factory.createStyle(blogStyleType);
        abstractStyleSelectorGroup.add(blogStyle);
        blogStyleType.setId(ImageStyleId);

        BalloonStyleType blogBalloonStyle = factory.createBalloonStyleType();
        blogBalloonStyle.setText("$[name]<div>$[description]</div>");
        blogStyleType.setBalloonStyle(blogBalloonStyle);

        LinkType blogIcon = factory.createLinkType();
        blogIcon.setHref(settings.getBlogIcon());
        IconStyleType blogIconStyle = factory.createIconStyleType();
        blogIconStyle.setIcon(blogIcon);
        blogStyleType.setIconStyle(blogIconStyle);
        
        // spot styles
        for (SpotType type : SpotType.values())
        {
            StyleType styleType = factory.createStyleType();
            JAXBElement<StyleType> style = factory.createStyle(styleType);
            abstractStyleSelectorGroup.add(style);
            styleType.setId(SpotType.getSpotStyleId(type));

            BalloonStyleType balloonStyle = factory.createBalloonStyleType();
            balloonStyle.setText("$[name]<div>$[description]</div>");
            styleType.setBalloonStyle(balloonStyle);

            LinkType icon = factory.createLinkType();
            icon.setHref(settings.getSpotIcon(type));
            IconStyleType iconStyle = factory.createIconStyleType();
            iconStyle.setIcon(icon);
            styleType.setIconStyle(iconStyle);
        }
    }

    private URL getBase(HttpServletRequest request) throws IOException
    {
        try
        {
            URI uri = new URI(request.getRequestURL().toString());
            return uri.resolve("/").toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new IOException(ex);
        }
        catch (URISyntaxException ex)
        {
            throw new IOException(ex);
        }
    }

    private String getRequestUrl(HttpServletRequest request)
    {
        String namespace = NamespaceManager.get();
        String serverName = request.getServerName();
        if (serverName.endsWith(namespace))
        {
            return request.getRequestURL().toString();
        }
        else
        {
            return request.getRequestURL().toString()+"?"+NamespaceParameter+"="+NamespaceManager.get();
        }
    }

}
