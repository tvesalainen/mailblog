/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoDataT extends RemoteHelper
{

    
    public GeoDataT()
    {
    }
    @Test
    public void testSomeMethod() throws IOException
    {
        GeoData d = new GeoData();
        GeoPtBoundingBox bb = GeoPtBoundingBox.getSouthWestNorthEastInstance("6.938551,-81.19235,10.197208,-78.549039");
        JSONObject regionKeys = d.regionKeys(bb);
        try (Writer w = new OutputStreamWriter(System.err))
        {
            regionKeys.write(w);
        }
    }
    
}
