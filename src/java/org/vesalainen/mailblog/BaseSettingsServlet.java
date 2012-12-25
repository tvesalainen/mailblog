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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
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
public class BaseSettingsServlet extends EntityServlet implements BlogConstants
{

    public BaseSettingsServlet()
    {
        super("Settings");
        addProperty("ConfirmEmail", Boolean.class, false);
        addProperty("ShowCount", Long.class, false, true);
        addProperty("Template", Text.class, false, true);
        addProperty("Language", String.class, false, true);
        addProperty("PicMaxHeight", Long.class, false, true);
        addProperty("PicMaxWidth", Long.class, false, true);
        addProperty("FixPic", Boolean.class, false);
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
    protected Key createKey(HttpServletRequest req)
    {
        return KeyFactory.createKey(kind, BaseKey);
    }

    @Override
    protected Entity getEntity(Key key) throws HttpException
    {
        try
        {
            return super.getEntity(key);
        }
        catch (HttpException ex)
        {
            if (ex.getStatusCode() == HttpServletResponse.SC_NOT_FOUND)
            {
                return new Entity(key);
            }
            else
            {
                throw ex;
            }
        }
    }

    @Override
    protected String getTitle(Entity entity)
    {
        return BaseKey;
    }

}
