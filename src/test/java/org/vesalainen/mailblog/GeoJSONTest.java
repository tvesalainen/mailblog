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

import com.google.appengine.api.datastore.GeoPt;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.mailblog.GeoJSON.FeatureCollection;

/**
 *
 * @author tkv
 */
public class GeoJSONTest
{
    
    public GeoJSONTest()
    {
    }

    @Test
    public void test0()
    {
        StringWriter sw = new StringWriter();
        FeatureCollection fc = new FeatureCollection();
        assertEquals("FeatureCollection", fc.getJson().get("type"));
        fc.addPoint(new GeoPt(27.49F, 15.45F));
        fc.write(sw);
    }
    
}
