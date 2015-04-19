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
package org.vesalainen.mailblog;

import java.io.IOException;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author tkv
 */
public class ResourceEditServlet extends EntityServlet
{

    public ResourceEditServlet()
    {
        super(ResourceKind);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        clearProperties();
        DS ds = DS.get();
        Resources resources = ds.getResources();
        for (Entry<String,Object> entry : resources.getMap().entrySet())
        {
            addProperty(entry.getKey())
                    .setType(entry.getValue().getClass());
        }
        super.doGet(req, resp);
    }
    
}
