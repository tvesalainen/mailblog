/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.mailblog.exif;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPException;
import com.google.appengine.api.datastore.GeoPt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Timo Vesalainen
 */
public class ExifParser
{
    private byte[] bytes;
    private ExifAPP1 exifApp1;
    private XMPAPP1 xmpApp1;

    public ExifParser(byte[] bytes) throws IOException, ExifException, XMPException
    {
        this.bytes = bytes;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        parse(buffer);
        if (xmpApp1 == null)
        {
            xmpApp1 = new XMPAPP1();
        }
        if (exifApp1 != null)
        {
            Exif2XMP.populate(exifApp1, xmpApp1);
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
                                xmpApp1 = new XMPAPP1(segment);
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

    public String getDescription(Locale locale) throws XMPException
    {
        return xmpApp1.getProperty(locale, XMPConst.NS_DC, "description");
    }
    public void setDescription(Locale locale, String description) throws UnsupportedEncodingException, XMPException
    {
        if (exifApp1 != null)
        {
            Interoperability ioa = exifApp1.get(ExifConstants.IFD_oTH, ExifConstants.IMAGEDESCRIPTION);
            if (ioa != null)
            {
                ioa.updateValue(description);
            }
        }
        xmpApp1.setProperty(locale, XMPConst.NS_DC, "description", description);
    }

    public String getUserComment(Locale locale) throws XMPException
    {
        return xmpApp1.getProperty(locale, XMPConst.NS_EXIF, "UserComment");
    }
    public void setUserComment(Locale locale, String description) throws UnsupportedEncodingException, XMPException
    {
        Interoperability ioa = exifApp1.get(ExifConstants.EXIFIFDPOINTER, ExifConstants.USERCOMMENT);
        if (ioa != null)
        {
            ioa.updateValue(description);
        }
        xmpApp1.setProperty(locale, XMPConst.NS_EXIF, "UserComment", description);
    }

    public String getTitle(Locale locale) throws XMPException
    {
        return xmpApp1.getProperty(locale, XMPConst.NS_DC, "title");
    }
    public void setTitle(Locale locale, String title) throws UnsupportedEncodingException, XMPException
    {
        xmpApp1.setProperty(locale, XMPConst.NS_DC, "title", title);
    }

    public String getCopyright(Locale locale) throws XMPException
    {
        return xmpApp1.getProperty(locale, XMPConst.NS_DC, "rights");
    }
    public void setCopyright(Locale locale, String copyright) throws UnsupportedEncodingException, XMPException
    {
        if (exifApp1 != null)
        {
            Interoperability ioa = exifApp1.get(ExifConstants.IFD_oTH, ExifConstants.COPYRIGHT);
            if (ioa != null)
            {
                ioa.updateValue(copyright);
            }
        }
        xmpApp1.setProperty(locale, XMPConst.NS_DC, "rights", copyright);
    }

    public String getLabel() throws XMPException
    {
        return xmpApp1.getProperty(XMPConst.NS_XMP, "Label");
    }
    public void setLabel(String label) throws UnsupportedEncodingException, XMPException
    {
        xmpApp1.setProperty(XMPConst.NS_XMP, "Label", label);
    }

    public Date getMetadataDate() throws XMPException
    {
        return xmpApp1.getDateProperty(XMPConst.NS_XMP, "MetadataDate");
    }
    public void setMetadataDate(Date date) throws UnsupportedEncodingException, XMPException
    {
        xmpApp1.setProperty(XMPConst.NS_XMP, "MetadataDate", date);
    }

    public Date getModifyDate() throws XMPException
    {
        return xmpApp1.getDateProperty(XMPConst.NS_XMP, "ModifyDate");
    }
    public void setModifyDate(Date date) throws UnsupportedEncodingException, XMPException
    {
        xmpApp1.setProperty(XMPConst.NS_XMP, "ModifyDate", date);
    }

    public GeoPt getLocation() throws XMPException, ParseException
    {
        return xmpApp1.getLocation();
    }
    public void setLocation(GeoPt location) throws XMPException
    {
        xmpApp1.setLocation(location);
    }
    public void setLatitude(double latitude) throws XMPException
    {
        xmpApp1.setLatitude(latitude);
    }
    public void setLongitude(double longitude) throws XMPException
    {
        xmpApp1.setLongitude(longitude);
    }
    public String[] getCreators() throws XMPException
    {
        return xmpApp1.getArray(XMPConst.NS_DC, "creator");
    }
    public void addCreator(String creator) throws UnsupportedEncodingException, XMPException
    {
        if (exifApp1 != null)
        {
            Interoperability ioa = exifApp1.get(ExifConstants.IFD_oTH, ExifConstants.ARTIST);
            if (ioa != null)
            {
                ioa.updateValue(creator);
            }
        }
        xmpApp1.addSeq(XMPConst.NS_DC, "creator", creator);
    }
    public String[] getSubjects() throws XMPException
    {
        return xmpApp1.getArray(XMPConst.NS_DC, "subject");
    }
    public void addSubject(String subject) throws UnsupportedEncodingException, XMPException
    {
        xmpApp1.addBag(XMPConst.NS_DC, "subject", subject);
    }
    public String[] getOwners() throws XMPException
    {
        return xmpApp1.getArray(XMPConst.NS_XMP_RIGHTS, "Owner");
    }
    public void addOwner(String subject) throws UnsupportedEncodingException, XMPException
    {
        xmpApp1.addBag(XMPConst.NS_XMP_RIGHTS, "Owner", subject);
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
            System.err.println(parser.getModifyDate());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
