/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPProperty;
import com.google.appengine.api.datastore.GeoPt;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author tkv
 */
public class XMPAPP1
{
    //private static final CoordinateFormat LATITUDEFORMAT = new CoordinateFormat(Locale.US, "D,mmmmmN");
    //private static final CoordinateFormat LONGITUDEFORMAT = new CoordinateFormat(Locale.US, "D,mmmmmE");
    private XMPMeta xmp;

    public XMPAPP1()
    {
        xmp = XMPMetaFactory.create();
    }

    public XMPAPP1(ByteBuffer app1Segment) throws ExifException, IOException, UnknownAPP1Exception
    {
        app1Segment.rewind();
        ByteBufferHelper.consumeByte(app1Segment, ExifConstants.MARKER_PREFIX);
        ByteBufferHelper.consumeByte(app1Segment, ExifConstants.APP1);
        int length = app1Segment.getShort() & 0xffff;
        String namespace = ByteBufferHelper.readASCII(app1Segment);
        if (namespace.equals(ExifConstants.XMP))
        {
            try
            {
                xmp = XMPMetaFactory.parse(new ByteBufferInputStream(app1Segment));
                XMPMetaFactory.serialize(xmp, System.err);
            }
            catch (XMPException ex)
            {
                throw new ExifException(ex);
            }
        }
        else
        {
            throw new UnknownAPP1Exception(namespace+" not supported");
        }
    }

    public ByteBuffer createSegment() throws XMPException
    {
        ByteBuffer app1Segment = ByteBuffer.allocate(0x10000);
        app1Segment.put((byte)ExifConstants.MARKER_PREFIX);
        app1Segment.put((byte)ExifConstants.APP1);
        app1Segment.putShort((short)0);
        app1Segment.put(ExifConstants.XMP.getBytes());
        app1Segment.put((byte)0);
        XMPMetaFactory.serialize(getXmp(), new ByteBufferOutputStream(app1Segment));
        app1Segment.flip();
        app1Segment.putShort(2,(short) (app1Segment.limit() - 2));
        return app1Segment;
    }

    public String getProperty(Locale locale, String namespace, String property) throws XMPException
    {
        XMPProperty prop = null;
        if (locale.getVariant().isEmpty())
        {
            prop = xmp.getLocalizedText(namespace, property, locale.getLanguage(), locale.getLanguage());
        }
        else
        {
            prop = xmp.getLocalizedText(namespace, property, locale.getLanguage(), locale.getVariant());
        }
        if (prop != null)
        {
            return prop.getValue().toString();
        }
        else
        {
            return null;
        }
    }
    public void setProperty(Locale locale, String namespace, String property, String value) throws XMPException
    {
        if (locale.getVariant().isEmpty())
        {
            xmp.setLocalizedText(namespace, property, locale.getLanguage(), locale.getLanguage(), value);
        }
        else
        {
            xmp.setLocalizedText(namespace, property, locale.getLanguage(), locale.getVariant(), value);
        }
    }
    public String getProperty(String namespace, String property) throws XMPException
    {
        XMPProperty prop = xmp.getProperty(namespace, property);
        if (prop != null)
        {
            return prop.toString();
        }
        else
        {
            return null;
        }
    }
    public void setProperty(String namespace, String property, String value) throws XMPException
    {
        xmp.setProperty(namespace, property, value);
    }
    public Date getDateProperty(String namespace, String property) throws XMPException
    {
        Calendar cal = xmp.getPropertyCalendar(namespace, property);
        if (cal != null)
        {
            return cal.getTime();
        }
        else
        {
            return null;
        }
    }
    public void setProperty(String namespace, String property, Date date) throws XMPException
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        xmp.setPropertyCalendar(namespace, property, cal);
    }
    public GeoPt getLocation() throws XMPException, ParseException
    {
        String lat = getProperty(XMPConst.NS_EXIF, "GPSLatitude");
        String lon = getProperty(XMPConst.NS_EXIF, "GPSLongitude");
        if (lat != null && lon != null)
        {
            throw new UnsupportedOperationException("TODO");
            //return new GeoPt(LATITUDEFORMAT.parseDouble(lat), LONGITUDEFORMAT.parseDouble(lon));
        }
        return null;
    }
    public void setLocation(GeoPt location) throws XMPException
    {
        setLatitude(location.getLatitude());
        setLongitude(location.getLongitude());
    }
    public void setLatitude(double latitude) throws XMPException
    {
        throw new UnsupportedOperationException("TODO");
        //setProperty(XMPConst.NS_EXIF, "GPSLatitude", LATITUDEFORMAT.format(latitude));
    }
    public void setLongitude(double longitude) throws XMPException
    {
        throw new UnsupportedOperationException("TODO");
        //setProperty(XMPConst.NS_EXIF, "GPSLongitude", LONGITUDEFORMAT.format(longitude));
    }
    public void addSeq(String namespace, String property, String value) throws XMPException
    {
        PropertyOptions arrOpt = new PropertyOptions(PropertyOptions.ARRAY_ORDERED);
        xmp.appendArrayItem(namespace, property, arrOpt, value, null);
    }

    public void addBag(String namespace, String property, String value) throws XMPException
    {
        PropertyOptions arrOpt = new PropertyOptions(PropertyOptions.ARRAY);
        xmp.appendArrayItem(namespace, property, arrOpt, value, null);
    }

    public String[] getArray(String namespace, String property) throws XMPException
    {
        int len = xmp.countArrayItems(namespace, property);
        String[] res = new String[len];
        for (int ii=0;ii<len;ii++)
        {
            XMPProperty prop = xmp.getArrayItem(namespace, property, len);
            res[ii] = prop.getValue().toString();
        }
        return res;
    }
    /**
     * @return the xmp
     */
    public XMPMeta getXmp()
    {
        return xmp;
    }
}
