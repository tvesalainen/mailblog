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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.KeyFactory;
import java.time.ZonedDateTime;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.mailblog.BlogConstants.PlacemarkKind;
import static org.vesalainen.mailblog.CachingDatastoreService.getRootKey;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BodyPropertyHandlerTest extends DSHelper
{
    
    public BodyPropertyHandlerTest()
    {
    }

    @Test
    public void test1() throws EntityNotFoundException
    {
        BodyPropertyHandler bph = new BodyPropertyHandler();
        ZonedDateTime zdt = ZonedDateTime.parse("2018-04-21T20:06:27Z");
        Date date = Date.from(zdt.toInstant());
        String replaced = bph.replace("a${dateTime=2018-04-21 20.06.27Z}b${location=08-28,99N 079-56,65W}c${messenger=Iiris}d${type=Custom}e");
        assertEquals("abcde", replaced);
        DS ds = DS.get();
        Entity placemark = ds.get(KeyFactory.createKey(getRootKey(), PlacemarkKind, date.getTime()));
        assertNotNull(placemark);
        assertEquals("Iiris", placemark.getProperty("Title"));
        GeoPt pt = new GeoPt(8.483167F,-79.944168F);
        assertEquals(pt, placemark.getProperty("Location"));
    }
    
}
