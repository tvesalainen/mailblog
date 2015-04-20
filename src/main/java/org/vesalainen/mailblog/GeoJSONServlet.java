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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author tkv
 */
public class GeoJSONServlet extends BaseServlet
{
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        RunInNamespace<Boolean> checkETag = new RunInNamespace() 
        {
            @Override
            protected Boolean run()
            {
                try
                {
                    DS ds = DS.get();
                    return ds.sameETag(request, response);
                }
                catch (IOException ex)
                {
                    return false;
                }
            }
        };
        if (checkETag.doIt(null, settings.isCommonPlacemarks()))
        {
            return;
        }
        if (!ds.serveFromCache(request, response))
        {
            RunInNamespace<String> getETag = new RunInNamespace() 
            {
                @Override
                protected String run()
                {
                    DS ds = DS.get();
                    return ds.getETag();
                }
            };
            String eTag = getETag.doIt(null, settings.isCommonPlacemarks());
            String bboxStr = request.getParameter(BoundingBoxParameter);
            if (bboxStr != null)
            {
                BoundingBox bb = BoundingBox.getSouthWestNorthEastInstance(bboxStr);
                try (DS.CacheWriter cw = ds.createCacheWriter(request, response))
                {
                    cw.setETag(eTag)
                    .setContentType("application/json");
                    ds.writeRegionKeys(cw, bb);
                }
            }
            else
            {
                String keyStr = request.getParameter(KeyParameter);
                if (keyStr != null)
                {
                    Key key = KeyFactory.stringToKey(keyStr);
                    log(key.toString());
                    try (DS.CacheWriter cw = ds.createCacheWriter(request, response))
                    {
                        cw.setETag(eTag)
                        .setContentType("application/json")
                        .setMaxAge(86400);
                        ds.writeFeature(cw, key);
                    }
                }
                else
                {
                    String heightStr = request.getParameter(HeightParameter);
                    String widthStr = request.getParameter(WidthParameter);
                    if (heightStr != null && widthStr != null)
                    {
                        int height = Integer.parseInt(heightStr);
                        int width = Integer.parseInt(widthStr);
                        try (DS.CacheWriter cw = ds.createCacheWriter(request, response))
                        {
                            cw.setETag(eTag)
                            .setContentType("application/json");
                            ds.writeMapInit(cw, height, width);
                        }
                    }
                }
            }
        }
    }    
}
