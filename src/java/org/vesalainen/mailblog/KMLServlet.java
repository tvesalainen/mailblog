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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import net.opengis.kml.DocumentType;
import net.opengis.kml.LinkType;
import net.opengis.kml.LookAtType;
import net.opengis.kml.NetworkLinkType;
import net.opengis.kml.ObjectFactory;
import net.opengis.kml.RefreshModeEnumType;
import net.opengis.kml.ViewRefreshModeEnumType;
import org.vesalainen.kml.KML;
import static org.vesalainen.mailblog.BlogConstants.LocationProperty;
import static org.vesalainen.mailblog.BlogConstants.NamespaceParameter;
import org.vesalainen.mailblog.DS.CacheOutputStream;
import org.vesalainen.mailblog.DS.CacheWriter;

/**
 * @author Timo Vesalainen
 */
public class KMLServlet extends HttpServlet implements BlogConstants
{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        DS ds = DS.get();
        URL base = getBase(request);
        if (!ds.serveFromCache(request, response))
        {
            MaidenheadLocator2[] bb = MaidenheadLocator2.getBoundingBox(request);
            if (bb != null)
            {
                try (CacheOutputStream cos = ds.createCacheOutputStream(request, response, "application/vnd.google-earth.kmz", "utf-8", false))
                {
                    log("updateKml");
                    ds.updateKml(bb, base, cos);
                    cos.cache();
                }
            }
            else
            {
                try (CacheWriter cw = ds.createCacheWriter(request, response, "application/vnd.google-earth.kml+xml", "utf-8", false))
                {
                    KML kml = new KML();
                    ObjectFactory factory = kml.getFactory();
                    DocumentType documentType = factory.createDocumentType();
                    JAXBElement<DocumentType> document = factory.createDocument(documentType);
                    NetworkLinkType networkLinkType = factory.createNetworkLinkType();
                    JAXBElement<NetworkLinkType> networkLink = factory.createNetworkLink(networkLinkType);
                    documentType.getAbstractFeatureGroup().add(networkLink);
                    networkLinkType.setId("network-link");
                    networkLinkType.setFlyToView(Boolean.TRUE);
                    networkLinkType.setRefreshVisibility(Boolean.FALSE);
                    // link
                    LinkType link = factory.createLinkType();
                    link.setRefreshMode(RefreshModeEnumType.ON_CHANGE);
                    link.setViewRefreshMode(ViewRefreshModeEnumType.ON_STOP);
                    link.setHref(getRequestUrl(request));
                    link.setViewFormat(BoundingBoxParameter+"=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]");
                    networkLinkType.setLink(link);
                    // lookAt
                    Settings settings = ds.getSettings();
                    Entity lastPlacemark = ds.fetchLastPlacemark(settings);
                    GeoPt location = (GeoPt) lastPlacemark.getProperty(LocationProperty);
                    LookAtType lookAtType = factory.createLookAtType();
                    lookAtType.setLatitude((double)location.getLatitude());
                    lookAtType.setLongitude((double)location.getLongitude());
                    lookAtType.setRange(2000.0);
                    JAXBElement<LookAtType> lookAt = factory.createLookAt(lookAtType);
                    documentType.setAbstractViewGroup(lookAt);
                    kml.set(document);
                    kml.write(cw);
                    log("networkLink");
                    cw.cache();
                }
            }
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
