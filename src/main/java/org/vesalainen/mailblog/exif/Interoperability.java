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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author tkv
 */
public final class Interoperability implements Comparable<Interoperability>
{
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    private ByteBuffer array;
    private ByteBuffer value;
    private int length;
    private IFD ifd;

    public Interoperability(ByteBuffer app1Body, ExifAPP1 app1) throws IOException
    {
        array = ByteBufferHelper.sliceLength(app1Body, 12);
        if (isIFD(app1))
        {
            ByteBuffer app1BodyCopy = app1Body.duplicate();
            app1BodyCopy.order(app1Body.order());
            app1BodyCopy.position(offset());
            try
            {
                ifd = new IFD(app1BodyCopy, tag(), app1);
            }
            catch (Exception ex)
            {
                //ex.printStackTrace();
            }
            value = ByteBufferHelper.sliceLength(array, 8, 4);
            length = 4;
        }
        else
        {
            length = count()*valueLength();
            if (isLongValue())
            {
                value = ByteBufferHelper.sliceLength(app1Body, offset(), length);
            }
            else
            {
                value = ByteBufferHelper.sliceLength(array, 8, length);
            }
            if (tag() == ExifConstants.MAKE)
            {
                app1.setMake(getValue());
            }
        }
    }

    public boolean isIFD(ExifAPP1 app1)
    {
        int tag = tag();
        if (app1 != null && tag == ExifConstants.MAKERNOTE && app1.makerNoteNum() != 0)
        {
            return true;
        }
        return (
                tag == ExifConstants.EXIFIFDPOINTER ||
                tag == ExifConstants.GPSINFOIFDPOINTER ||
                tag == ExifConstants.INTEROPERABILITYIFDPOINTER
                );
    }

    public boolean isDate()
    {
        int tag = tag();
        return (
                tag == ExifConstants.DATETIME ||
                tag == ExifConstants.DATETIMEORIGINAL ||
                tag == ExifConstants.DATETIMEDIGITIZED
                );
    }

    public boolean isLongValue()
    {
        return  (length > 4);
    }
    /**
     * Single value length in bytes
     * @return
     */
    private int valueLength()
    {
        switch (type())
        {
            case ExifConstants.BYTE:      // An 8-bit unsigned integer.,
                return 1;
            case ExifConstants.ASCII:     // An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL.,
                return 1;
            case ExifConstants.SHORT:     // A 16-bit (2-byte) unsigned integer,
                return 2;
            case ExifConstants.LONG:      // A 32-bit (4-byte) unsigned integer,
                return 4;
            case ExifConstants.RATIONAL:  // Two LONGs. The first LONG is the numerator and the second LONG expresses the denominator.,
                return 8;
            case ExifConstants.UNDEFINED: // An 8-bit byte that can take any value depending on the field definition,
                return 1;
            case ExifConstants.SLONG:     // A 32-bit (4-byte) signed integer (2's complement notation),
                return 4;
            case ExifConstants.SRATIONAL: // Two SLONGs. The first SLONG is the numerator and the second SLONG is the denominator.
                return 8;
            default:
                throw new UnsupportedOperationException(type()+" type not supported");
        }
    }

    public void updateValue(Date date) throws UnsupportedEncodingException
    {
        if (!isDate())
        {
            throw new IllegalArgumentException("Trying to set '"+date+"' to non Date tag "+tag());
        }
        updateValue(DATEFORMAT.format(date));
    }
    /**
     * If str doesn't fit the start is updated. Then a new buffer is allocated
     * and updated. Next call will return the updated whole value. However
     * only that amount of str that fitted is written to file.
     * @param str
     * @throws UnsupportedEncodingException
     */
    public void updateValue(String str) throws UnsupportedEncodingException
    {
        if (tag() == ExifConstants.USERCOMMENT)
        {
            updateUserComment(str);
        }
        else
        {
            if (type() != ExifConstants.ASCII)
            {
                throw new IllegalArgumentException("Trying to set '"+str+"' to non ASCII type "+type());
            }
            byte[] bb = str.getBytes("US-ASCII");
            bb = Arrays.copyOf(bb, bb.length+1);
            updateValue(bb);
        }
    }

    public void updateUserComment(String str) throws UnsupportedEncodingException
    {
        if (tag() != ExifConstants.USERCOMMENT)
        {
            throw new IllegalArgumentException("Trying to set '"+str+"' to non USERCOMMENT type "+type());
        }
        int neededLength = 8+str.length()*2;
        int count = count();
        if (count < neededLength)
        {
            if (count >= 10)
            {
                putUserComment(value, str.substring(0, (count-8)/2));
            }
            ByteOrder bo = value.order();
            value = ByteBuffer.allocate(neededLength);
            value.order(bo);
        }
        putUserComment(value, str);
    }

    private ByteBuffer putUserComment(ByteBuffer buffer, String str)
    {
        //ByteBuffer buffer = ByteBuffer.allocate(8+str.length()*2);
        buffer.rewind();
        buffer.put("Unicode".getBytes());
        buffer.put((byte)0);
        buffer.asCharBuffer().put(str);
        return buffer;
    }
    public void updateValue(byte[] bb) throws UnsupportedEncodingException
    {
        int count = count();
        value.rewind();
        if (bb.length > count-1)
        {
            if (count > 1)
            {
                value.put(bb, 0, count-1);
                value.put((byte)0);
            }
            ByteOrder bo = value.order();
            value = ByteBuffer.allocate(bb.length+1);
            value.order(bo);
        }
        value.put(bb);
        value.put((byte)0);
    }

    public void updateValue(long... val)
    {
        int count = count();
        value.rewind();
        if (count != val.length)
        {
            throw new IllegalArgumentException("Trying to set "+val.length+" arguments to "+count+" dimension value");
        }
        switch (type())
        {
            case ExifConstants.BYTE:      // An 8-bit unsigned integer.,
            {
                byte[] bb = new byte[val.length];
                for (int ii=0;ii<val.length;ii++)
                {
                    if ((val[ii]>>8) != 0)
                    {
                        throw new IllegalArgumentException(ii+" doesn't fit to unsigned byte");
                    }
                    bb[ii] =     (byte) (ii & 0xff);
                }
                value.put(bb);
            }
            break;
            case ExifConstants.SHORT:     // A 16-bit (2-byte) unsigned integer,
            {
                short[] bb = new short[val.length];
                for (int ii=0;ii<val.length;ii++)
                {
                    if ((val[ii]>>16) != 0)
                    {
                        throw new IllegalArgumentException(ii+" doesn't fit to unsigned short");
                    }
                    bb[ii] = (short)val[ii];
                }
                value.asShortBuffer().put(bb);
            }
            break;
            case ExifConstants.LONG:      // A 32-bit (4-byte) unsigned integer,
            case ExifConstants.SLONG:     // A 32-bit (4-byte) signed integer (2's complement notation),
            {
                int[] bb = new int[val.length];
                for (int ii=0;ii<val.length;ii++)
                {
                    if ((val[ii]>>32) != 0)
                    {
                        throw new IllegalArgumentException(ii+" doesn't fit to int");
                    }
                    bb[ii] = (int) val[ii];
                }
                value.asIntBuffer().put(bb);
            }
            break;
            default:
                throw new UnsupportedOperationException(type()+" type not supported");
        }
    }

    public Object getValue() throws IOException
    {
        if (value == null)
        {
            return "Construction ongoing...";
        }
        int count = count();
        value.rewind();
        switch (type())
        {
            case ExifConstants.BYTE:      // An 8-bit unsigned integer.,
            {
                if (count == 1)
                {
                    return value.get();
                }
                else
                {
                    byte[] bb = new byte[count];
                    value.get(bb);
                    Byte[] bbb = new Byte[bb.length];
                    for (int ii=0;ii<bb.length;ii++)
                    {
                        bbb[ii] = bb[ii];
                    }
                    return bbb;
                }
            }
            case ExifConstants.ASCII:     // An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL.,
            {
                if (count == 1)
                {
                    return (char) ((char) value.get() & 0xff);
                }
                else
                {
                    byte[] bb = new byte[count-1];
                    value.get(bb);
                    String str = new String(bb, Charset.forName("US-ASCII"));
                    if (isDate())
                    {
                        try
                        {
                            return DATEFORMAT.parse(str);
                        }
                        catch (ParseException ex)
                        {
                            return str;
                        }
                    }
                    else
                    {
                        return str;
                    }
                }
            }
            case ExifConstants.SHORT:     // A 16-bit (2-byte) unsigned integer,
            {
                if (count == 1)
                {
                    return (short)(value.getShort() & 0xffff);
                }
                else
                {
                    short[] ss = new short[count];
                    value.asShortBuffer().get(ss);
                    Short[] sss = new Short[ss.length];
                    for (int ii=0;ii<ss.length;ii++)
                    {
                        sss[ii] = ss[ii];
                    }
                    return sss;
                }
            }
            case ExifConstants.LONG:      // A 32-bit (4-byte) unsigned integer,
            {
                if (count == 1)
                {
                    return (long)(value.getInt() & 0xffffffff);
                }
                else
                {
                    Long[] ll = new Long[count];
                    IntBuffer ib = value.asIntBuffer();
                    for (int ii=0;ii<count;ii++)
                    {
                        ll[ii] = (long)(ib.get(ii) & 0xffffffff);
                    }
                    return ll;
                }
            }
            case ExifConstants.RATIONAL:  // Two LONGs. The first LONG is the numerator and the second LONG expresses the denominator.,
            {
                if (count() == 1)
                {
                    IntBuffer lb = value.asIntBuffer();
                    return (double)((long)(lb.get(0) & 0xffffffff) / (long)(lb.get(1) & 0xffffffff));
                }
                else
                {
                    Double[] rr = new Double[count];
                    IntBuffer ib = value.asIntBuffer();
                    for (int ii=0;ii<count;ii++)
                    {
                        rr[ii] = (double)((long)(ib.get(2*ii) & 0xffffffff) / (long)(ib.get(2*ii+1) & 0xffffffff));
                    }
                    return rr;
                }
            }
            case ExifConstants.UNDEFINED: // An 8-bit byte that can take any value depending on the field definition,
            {
                switch (tag())
                {
                    case ExifConstants.EXIFVERSION:
                    case ExifConstants.FLASHPIXVERSION:
                    {
                        byte[] bb = new byte[count];
                        value.get(bb);
                        return new String(bb, Charset.forName("US-ASCII"));
                    }
                    case ExifConstants.USERCOMMENT:
                    {
                        if (count < 4)
                        {
                            return "";
                        }
                        byte[] bb = new byte[8];
                        value.get(bb);
                        String cs = new String(bb);
                        for (int ii=0;ii<8;ii++)
                        {
                            if (bb[ii] == 0)
                            {
                                cs = new String(bb, 0, ii);
                                break;
                            }
                        }
                        if ("ASCII".equals(cs))
                        {
                            cs = "US-ASCII";
                        }
                        if ("Unicode".equals(cs))
                        {
                            if (ByteOrder.BIG_ENDIAN.equals(value.order()))
                            {
                                cs = "UTF-16BE";
                            }
                            else
                            {
                                cs = "UTF-16LE";
                            }
                        }
                        bb = new byte[count()-8];
                        value.get(bb);
                        if (!cs.trim().isEmpty())
                        {
                            Charset charset = Charset.forName(cs);
                            return new String(bb, charset);
                        }
                        else
                        {
                            return "";
                        }
                    }
                    case ExifConstants.OECF:
                    case ExifConstants.SPATIALFREQUENCYRESPONSE:
                        return new OECF(value);
                    case ExifConstants.CFAPATTERN:
                        return new CFA(value);
                    default:
                        return "";   //throw new UnsupportedOperationException(tag()+" tag not supported for UNDEFINED type");
                }

            }
            case ExifConstants.SLONG:     // A 32-bit (4-byte) signed integer (2's complement notation),
            {
                if (count == 1)
                {
                    return value.getInt();
                }
                else
                {
                    int[] ii = new int[count];
                    value.asIntBuffer().get(ii);
                    Integer[] iii = new Integer[ii.length];
                    for (int jj=0;jj<ii.length;jj++)
                    {
                        iii[jj] = ii[jj];
                    }
                    return iii;
                }
            }
            case ExifConstants.SRATIONAL: // Two SLONGs. The first SLONG is the numerator and the second SLONG is the denominator.
            {
                if (count() == 1)
                {
                    IntBuffer ib = value.asIntBuffer();
                    return (double)(ib.get(0) / ib.get(1));
                }
                else
                {
                    Double[] rr = new Double[count];
                    IntBuffer ib = value.asIntBuffer();
                    for (int ii=0;ii<count;ii++)
                    {
                        rr[ii] = (double)(ib.get(2*ii) / ib.get(2*ii+1));
                    }
                    return rr;
                }
            }
            default:
                throw new UnsupportedOperationException(type()+" type not supported");
        }
    }

    public Date getDateValue() throws IOException
    {
        if (!isDate())
        {
            throw new IllegalArgumentException("Trying to get date for non Date tag "+tag());
        }
        Date date;
        Object obj = getValue();
        if (obj instanceof Date && count() == 20)
        {
            date = (Date) obj;
        }
        else
        {
            date = new Date(0);
        }
        return date;
    }

    public int getShortValue() throws IOException
    {
        Number num = (Number) getValue();
        return num.intValue();
    }

    public int getIntValue() throws IOException
    {
        Number num = (Number) getValue();
        return num.intValue();
    }

    public long getLongValue() throws IOException
    {
        Number num = (Number) getValue();
        return num.longValue();
    }

    public int tag()
    {
        int ll = array.getShort(0) & 0xffff;
        return ll;
    }

    public int type()
    {
        int ll = array.getShort(2) & 0xffff;
        return ll;
    }

    public int count()
    {
        return array.getInt(4);
    }

    public int offset()
    {
        return array.getInt(8);
    }

    /**
     * @return the ifd
     */
    public IFD getIfd()
    {
        return ifd;
    }

    public String name()
    {
        return TagHelper.getName(tag());
    }

    @Override
    public String toString()
    {
        //try
        {
            return TagHelper.getName(tag()) + ": ";// + getValue();
        }
        /*
        catch (IOException ex)
        {
            return TagHelper.getName(tag()) + ": " + ex.getMessage();
        }
         */
    }

    @Override
    public int compareTo(Interoperability o)
    {
        return tag() - o.tag();
    }


}
