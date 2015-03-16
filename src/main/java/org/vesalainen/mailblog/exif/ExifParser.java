/*
 * Copyright (C) 2012 Timo Vesalainen
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

import com.google.appengine.api.datastore.Entity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Timo Vesalainen
 */
public class ExifParser
{
    private ExifAPP1 exifApp1;

    public ExifParser(byte[] bytes) throws IOException, ExifException
    {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        parse(buffer);
    }

    public void populate(Entity entity) throws IOException
    {
        if (exifApp1 != null)
        {
            Exif2Entity.populate(exifApp1, entity);
        }
    }
    private List<ByteBuffer> parse(ByteBuffer buffer) throws IOException, ExifException
    {
        List<ByteBuffer> list = new ArrayList<ByteBuffer>();
        JPEGVisitor<List<ByteBuffer>> visitor = new JPEGVisitor<List<ByteBuffer>>(list, buffer)
        {

            @Override
            public void visit(List<ByteBuffer> list, int marker, ByteBuffer segment) throws ExifException
            {
                if (marker == ExifConstants.APP1)
                {
                    try
                    {
                        String identifier = getIdentifier(segment);
                        if (ExifConstants.EXIF.equals(identifier))
                        {
                            segment = ByteBufferHelper.createCopy(segment);
                            exifApp1 = new ExifAPP1(segment);
                        }
                        else
                        {
                            if (ExifConstants.XMP.equals(identifier))
                            {
                                //xmpApp1 = new XMPAPP1(segment);
                            }
                            else
                            {
                                System.err.println("Unknown APP1 identifier " + identifier);
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        throw new ExifException(ex);
                    }
                }
                else
                {
                    if (marker == ExifConstants.APP0)
                    {
                            //jfifIndex = list.size();
                    }
                    else
                    {
                        list.add(segment);  // APP0 & APP1 are excluded
                    }
                }
            }
        };
        visitor.walk();
        return list;
    }

    private String getIdentifier(ByteBuffer app1Segment) throws ExifException, IOException
    {
        app1Segment.rewind();
        ByteBufferHelper.consumeByte(app1Segment, ExifConstants.MARKER_PREFIX);
        ByteBufferHelper.consumeByte(app1Segment, ExifConstants.APP1);
        app1Segment.getShort();
        return ByteBufferHelper.readASCII(app1Segment);
    }

    public Date getTimestamp()
    {
        if (exifApp1 != null)
        {
            return exifApp1.getTimestamp();
        }
        return null;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            File file = new File("c:\\temp\\IMG_2601.JPG");
            long length = file.length();
            byte[] buf = new byte[(int)length];
            FileInputStream fis = new FileInputStream(file);
            fis.read(buf);
            fis.close();
            ExifParser parser = new ExifParser(buf);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
