/*
 * Copyright (C) 2004 Timo Vesalainen
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ExifAPP1
{
    private IFD[] ifds = new IFD[2];
    private ByteBuffer body;
    private ByteBuffer thumbnail;
    private Object make;
    private Map<Integer,IFD> ifdMap = new HashMap<Integer,IFD>();
    private Map<String,String> metadata = new HashMap<String,String>();
    private Date timestamp;


    public ExifAPP1(ByteBuffer app1Segment) throws ExifException, IOException, UnknownAPP1Exception
    {
        body = app1Segment;
        app1Segment.rewind();
        body = app1Segment.duplicate();
        ByteBufferHelper.consumeByte(app1Segment, ExifConstants.MARKER_PREFIX);
        ByteBufferHelper.consumeByte(app1Segment, ExifConstants.APP1);
        app1Segment.getShort();
        String namespace = ByteBufferHelper.readASCII(app1Segment);
        if (namespace.equals(ExifConstants.EXIF))
        {
            ByteBufferHelper.consumeByte(app1Segment, 0);
            ByteBuffer app1Body = app1Segment.slice();
            app1Body.order(app1Segment.order());
            int byteOrder = app1Body.getShort();
            if (byteOrder == ExifConstants.LITTLE_ENDIAN)
            {
                app1Body.order(ByteOrder.LITTLE_ENDIAN);
            }
            ByteBufferHelper.consumeShort(app1Body, 42);
            int pos = app1Body.position();
            int offset = app1Body.getInt();
            int ifdNum = ExifConstants.IFD_oTH;
            while (offset != 0)
            {
                app1Body.position(offset);
                IFD ifd = new IFD(app1Body, ifdNum, this);
                addIFD(ifd);
                pos = app1Body.position();
                offset = app1Body.getInt();
                ifdNum++;
            }

            Interoperability thumbnailOffset = get(ExifConstants.IFD_1ST, ExifConstants.JPEGINTERCHANGEFORMAT);
            Interoperability thumbnailLength = get(ExifConstants.IFD_1ST, ExifConstants.JPEGINTERCHANGEFORMATLENGTH);
            if (thumbnailOffset != null && thumbnailLength != null)
            {
                app1Body.position(thumbnailOffset.getIntValue());
                thumbnail = app1Body.slice();
            }
            for (int ifdTag : ifdMap.keySet())
            {
                //System.err.println(TagHelper.getName(ifdTag));
                //System.err.println("-------------------------");
                IFD ifd = ifdMap.get(ifdTag);
                for (Interoperability ioa : ifd.getAll())
                {
                    int tag = ioa.tag();
                    switch (tag)
                    {
                        case 306:
                        case 36867:
                        case 36868:
                            Object date = ioa.getValue();
                            if (date instanceof Date)
                            {
                                timestamp = (Date) date;
                            }
                    }
                }
            }
        }
        else
        {
            throw new UnknownAPP1Exception(namespace+" not supported");
        }
    }

    public Interoperability get(int ifdNum, int tag)
    {
        IFD ifd = ifdMap.get(ifdNum);
        if (ifd != null)
        {
            return ifd.get(tag);
        }
        return null;
    }

    public Collection<IFD> getIFDs()
    {
        return ifdMap.values();
    }

    public void addIFD(IFD ifd)
    {
        if (this.ifds[0] == null)
        {
            this.ifds[0] = ifd;
        }
        else
        {
            if (this.ifds[1] == null)
            {
                this.ifds[1] = ifd;
            }
            else
            {
                throw new IllegalArgumentException("Beyond 1st IFD???");
            }
        }
    }

    public void setThumbnail(ByteBuffer buffer)
    {
        thumbnail = buffer;
    }
    public void addIFD(int ifdNum, IFD ifd)
    {
        ifdMap.put(ifdNum, ifd);
    }

    public int makerNoteNum()
    {
        if (ExifConstants.MAKECANON.equals(make))
        {
            return ExifConstants.MAKERNOTECANON;
        }
        /*
        if (ExifConstants.MAKEPENTAX.equals(make))
        {
            return ExifConstants.MAKERNOTEPENTAX;
        }
         */
        return -1;
    }
    /**
     * @return the make
     */
    public Object getMake()
    {
        return make;
    }

    /**
     * @param make the make to set
     */
    public void setMake(Object make)
    {
        this.make = make;
    }

    /**
     * @return the body
     */
    public ByteBuffer getBody()
    {
        return body;
    }

    /**
     * @return the thumbnail
     */
    public ByteBuffer getThumbnail()
    {
        return thumbnail;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }
}
