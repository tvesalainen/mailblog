/*
 * Copyright (C) 2012 Timo Vesalainen
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
package org.vesalainen.mailblog.admin;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.awt.Color;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen
 */
public class CommonSettingsServlet extends FieldSettingsServlet
{

    public CommonSettingsServlet()
    {
        super(SettingsKind);
        addProperty(CommonPlacemarksProperty)
                .setType(Boolean.class)
                .setTooltip("If true the placemarks and tracks are stored in empty namespace");
        addProperty(TrackColorProperty)
                .setType(Color.class)
                .setTooltip("Color used for track");
        addProperty(MinOpaqueProperty)
                .setType(Long.class)
                .setAttribute("min", "0")
                .setAttribute("max", "255")
                .setTooltip("Minimum opaque for ageing track");
        addProperty(BlogIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show blogs place");
        addProperty(BlogIconPpmProperty)
                .setType(Long.class)
                .setAttribute("min", "0")
                .setTooltip("Minimum pixels per mile for blog icon");
        addProperty(ImageIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show image ");
        addProperty(ImageIconPpmProperty)
                .setType(Long.class)
                .setAttribute("min", "0")
                .setTooltip("Minimum pixels per mile for image icon");
        addProperty(HiLiteIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show when point in time is used");
        addProperty(HiLiteIconPpmProperty)
                .setType(Long.class)
                .setAttribute("min", "0")
                .setTooltip("Minimum pixels per mile for hilite icon");
        addProperty(AnchoredIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show a place where Spot Ok button was pressed");
        addProperty(WaypointIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show a place where Spot Custom button was pressed");
        addProperty(DestinationIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show a place where Spot Help button was pressed");
        addProperty(PlacemarkIconPpmProperty)
                .setType(Long.class)
                .setAttribute("min", "0")
                .setTooltip("Minimum pixels per mile for placemark icon");
        addProperty(TrackBearingToleranceProperty)
                .setType(Double.class)
                .setTooltip("If bearing changes less than given degrees, no new trackpoint is created");
        addProperty(TrackMinDistanceProperty)
                .setType(Double.class)
                .setTooltip("Minimum distance for two trackpoints in NM");
        addProperty(TrackMaxSpeedProperty)
                .setType(Double.class)
                .setTooltip("Maximum speed for vessel in knots");
        addProperty(EyeAltitudeProperty)
                .setType(Double.class)
                .setTooltip("Eye altitude in meters in Google Earth lookat");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn())
        {
            if (userService.isUserAdmin())
            {
                String orig = NamespaceManager.get();
                try
                {
                    NamespaceManager.set(null);
                    super.doGet(req, resp);
                }
                finally
                {
                    NamespaceManager.set(orig);
                }
            }
            else
            {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        else
        {
            String loginURL = userService.createLoginURL("");
            resp.getWriter().write(loginURL);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn())
        {
            if (userService.isUserAdmin())
            {
                String orig = NamespaceManager.get();
                try
                {
                    NamespaceManager.set(null);
                    super.doPost(req, resp);
                }
                finally
                {
                    NamespaceManager.set(orig);
                }
            }
            else
            {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        else
        {
            String loginURL = userService.createLoginURL("");
            resp.getWriter().write(loginURL);
        }
    }

    @Override
    protected String getTitle(Entity entity)
    {
        return BaseKey;
    }

}
