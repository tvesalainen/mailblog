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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class DSTest extends DSHelper
{
    
    public DSTest()
    {
    }

    @Test
    public void testDistance()
    {
        GeoPt l1 = new GeoPt(60, 25);
        GeoPt l2 = new GeoPt(59, 25);
        GeoPt l3 = new GeoPt(60, 24);
        assertEquals(1, DS.getDegreesDistance(l1, l2), Epsilon);
        assertEquals(0.5, DS.getDegreesDistance(l1, l3), Epsilon);
    }
    
}
