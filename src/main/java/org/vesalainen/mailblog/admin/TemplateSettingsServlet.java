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
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen
 */
public class TemplateSettingsServlet extends EntityServlet
{
    private static final String Help = "Following expressions are available:\n"+
        "${Subject} Blog subject\n"+
        "${Date} Blog date\n"+
        "${Sender} Sender\n"+
        "${Blog} Blog content\n"+
        "${Url} Direct blog Url\n"+
        "${Id} Blog datastore id as string\n"+
        "${Location} Coordinates\n";

    public TemplateSettingsServlet()
    {
        super(SettingsKind);
        addProperty(BlogTemplateProperty)
                .setType(Text.class)
                .setAttribute("rows", "20")
                .setAttribute("cols", "80")
                .setMandatory()
                .setTooltip(Help);
        addProperty(CommentTemplateProperty)
                .setType(Text.class)
                .setAttribute("rows", "15")
                .setAttribute("cols", "80")
                .setMandatory()
                .setTooltip(Help);
        addProperty(SearchResultsTemplateProperty)
                .setType(Text.class)
                .setAttribute("rows", "15")
                .setAttribute("cols", "80")
                .setMandatory()
                .setTooltip(Help);
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
