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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
public class BloggerSettingsServlet extends EntityServlet implements BlogConstants
{

    public BloggerSettingsServlet()
    {
        super("Settings");
        addProperty("Email", Email.class, true);
        addProperty("Nickname", String.class, false);
        addProperty("ConfirmEmail", Boolean.class, false);
        addProperty("ShowCount", Long.class, false);
        addProperty("Template", Text.class, false);
        addProperty("Language", String.class, false);
        addProperty("PicMaxHeight", Long.class, false);
        addProperty("PicMaxWidth", Long.class, false);
        addProperty("FixPic", Boolean.class, false);
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
    protected Key createKey(HttpServletRequest req)
    {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        Key baseKey = KeyFactory.createKey(kind, BaseKey);
        return KeyFactory.createKey(baseKey, kind, user.getEmail());
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
