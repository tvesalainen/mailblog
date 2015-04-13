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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.vesalainen.gpx.GPX;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.exif.ExifParserTest;
import org.vesalainen.mailblog.types.TimeSpan;

/**
 *
 * @author tkv
 */
public class OpenCPNTrackHandlerTest
{
    private final LocalServiceTestHelper helper = 
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));
    
    public OpenCPNTrackHandlerTest()
    {
    }

    @Before
    public void setUp()
    {
        helper.setUp();
    }

    @After
    public void tearDown()
    {
        helper.tearDown();
    }

    @Test
    public void test()
    {
            try (InputStream is = ExifParserTest.class.getClassLoader().getResourceAsStream("lasgalletes-mogan.gpx"))
            {
                GPX gpx = new GPX(is);
                DS ds = DS.get();
                OpenCPNTrackHandler th = new OpenCPNTrackHandler(ds);
                gpx.browse(5, 0.1, th);
                Query q1 = new Query(TrackKind);
                PreparedQuery p1 = ds.prepare(q1);
                for (Entity track : p1.asIterable())
                {
                    BoundingBox bb1 = new BoundingBox(track);
                    TimeSpan ts1 = new TimeSpan(track);
                    Query q2 = new Query(TrackSeqKind);
                    q2.setAncestor(track.getKey());
                    PreparedQuery p2 = ds.prepare(q2);
                    for (Entity trackSeq : p2.asIterable())
                    {
                        BoundingBox bb2 = new BoundingBox(trackSeq);
                        assertTrue(bb1.isInside(bb2));
                        TimeSpan ts2 = new TimeSpan(trackSeq);
                        assertTrue(ts1.isInside(ts2));
                        Query q3 = new Query(TrackPointKind);
                        q3.setAncestor(trackSeq.getKey());
                        PreparedQuery p3 = ds.prepare(q3);
                        for (Entity trackPoint : p3.asIterable())
                        {
                            long time = trackPoint.getKey().getId();
                            assertTrue(ts2.isInside(time));
                            GeoPt location = (GeoPt) trackPoint.getProperty(LocationProperty);
                            assertTrue(bb2.isInside(location));
                        }
                    }
                }
            }
            catch (IOException | JAXBException ex)
            {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
    }

}
