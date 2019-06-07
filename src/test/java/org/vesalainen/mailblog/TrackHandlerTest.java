/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.gpx.GPX;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.exif.ExifException;
import org.vesalainen.mailblog.exif.ExifParser;
import org.vesalainen.mailblog.exif.ExifParserTest;
import org.vesalainen.mailblog.types.TimeSpan;
import org.vesalainen.nmea.util.TrackInput;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrackHandlerTest extends DSHelper
{
    
    public TrackHandlerTest()
    {
    }


    @Test
    public void testOpenCPN()
    {
        try (InputStream is = ExifParserTest.class.getClassLoader().getResourceAsStream("laspalmas-lasgalletas.gpx"))
        {
            GPX gpx = new GPX(is);
            DS ds = DS.get();
            OpenCPNTrackHandler th = new OpenCPNTrackHandler(ds);
            gpx.browse(1, 0.1, th);
            testDS(ds);
        }
        catch (ParseException | IOException | JAXBException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testCompressed()
    {
        try (InputStream is = ExifParserTest.class.getClassLoader().getResourceAsStream("20150301171540.trc"))
        {
            TrackInput trackInput = new TrackInput(is);
            DS ds = DS.get();
            CompressedTrackHandler cth = new CompressedTrackHandler(ds);
            cth.handle(trackInput);
            testDS(ds);
        }
        catch (ParseException | IOException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    private void testDS(DS ds) throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601Format);
        Date expBegin = sdf.parse("2015-03-01T17:15:40Z");
        Date expEnd = sdf.parse("2015-03-02T13:35:07Z");
        Query q1 = new Query(TrackKind);
        PreparedQuery p1 = ds.prepare(q1);
        for (Entity track : p1.asIterable())
        {
            GeoPtBoundingBox bb1 = GeoPtBoundingBox.getInstance(track);
            TimeSpan ts1 = new TimeSpan(track);
            assertEquals(expBegin, ts1.getBegin());
            assertEquals(expEnd, ts1.getEnd());
            Query q2 = new Query(TrackSeqKind);
            q2.setAncestor(track.getKey());
            PreparedQuery p2 = ds.prepare(q2);
            for (Entity trackSeq : p2.asIterable())
            {
                GeoPtBoundingBox bb2 = GeoPtBoundingBox.getInstance(trackSeq);
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
    @Test
    public void testLocatePics()
    {
        DS ds = DS.get();
        Key settingsKey = ds.createSettingsKey();
        Entity settings = new Entity(settingsKey);
        ds.put(settings);
        
        try (InputStream is = ExifParserTest.class.getClassLoader().getResourceAsStream("laspalmas-lasgalletas.gpx"))
        {
            GPX gpx = new GPX(is);
            OpenCPNTrackHandler th = new OpenCPNTrackHandler(ds);
            gpx.browse(1, 0.1, th);
        }
        catch (IOException | JAXBException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        
        Date now = new Date();
        assertEquals(255, ds.getAlpha(now));

        try
        {
            List<Key> keyList = new ArrayList<>();
            for (String img : new String[] {"IMGP1142.JPG", "IMGP1143.JPG"})
            {
                URL url = ExifParserTest.class.getClassLoader().getResource(img);
                Path path = Paths.get(url.toURI());
                File file = path.toFile();
                FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
                MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                ExifParser parser = new ExifParser(mbb);
                Key metadataKey = ds.getMetadataKey(img);
                Entity metadata = new Entity(metadataKey);
                metadata.setProperty(LocationProperty, null);
                parser.populate(metadata);
                ds.put(metadata);
                keyList.add(metadataKey);
            }
            
            ds.connectPictures(new PrintWriter(System.err));
            //<trkpt lat="28.018356167" lon="-16.543942167">
                //<time>2015-03-02T12:08:09Z</time>
            //</trkpt>
            double dLat = 28.017381333 - 28.018356167;
            double dLon = -16.548457333 - -16.543942167;
            // IMGP1142.JPG
            Key metadataKey = keyList.get(0);
            Entity metadata = ds.get(metadataKey);
            assertNotNull(metadata);
            GeoPt location = (GeoPt) metadata.getProperty(LocationProperty);
            assertNotNull(location);
            double dSec = 19;
            double tSec = 193;
            double c = dSec/tSec;
            double expLat = 28.018356167 + c*dLat;
            double expLon = -16.543942167 + c*dLon;
            assertEquals(expLat, location.getLatitude(), Epsilon);
            assertEquals(expLon, location.getLongitude(), Epsilon);
            //<trkpt lat="28.017381333" lon="-16.548457333">
              //<time>2015-03-02T12:11:24Z</time>
            //</trkpt>
            metadataKey = keyList.get(1);
            metadata = ds.get(metadataKey);
            assertNotNull(metadata);
        }
        catch (EntityNotFoundException | IOException | URISyntaxException | ExifException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
}
