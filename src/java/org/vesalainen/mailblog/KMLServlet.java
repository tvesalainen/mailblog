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
import net.opengis.kml.LinkType;
import net.opengis.kml.NetworkLinkType;
import net.opengis.kml.RefreshModeEnumType;
import net.opengis.kml.ViewRefreshModeEnumType;
import org.vesalainen.kml.KML;

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
            String bbox = request.getParameter(BoundingBoxParameter);
            if (bbox != null)
            {
                String[] ss = bbox.split(",");
                float west = Float.parseFloat(ss[0]);
                float south = Float.parseFloat(ss[1]);
                float east = Float.parseFloat(ss[2]);
                float north = Float.parseFloat(ss[3]);
                response.setContentType("application/vnd.google-earth.kmz");
                response.setCharacterEncoding("utf-8");
                ds.updateKml(west, south, east, north, base, response.getOutputStream());
            }
            else
            {
                DS.CacheWriter cw = ds.createCacheWriter(request, response, "application/vnd.google-earth.kml+xml", "utf-8", false);
                KML kml = new KML();
                JAXBElement<NetworkLinkType> networkLink = kml.createNetworkLink();
                networkLink.getValue().setRefreshVisibility(Boolean.TRUE);
                networkLink.getValue().setFlyToView(Boolean.TRUE);
                LinkType link = kml.createLink();
                link.setRefreshMode(RefreshModeEnumType.ON_CHANGE);
                link.setViewRefreshMode(ViewRefreshModeEnumType.ON_STOP);
                link.setHref(request.getRequestURL().toString());
                link.setViewFormat(BoundingBoxParameter+"=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]");
                networkLink.getValue().setLink(link);
                kml.set(networkLink);
                kml.write(cw);
                cw.ready();
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

}
