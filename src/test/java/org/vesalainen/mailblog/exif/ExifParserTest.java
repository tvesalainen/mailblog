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
package org.vesalainen.mailblog.exif;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ExifParserTest
{
    private final LocalServiceTestHelper helper = 
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));
    
    public ExifParserTest()
    {
    }

    @Before
    public void setUp()
    {
        helper.setUp();
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    }

    @After
    public void tearDown()
    {
        helper.tearDown();
    }

    @Test
    public void test0()
    {
        try
        {
            URL url = ExifParserTest.class.getClassLoader().getResource("IMGP1235.JPG");
            Path path = Paths.get(url.toURI());
            File file = path.toFile();
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            ExifParser parser = new ExifParser(mbb);
            Entity entity = new Entity("Metadata");
            parser.populate(entity);
            assertEquals("0230", entity.getProperty(ExifVersionProperty));
            assertEquals("WGS-84", entity.getProperty(GPSMapDatumProperty));
            assertEquals(105.0, entity.getProperty(ImgDirectionProperty));
            System.err.println(entity);
        }
        catch (URISyntaxException | IOException | ExifException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
}
