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
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tkv
 */
public class CronServlet extends BaseServlet
{

    @Override
    protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final DS ds = DS.get();
        resp.setContentType("text/plain");
        final PrintWriter pw = resp.getWriter();
        for (String namespace : ds.getNamespaceList())
        {
            RunInNamespace rin = new RunInNamespace() {
                @Override
                protected Object run()
                {
                    if ("populateTrack".equals(req.getQueryString()))
                    {
                        ds.populateTrack(pw);
                    }
                    if ("connectPics".equals(req.getQueryString()))
                    {
                        ds.connectPictures(pw);
                    }
                    return null;
                }
            };
            rin.doIt(namespace);
        }
        pw.close();
    }

    
}
