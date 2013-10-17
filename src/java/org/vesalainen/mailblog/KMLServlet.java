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
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
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
import net.opengis.kml.RefreshModeEnumType;
import net.opengis.kml.RegionType;
import net.opengis.kml.StyleType;
import net.opengis.kml.ViewRefreshModeEnumType;
import org.vesalainen.kml.KML;
import org.vesalainen.kml.KMZ;
import static org.vesalainen.mailblog.BlogConstants.LocationProperty;
import static org.vesalainen.mailblog.BlogConstants.NamespaceParameter;
import org.vesalainen.mailblog.DS.CacheOutputStream;
import org.vesalainen.mailblog.DS.CacheWriter;

/**
 * @author Timo Vesalainen
 */
public class KMLServlet extends HttpServlet implements BlogConstants
{
    private static final String PathStyleId = "path-style";
    private static final double OverAllMin = 1000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        DS ds = DS.get();
        URL base = getBase(request);
        if (!ds.sameETagOrCached(request, response))
        {
            String keyString = request.getParameter("key");
            if (keyString != null)
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response, "application/vnd.google-earth.kmz", "utf-8", false))
                {
                    log("updateKml");
                    cos.cache();
                }
            }
            else
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response, "application/vnd.google-earth.kmz", "utf-8", false))
                {
                    writeRegions(request, cos);
                    log("networkLink");
                    cos.cache();
                }
            }
        }
    }

    public void writeRegions(HttpServletRequest request, CacheOutputStream out) throws IOException
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
        // lod
        LodType overallLodType = factory.createLodType();
        overallLodType.setMinLodPixels(-1.0);
        overallLodType.setMaxLodPixels(OverAllMin);
        overallRegionType.setLod(overallLodType);
        // placemarks
        Entity lastPlacemark = null;
        LatLonAltBox overallBox = new LatLonAltBox();
        for (Entity placemark : ds.fetchPlacemarks())
        {
            GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
            if (location != null)
            {
                overallBox.add(location);
                overallCoordinates.add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
                // network link
                NetworkLinkType networkLinkType = factory.createNetworkLinkType();
                // region
                RegionType regionType = factory.createRegionType();
                networkLinkType.setRegion(regionType);
                // latlonalt
                LatLonAltBoxType latLonAltBoxType = factory.createLatLonAltBoxType();
                latLonAltBoxType.setNorth((double)location.getLatitude()+0.001);
                latLonAltBoxType.setSouth((double)location.getLatitude());
                latLonAltBoxType.setWest((double)location.getLongitude());
                latLonAltBoxType.setEast((double)location.getLongitude()+0.001);
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
                lastPlacemark = placemark;
            }
        }
        if (lastPlacemark != null)
        {
            overallBox.populate(overallLatLonAltBoxType);
            // lookAt
            GeoPt location = (GeoPt) lastPlacemark.getProperty(LocationProperty);
            LookAtType lookAtType = factory.createLookAtType();
            lookAtType.setLatitude((double)location.getLatitude());
            lookAtType.setLongitude((double)location.getLongitude());
            lookAtType.setRange(2000.0);
            JAXBElement<LookAtType> lookAt = factory.createLookAt(lookAtType);
            documentType.setAbstractViewGroup(lookAt);
        }
        // write
        kmz.write(out);

    }
    private void setStyles(DocumentType documentType, ObjectFactory factory, Settings settings)
    {
        List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroup = documentType.getAbstractStyleSelectorGroup();
        // line style
        StyleType pathStyleType = factory.createStyleType();
        pathStyleType.setId(PathStyleId);
        LineStyleType lineStyleType = factory.createLineStyleType();
        lineStyleType.setColor(new byte[] {(byte)255, 0, 0, (byte)255});
        pathStyleType.setLineStyle(lineStyleType);
        JAXBElement<StyleType> pathStyle = factory.createStyle(pathStyleType);
        abstractStyleSelectorGroup.add(pathStyle);
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
