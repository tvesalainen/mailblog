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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class LatLonAltBoxTest
{
    private static final double Epsilon = 1e-6;
    public LatLonAltBoxTest()
    {
    }
    
    @Test
    public void testInit()
    {
        float north = 1.23F;
        float east = 2.34F;
        float south = 3.45F;
        float west = 4.56F;
        GeoPt northEast = new GeoPt(north, east);
        GeoPt southWest = new GeoPt(south, west);
        LatLonAltBox box1 = new LatLonAltBox(northEast, southWest);
        assertEquals(north, box1.getNorth(), Epsilon);
        assertEquals(east, box1.getEast(), Epsilon);
        assertEquals(south, box1.getSouth(), Epsilon);
        assertEquals(west, box1.getWest(), Epsilon);
        assertEquals(northEast, box1.getNorthEast());
        assertEquals(southWest, box1.getSouthWest());
        LatLonAltBox box2 = new LatLonAltBox(north, east, south, west);
        assertEquals(north, box2.getNorth(), Epsilon);
        assertEquals(east, box2.getEast(), Epsilon);
        assertEquals(south, box2.getSouth(), Epsilon);
        assertEquals(west, box2.getWest(), Epsilon);
        LatLonAltBox box3 = LatLonAltBox.getSouthWestNorthEastInstance(String.format(Locale.US, "%f,%f,%f,%f", south, west, north, east));
        assertEquals(north, box3.getNorth(), Epsilon);
        assertEquals(east, box3.getEast(), Epsilon);
        assertEquals(south, box3.getSouth(), Epsilon);
        assertEquals(west, box3.getWest(), Epsilon);
    }
    @Test
    public void testSerialize()
    {
        LatLonAltBox box = new LatLonAltBox();
        box.add(1, 2);
        box.add(3, 4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oas = new ObjectOutputStream(baos))
        {
            oas.writeObject(box);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais))
        {
            Object ob = ois.readObject();
            assertTrue(ob instanceof LatLonAltBox);
            LatLonAltBox box2 = (LatLonAltBox) ob;
            assertEquals(box.getNorth(), box2.getNorth(), Epsilon);
            assertEquals(box.getEast(), box2.getEast(), Epsilon);
            assertEquals(box.getSouth(), box2.getSouth(), Epsilon);
            assertEquals(box.getWest(), box2.getWest(), Epsilon);
        }
        catch (IOException | ClassNotFoundException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void testIsInside1()
    {
        LatLonAltBox box = new LatLonAltBox();
        box.add(10, 10);
        box.add(20, 20);
        assertTrue(box.isInside(15, 15));
        assertTrue(box.isInside(10, 20));
        assertTrue(box.isInside(20, 10));
        assertFalse(box.isInside(9, 10));
        assertFalse(box.isInside(10, 9));
        assertFalse(box.isInside(20.001, 20));
    }
    @Test
    public void testIsInside2()
    {
        LatLonAltBox box = new LatLonAltBox();
        box.add(10, -179);
        box.add(20, 179);
        assertTrue(box.isInside(15, -179.001));
        assertTrue(box.isInside(10, 179.004));
    }
    @Test
    public void testIsIntersection1()
    {
        LatLonAltBox box1 = new LatLonAltBox();
        box1.add(10, 10);
        box1.add(20, 20);
        LatLonAltBox box2 = new LatLonAltBox();
        box2.add(15, 15);
        box2.add(20, 20);
        assertTrue(box1.isIntersecting(box2));
    }
    @Test
    public void testIsIntersection2()
    {
        LatLonAltBox box1 = new LatLonAltBox();
        box1.add(10, 10);
        box1.add(20, 20);
        LatLonAltBox box2 = new LatLonAltBox();
        box2.add(21, 21);
        box2.add(23, 22);
        assertFalse(box1.isIntersecting(box2));
    }
    @Test
    public void testIsIntersection3()
    {
        LatLonAltBox box1 = new LatLonAltBox();
        box1.add(10, 10);
        box1.add(20, 20);
        LatLonAltBox box2 = new LatLonAltBox();
        box2.add(11, 9);
        box2.add(19, 22);
        assertTrue(box1.isIntersecting(box2));
    }
    @Test
    public void testIsIntersection4()
    {
        LatLonAltBox box1 = new LatLonAltBox();
        box1.add(10, 10);
        box1.add(20, 20);
        LatLonAltBox box2 = new LatLonAltBox();
        box2.add(9, 9);
        box2.add(21, 22);
        assertTrue(box1.isIntersecting(box2));
    }

}
