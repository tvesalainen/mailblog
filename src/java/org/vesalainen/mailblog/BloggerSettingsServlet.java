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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Timo Vesalainen
 */
public class BloggerSettingsServlet extends SettingsServlet implements BlogConstants
{

    public BloggerSettingsServlet()
    {
        super(SettingsKind);
        addProperty(NicknameProperty)
                .setMandatory();
        addProperty(PublishImmediatelyProperty)
                .setType(Boolean.class);
        addProperty(BlogAreaTemplateProperty)
                .setType(Text.class)
                .setAttribute("rows", "10")
                .setAttribute("cols", "80");
        addProperty(BlogTemplateProperty)
                .setType(Text.class)
                .setAttribute("rows", "10")
                .setAttribute("cols", "80");
        addProperty(CommentTemplateProperty)
                .setType(Text.class)
                .setAttribute("rows", "6")
                .setAttribute("cols", "80");
        addProperty(PicMaxHeightProperty)
                .setType(Long.class);
        addProperty(PicMaxWidthProperty)
                .setType(Long.class);
        addProperty(FixPicProperty)
                .setType(Boolean.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn())
        {
            super.doGet(req, resp);
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
            super.doPost(req, resp);
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
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        Key baseKey = KeyFactory.createKey(DS.getRootKey(), kind, BaseKey);
        Key key = KeyFactory.createKey(baseKey, kind, user.getEmail());
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
        Email email = (Email) entity.getProperty("Email");
        if (email == null)
        {
            throw new IllegalArgumentException("Email not found");
        }
        return email.getEmail();
    }

}
