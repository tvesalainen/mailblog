package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import static org.vesalainen.mailblog.BlogConstants.*;

/*
 * Copyright (C) 2015 tkv
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
/**
 *
 * @author tkv
 */
public class ResourceServlet extends BaseServlet
{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        DS ds = DS.get();
        if (!ds.sameETagOrCached(request, response))
        {
            try (DS.CacheWriter cw = ds.createCacheWriter(request, response))
            {
                cw.setContentType("application/json");
                JSONObject json = new JSONObject();
                Resources resources = ds.getResources();
                for (Map.Entry<String, Object> entry : resources.getMap().entrySet())
                {
                    Object value = entry.getValue();
                    if (value != null)
                    {
                        if (value instanceof Text)
                        {
                            Text text = (Text) value;
                            value = text.getValue();
                        }
                        json.put(entry.getKey(), value);
                    }
                }
                json.write(cw);
                cw.cache();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn() && userService.isUserAdmin())
        {
            final String id = request.getParameter(IdParameter);
            final String type = request.getParameter(TypeParameter);
            final String text = request.getParameter(TextParameter);
            log(id + " " + type + " " + text);
            if (id != null && type != null && text != null)
            {
                DS ds = DS.get();
                ds.addResource(id, type, text);
                ds.clearCache(request);
            }
        }
    }

}
