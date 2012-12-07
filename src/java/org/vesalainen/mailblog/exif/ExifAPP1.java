/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class ExifAPP1
{
    private IFD[] ifds = new IFD[2];
    private ByteBuffer body;
    private ByteBuffer thumbnail;
    private Object make;
    private Map<Integer,IFD> ifdMap = new HashMap<Integer,IFD>();


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
            MemoryEntry.add(pos, "Next IFD Offset="+Integer.toHexString(offset));
            int ifdNum = ExifConstants.IFD_oTH;
            while (offset != 0)
            {
                app1Body.position(offset);
                IFD ifd = new IFD(app1Body, ifdNum, this);
                addIFD(ifd);
                pos = app1Body.position();
                offset = app1Body.getInt();
                ifdNum++;
                MemoryEntry.add(pos, "Next IFD Offset="+Integer.toHexString(offset));
            }

            Interoperability thumbnailOffset = get(ExifConstants.IFD_1ST, ExifConstants.JPEGINTERCHANGEFORMAT);
            Interoperability thumbnailLength = get(ExifConstants.IFD_1ST, ExifConstants.JPEGINTERCHANGEFORMATLENGTH);
            if (thumbnailOffset != null && thumbnailLength != null)
            {
                app1Body.position(thumbnailOffset.getIntValue());
                MemoryEntry.add(app1Body.position(), "Thumbnail");
                thumbnail = app1Body.slice();
            }
            for (int ifdTag : ifdMap.keySet())
            {
                System.err.println(TagHelper.getName(ifdTag));
                System.err.println("-------------------------");
                IFD ifd = ifdMap.get(ifdTag);
                for (Interoperability ioa : ifd.getAll())
                {
                    System.err.println(ioa+ioa.getValue().toString());
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
}
