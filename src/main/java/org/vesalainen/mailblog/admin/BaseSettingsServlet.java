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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS;
import org.vesalainen.mailblog.HttpException;

/**
 *
 * @author Timo Vesalainen
 */
public class BaseSettingsServlet extends FieldSettingsServlet
{

    public BaseSettingsServlet()
    {
        super(SettingsKind);
        addProperty(TitleProperty);
        addProperty(DescriptionProperty)
                .setAttribute("size", "100");
        addProperty(ImageProperty)
                .setType(Link.class)
                .setAttribute("size", "100")
                .setTooltip("Link to blog image");
        addProperty(PublishImmediatelyProperty)
                .setType(Boolean.class);
        addProperty(LocaleProperty)
                .setType(Locale.class)
                .setMandatory();
        addProperty(TimeZoneProperty)
                .setType(TimeZone.class)
                .setMandatory();
        addProperty(PicMaxHeightProperty)
                .setType(Long.class)
                .setMandatory();
        addProperty(PicMaxWidthProperty)
                .setType(Long.class)
                .setMandatory();
        addProperty(FixPicProperty)
                .setType(Boolean.class);
        addProperty(CommonPlacemarksProperty)
                .setType(Boolean.class)
                .setTooltip("If true the placemarks and tracks are stored in empty namespace");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn())
        {
            if (userService.isUserAdmin())
            {
                super.doGet(req, resp);
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
                super.doPost(req, resp);
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