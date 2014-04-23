/*
 * Copyright (C) 2012 Timo Vesalainen
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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
public class CommonSettingsServlet extends EntityServlet
{

    public CommonSettingsServlet()
    {
        super(SettingsKind);
        addProperty(CommonPlacemarksProperty)
                .setType(Boolean.class)
                .setTooltip("If true the placemarks and tracks are stored in empty namespace");
        addProperty(PathColorProperty)
                .setType(Color.class)
                .setTooltip("Color used for path between placemarks");
        addProperty(TrackColorProperty)
                .setType(Color.class)
                .setTooltip("Color used for track");
        addProperty(BlogIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show blogs place (GoogleEarth)");
        addProperty(ImageIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show image (GoogleEarth)");
        addProperty(HiLiteIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show when point in time is used (GoogleEarth)");
        addProperty(SpotOkIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show a place where Spot Ok button was pressed (GoogleEarth)");
        addProperty(SpotCustomIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show a place where Spot Custom button was pressed (GoogleEarth)");
        addProperty(SpotHelpIconProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Used to show a place where Spot Help button was pressed (GoogleEarth)");
        addProperty(TrackBearingToleranceProperty)
                .setType(Double.class)
                .setTooltip("If bearing changes less than given degrees, no new trackpoint is created");
        addProperty(TrackMinDistanceProperty)
                .setType(Double.class)
                .setTooltip("Minimum distance for two trackpoints");
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
    protected Key getKey(HttpServletRequest req) throws HttpException
    {
        DS ds = DS.get();
        Key key = KeyFactory.createKey(DS.getRootKey(), kind, BaseKey);
        String keyString = req.getParameter(Key);
        if (keyString != null)
        {
            Key requestKey = KeyFactory.stringToKey(keyString);
            if (!key.equals(requestKey))
            {
                throw new HttpException(HttpServletResponse.SC_CONFLICT, key+" and request key "+requestKey+" differs");
            }
        }
        return key;
    }

    @Override
    protected String getTitle(Entity entity)
    {
        return BaseKey;
    }

}
