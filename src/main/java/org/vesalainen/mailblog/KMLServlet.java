/*
 * Copyright (C) 2013 Timo Vesalainen
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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.vesalainen.kml.KMZ;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS.CacheOutputStream;
import org.vesalainen.repacked.net.opengis.kml.AbstractFeatureType;
import org.vesalainen.repacked.net.opengis.kml.AbstractStyleSelectorType;
import org.vesalainen.repacked.net.opengis.kml.BalloonStyleType;
import org.vesalainen.repacked.net.opengis.kml.DocumentType;
import org.vesalainen.repacked.net.opengis.kml.IconStyleType;
import org.vesalainen.repacked.net.opengis.kml.LatLonAltBoxType;
import org.vesalainen.repacked.net.opengis.kml.LineStringType;
import org.vesalainen.repacked.net.opengis.kml.LineStyleType;
import org.vesalainen.repacked.net.opengis.kml.LinkType;
import org.vesalainen.repacked.net.opengis.kml.LodType;
import org.vesalainen.repacked.net.opengis.kml.LookAtType;
import org.vesalainen.repacked.net.opengis.kml.NetworkLinkType;
import org.vesalainen.repacked.net.opengis.kml.ObjectFactory;
import org.vesalainen.repacked.net.opengis.kml.PlacemarkType;
import org.vesalainen.repacked.net.opengis.kml.PointType;
import org.vesalainen.repacked.net.opengis.kml.RegionType;
import org.vesalainen.repacked.net.opengis.kml.StyleType;
import org.vesalainen.repacked.net.opengis.kml.ViewRefreshModeEnumType;

/**
 * @author Timo Vesalainen
 */
public class KMLServlet extends HttpServlet
{
    private static final String PathStyleId = "path-style";
    private static final String TrackStyleId = "track-style";
    private static final String ImageStyleId = "image-style";
    private static final String BlogStyleId = "blog-style";
    private static final String HiLiteStyleId = "hi-lite-style";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        URIBuilder builder = new URIBuilder(request)
                .setFragment(null)
                .setPath(null)
                .setQuery(null);
        builder = addNamespace(builder);
        String base = builder.toString();
        String styleUri = builder.setPath(StylePath).toString();
        if (!ds.serveFromCache(request, response))
        {
            String keyString = request.getParameter(KeyParameter);
            if (keyString != null)
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response))
                {
                    cos.setContentType("application/vnd.google-earth.kmz");
                    Key key = KeyFactory.stringToKey(keyString);
                    switch (key.getKind())
                    {
                        case PlacemarkKind:
                            writePlacemark(key, styleUri, cos);
                            break;
                        case BlogKind:
                            writeBlogLocation(key, styleUri, cos);
                            break;
                        case TrackSeqKind:
                            writeTrackSeq(key, styleUri, cos);
                            break;
                        default:
                            log("unexcpected key "+key);
                    }
                    cos.cache();
                }
            }
            else
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response))
                {
                    cos.setContentType("application/vnd.google-earth.kmz");
                    String pathInfo = request.getPathInfo();
                    pathInfo = pathInfo == null ? "" : pathInfo;
                    switch (pathInfo)
                    {
                        case StylePath:
                            writeStyles(cos);
                            break;
                        case PlacemarkPath:
                            writePlacemarkPath(base, styleUri, cos);
                            break;
                        case TrackSeqPath:
                            writeTrackSeqPath(base, styleUri, cos);
                            break;
                        case BlogLocationPath:
                            writeBlogLocationPath(base, styleUri, cos);
                            break;
                        default:
                            Entity lookAt = null;
                            String lookAtKeyString = request.getParameter(LookAtParameter);
                            if (lookAtKeyString != null)
                            {
                                Key lookAtKey = KeyFactory.stringToKey(lookAtKeyString);
                                lookAt = ds.get(lookAtKey);
                            }
                            else
                            {
                                lookAt = ds.fetchLastPlacemark(settings);
                            }
                            writeLookAt(builder, lookAt, cos);
                            break;
                    }
                    cos.cache();
                }
                catch (EntityNotFoundException ex)
                {
                    throw new IOException(ex);
                }
            }
        }
    }

    private void writeStyles(CacheOutputStream out) throws IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("Styles");
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // styles
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
        trackStyleType.setId(TrackStyleId);
        LineStyleType trackLineStyleType = factory.createLineStyleType();
        trackLineStyleType.setColor(settings.getTrackColor());
        trackStyleType.setLineStyle(trackLineStyleType);
        JAXBElement<StyleType> trackStyle = factory.createStyle(trackStyleType);
        abstractStyleSelectorGroup.add(trackStyle);
        // alpha track styles
        for (int a=55;a<=255;a++)
        {
            StyleType alphaTrackStyleType = factory.createStyleType();
            alphaTrackStyleType.setId(TrackStyleId+"-"+a);
            LineStyleType alphaTrackLineStyleType = factory.createLineStyleType();
            alphaTrackLineStyleType.setColor(settings.getTrackColor(a));
            alphaTrackStyleType.setLineStyle(alphaTrackLineStyleType);
            JAXBElement<StyleType> alphaTrackStyle = factory.createStyle(alphaTrackStyleType);
            abstractStyleSelectorGroup.add(alphaTrackStyle);
        }
        // imagestyle
        StyleType imageStyleType = factory.createStyleType();
        JAXBElement<StyleType> imageStyle = factory.createStyle(imageStyleType);
        abstractStyleSelectorGroup.add(imageStyle);
        imageStyleType.setId(ImageStyleId);

        BalloonStyleType imageBalloonStyle = factory.createBalloonStyleType();
        imageBalloonStyle.setText("<img src=\"/blob?sha1=$[id]\" alt=\"loading...\"></img>");
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
        blogStyleType.setId(BlogStyleId);

        BalloonStyleType blogBalloonStyle = factory.createBalloonStyleType();
        blogBalloonStyle.setText("<a href=\"/index.html?blog=$[id]\">$[name]</a>");
        blogStyleType.setBalloonStyle(blogBalloonStyle);

        LinkType blogIcon = factory.createLinkType();
        blogIcon.setHref(settings.getBlogIcon());
        IconStyleType blogIconStyle = factory.createIconStyleType();
        blogIconStyle.setIcon(blogIcon);
        blogStyleType.setIconStyle(blogIconStyle);
        
        // hi-lite style
        StyleType hiLiteStyleType = factory.createStyleType();
        JAXBElement<StyleType> hiLiteStyle = factory.createStyle(hiLiteStyleType);
        abstractStyleSelectorGroup.add(hiLiteStyle);
        hiLiteStyleType.setId(HiLiteStyleId);

        BalloonStyleType hiLiteBalloonStyle = factory.createBalloonStyleType();
        hiLiteBalloonStyle.setText("$[name]<div>$[description]</div>");
        hiLiteStyleType.setBalloonStyle(hiLiteBalloonStyle);

        LinkType hiLiteIcon = factory.createLinkType();
        hiLiteIcon.setHref(settings.getBlogIcon());
        IconStyleType hiLiteIconStyle = factory.createIconStyleType();
        hiLiteIconStyle.setIcon(hiLiteIcon);
        hiLiteStyleType.setIconStyle(hiLiteIconStyle);
        
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
        // write
        kmz.write(out);
    }

    private void log(KMZ kmz)
    {
        JAXBContext jaxbCtx;
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.repacked.net.opengis.kml");
            Marshaller marshaller = jaxbCtx.createMarshaller();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(kmz.getKml(), baos);
            String s = new String(baos.toByteArray());
            log(s);
        }
        catch (JAXBException ex)
        {
            log(ex.getMessage(), ex);
        }
    }
    
    private void writePlacemark(Key key, String styleUri, CacheOutputStream out) throws IOException
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
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("Placemark-"+KeyFactory.keyToString(key));
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        
        PlacemarkType placemarkType = factory.createPlacemarkType();
        JAXBElement<PlacemarkType> aPlacemark = factory.createPlacemark(placemarkType);
        abstractFeatureGroup.add(aPlacemark);
        
        String description = (String) placemark.getProperty(DescriptionProperty);
        SpotType spotType = SpotType.getSpotType(description);
        placemarkType.setStyleUrl(styleUri+"#"+SpotType.getSpotStyleId(spotType));

        String id = KeyFactory.keyToString(placemark.getKey());

        placemarkType.setId(id);

        GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
        if (location != null)
        {
            PointType pointType = factory.createPointType();
            pointType.getCoordinates().add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
            placemarkType.setAbstractGeometryGroup(factory.createPoint(pointType));
            StringBuilder sb = new StringBuilder();
            ds.describeLocation(placemark, sb);
            placemarkType.setDescription(sb.toString());
        }
        // write
        kmz.write(out);
    }

    private void writeBlogLocation(Key key, String styleUri, CacheOutputStream cos) throws IOException
    {
        DS ds = DS.get();
        Entity blog = null;
        try
        {
            blog = ds.get(key);
        }
        catch (EntityNotFoundException ex)
        {
            throw new IOException(ex);
        }
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("BlogLocation-"+KeyFactory.keyToString(key));
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        
        PlacemarkType placemarkType = factory.createPlacemarkType();
        JAXBElement<PlacemarkType> aPlacemark = factory.createPlacemark(placemarkType);
        abstractFeatureGroup.add(aPlacemark);
        
        placemarkType.setStyleUrl(styleUri+"#"+BlogStyleId);

        String id = KeyFactory.keyToString(blog.getKey());

        placemarkType.setId(id);

        String subject = (String) blog.getProperty(SubjectProperty);
        placemarkType.setName(subject);
        
        GeoPt location = (GeoPt) blog.getProperty(LocationProperty);
        if (location != null)
        {
            PointType pointType = factory.createPointType();
            pointType.getCoordinates().add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
            placemarkType.setAbstractGeometryGroup(factory.createPoint(pointType));
        }
        // write
        kmz.write(cos);
    }

    private void writeTrackSeq(Key trackSeqKey, String styleUri, CacheOutputStream out) throws IOException
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
        // Ageing
        int alpha = getAlpha(begin);
        Iterable<Entity> imageMetadataIterable = ds.fetchImageMetadata(begin, end);

        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("TrackSeq-"+KeyFactory.keyToString(trackSeqKey));
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
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
        trackPointPlacemarkType.setStyleUrl(styleUri+'#'+TrackStyleId+"-"+alpha);
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
                        imagePlacemarkType.setId(sha1);
                        imagePlacemarkType.setStyleUrl(styleUri+"#"+ImageStyleId);
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

    private void writeLookAt(String base, String styleUri, CacheOutputStream out) throws IOException
    {
        DS ds = DS.get();
        Iterable<Entity> placemarkIterator = ds.fetchPlacemarks();
        Iterable<Entity> trackSeqIterator = ds.fetchTrackSeqs();
        Iterable<Entity> blogLocationIterator = ds.fetchBlogLocations();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // placemarks
        Entity lastPlacemark = null;
        LatLonAltBox overallBox = new LatLonAltBox();
        for (Entity placemark : placemarkIterator)
        {
            GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
            if (location != null)
            {
                if (lastPlacemark != null)
                {
                    GeoPt prevLocation = (GeoPt) lastPlacemark.getProperty(LocationProperty);
                    // overall placemark
                    PlacemarkType overallPlacemarkType = factory.createPlacemarkType();
                    JAXBElement<PlacemarkType> overallPlacemark = factory.createPlacemark(overallPlacemarkType);
                    abstractFeatureGroup.add(overallPlacemark);
                    // linestring
                    overallPlacemarkType.setStyleUrl(styleUri+'#'+PathStyleId);
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
                LatLonAltBox box = new LatLonAltBox(location, settings.getTrackMinimumDistance());
                box.populate(latLonAltBoxType);
                regionType.setLatLonAltBox(latLonAltBoxType);
                // lod
                LodType lodType = factory.createLodType();
                lodType.setMinLodPixels(10.0);
                lodType.setMaxLodPixels(-1.0);
                regionType.setLod(lodType);
                // link
                LinkType linkType = factory.createLinkType();
                linkType.setHref(base);
                linkType.setHttpQuery("key="+KeyFactory.keyToString(placemark.getKey()));
                linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
                networkLinkType.setLink(linkType);
                JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                abstractFeatureGroup.add(networkLink);
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
            lookAtType.setRange(settings.getEyeAltitude());
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
                linkType.setHref(base);
                linkType.setHttpQuery("key="+KeyFactory.keyToString(trackSeq.getKey()));
                linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
                networkLinkType.setLink(linkType);
                JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                abstractFeatureGroup.add(networkLink);
            }
        }
        for (Entity blogLocation : blogLocationIterator)
        {
            GeoPt location = (GeoPt) blogLocation.getProperty(LocationProperty);
            // network link
            NetworkLinkType networkLinkType = factory.createNetworkLinkType();
            // region
            RegionType regionType = factory.createRegionType();
            networkLinkType.setRegion(regionType);
            // latlonalt
            LatLonAltBoxType latLonAltBoxType = factory.createLatLonAltBoxType();
            LatLonAltBox box = new LatLonAltBox(location, settings.getTrackMinimumDistance());
            box.populate(latLonAltBoxType);
            regionType.setLatLonAltBox(latLonAltBoxType);
            // lod
            LodType lodType = factory.createLodType();
            lodType.setMinLodPixels(10.0);
            lodType.setMaxLodPixels(-1.0);
            regionType.setLod(lodType);
            // link
            LinkType linkType = factory.createLinkType();
            linkType.setHref(base);
            linkType.setHttpQuery("key="+KeyFactory.keyToString(blogLocation.getKey()));
            linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
            networkLinkType.setLink(linkType);
            JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
            abstractFeatureGroup.add(networkLink);
        }
        // write
        kmz.write(out);

    }
    private void writePlacemarkPath(String base, String styleUri, CacheOutputStream cos) throws IOException
    {
        DS ds = DS.get();
        Iterable<Entity> placemarkIterator = ds.fetchPlacemarks();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("PlacemarkPath");
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // placemarks
        Entity lastPlacemark = null;
        LatLonAltBox overallBox = new LatLonAltBox();
        for (Entity placemark : placemarkIterator)
        {
            GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
            if (location != null)
            {
                if (lastPlacemark != null)
                {
                    Date ts = (Date) placemark.getProperty(TimestampProperty);
                    int alpha = getAlpha(ts);
                    GeoPt prevLocation = (GeoPt) lastPlacemark.getProperty(LocationProperty);
                    // overall placemark
                    PlacemarkType overallPlacemarkType = factory.createPlacemarkType();
                    JAXBElement<PlacemarkType> overallPlacemark = factory.createPlacemark(overallPlacemarkType);
                    abstractFeatureGroup.add(overallPlacemark);
                    // linestring
                    overallPlacemarkType.setStyleUrl(styleUri+'#'+TrackStyleId+"-"+alpha);
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
                LatLonAltBox box = new LatLonAltBox(location, settings.getTrackMinimumDistance());
                box.populate(latLonAltBoxType);
                regionType.setLatLonAltBox(latLonAltBoxType);
                // lod
                LodType lodType = factory.createLodType();
                lodType.setMinLodPixels(10.0);
                lodType.setMaxLodPixels(-1.0);
                regionType.setLod(lodType);
                // link
                LinkType linkType = factory.createLinkType();
                linkType.setHref(base);
                linkType.setHttpQuery("key="+KeyFactory.keyToString(placemark.getKey()));
                linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
                networkLinkType.setLink(linkType);
                JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                abstractFeatureGroup.add(networkLink);
                lastPlacemark = placemark;
            }
        }
        // write
        kmz.write(cos);

    }

    private void writeTrackSeqPath(String base, String styleUri, CacheOutputStream cos) throws IOException
    {
        DS ds = DS.get();
        Iterable<Entity> trackSeqIterator = ds.fetchTrackSeqs();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("TrackSeqPath");
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
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
                linkType.setHref(base);
                linkType.setHttpQuery("key="+KeyFactory.keyToString(trackSeq.getKey()));
                linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
                networkLinkType.setLink(linkType);
                JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                abstractFeatureGroup.add(networkLink);
            }
        }
        // write
        kmz.write(cos);

    }

    private void writeBlogLocationPath(String base, String styleUri, CacheOutputStream cos) throws IOException
    {
        DS ds = DS.get();
        Iterable<Entity> blogLocationIterator = ds.fetchBlogLocations();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("BlogLocationPath");
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // placemarks
        for (Entity blogLocation : blogLocationIterator)
        {
            GeoPt location = (GeoPt) blogLocation.getProperty(LocationProperty);
            // network link
            NetworkLinkType networkLinkType = factory.createNetworkLinkType();
            // region
            RegionType regionType = factory.createRegionType();
            networkLinkType.setRegion(regionType);
            // latlonalt
            LatLonAltBoxType latLonAltBoxType = factory.createLatLonAltBoxType();
            LatLonAltBox box = new LatLonAltBox(location, settings.getTrackMinimumDistance());
            box.populate(latLonAltBoxType);
            regionType.setLatLonAltBox(latLonAltBoxType);
            // lod
            LodType lodType = factory.createLodType();
            lodType.setMinLodPixels(10.0);
            lodType.setMaxLodPixels(-1.0);
            regionType.setLod(lodType);
            // link
            LinkType linkType = factory.createLinkType();
            linkType.setHref(base);
            linkType.setHttpQuery("key="+KeyFactory.keyToString(blogLocation.getKey()));
            linkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REGION);
            networkLinkType.setLink(linkType);
            JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
            abstractFeatureGroup.add(networkLink);
        }
        // write
        kmz.write(cos);

    }

    private void writeLookAt(URIBuilder builder, Entity lookAtEntity, CacheOutputStream cos) throws IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        KMZ kmz = new KMZ();
        ObjectFactory factory = kmz.getFactory();
        DocumentType documentType = factory.createDocumentType();
        documentType.setId("LookAt");
        JAXBElement<DocumentType> document = factory.createDocument(documentType);
        List<JAXBElement<? extends AbstractFeatureType>> abstractFeatureGroup = documentType.getAbstractFeatureGroup();
        kmz.getKml().getValue().setAbstractFeatureGroup(document);
        // lookAt
        GeoPt location = (GeoPt) lookAtEntity.getProperty(LocationProperty);
        LookAtType lookAtType = factory.createLookAtType();
        lookAtType.setLatitude((double)location.getLatitude());
        lookAtType.setLongitude((double)location.getLongitude());
        lookAtType.setRange(settings.getEyeAltitude());
        JAXBElement<LookAtType> lookAt = factory.createLookAt(lookAtType);
        documentType.setAbstractViewGroup(lookAt);
        
        // network links
        // PlacemarkPath
        NetworkLinkType placemarkNetworkLinkType = factory.createNetworkLinkType();
        LinkType placemarkLinkType = factory.createLinkType();
        placemarkLinkType.setHref(builder.setPath(PlacemarkPath).toString());
        placemarkLinkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REQUEST);
        placemarkLinkType.setViewFormat("");
        placemarkNetworkLinkType.setLink(placemarkLinkType);
        JAXBElement<NetworkLinkType> placemarkNetworkLink = factory.createNetworkLink(placemarkNetworkLinkType);
        abstractFeatureGroup.add(placemarkNetworkLink);
                
        // TrackSeqPath
        NetworkLinkType trackSeqNetworkLinkType = factory.createNetworkLinkType();
        LinkType trackSeqLinkType = factory.createLinkType();
        trackSeqLinkType.setHref(builder.setPath(TrackSeqPath).toString());
        trackSeqLinkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REQUEST);
        trackSeqLinkType.setViewFormat("");
        trackSeqNetworkLinkType.setLink(trackSeqLinkType);
        JAXBElement<NetworkLinkType> trackSeqNetworkLink = factory.createNetworkLink(trackSeqNetworkLinkType);
        abstractFeatureGroup.add(trackSeqNetworkLink);
                
        // BlogLocationPath
        NetworkLinkType blogLocationNetworkLinkType = factory.createNetworkLinkType();
        LinkType blogLocationLinkType = factory.createLinkType();
        blogLocationLinkType.setHref(builder.setPath(BlogLocationPath).toString());
        blogLocationLinkType.setViewRefreshMode(ViewRefreshModeEnumType.ON_REQUEST);
        blogLocationLinkType.setViewFormat("");
        blogLocationNetworkLinkType.setLink(blogLocationLinkType);
        JAXBElement<NetworkLinkType> blogLocationNetworkLink = factory.createNetworkLink(blogLocationNetworkLinkType);
        abstractFeatureGroup.add(blogLocationNetworkLink);
                
        // write
        kmz.write(cos);

    }

    private URIBuilder addNamespace(URIBuilder builder)
    {
        String namespace = NamespaceManager.get();
        if (builder.getUri().getHost().endsWith(namespace))
        {
            return builder;
        }
        else
        {
            return builder.setQuery(NamespaceParameter+"="+NamespaceManager.get());
        }
    }

    private int getAlpha(Date begin)
    {
        if (begin == null)
        {
            return 0;
        }
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        long x0 = ds.getTrackSeqsBegin().getTime();
        long xn = System.currentTimeMillis();
        int minOpaque = settings.getMinOpaque();
        double span = 255-minOpaque;
        double c = span/Math.sqrt(xn-x0);
        int age = (int) Math.round(c*Math.sqrt(xn-begin.getTime()));
        return 255-age;
    }

}
