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

import java.nio.ByteBuffer;

/**
 *
 * @author tkv
 */
public abstract class JPEGVisitor<T>
{
    private T context;
    private ByteBuffer image;
    public JPEGVisitor(T context, ByteBuffer image)
    {
        this.context = context;
        this.image = image;
    }

    public void walk() throws ExifException
    {
        image.rewind();
        ByteBufferHelper.consumeByte(image, ExifConstants.MARKER_PREFIX);
        ByteBufferHelper.consumeByte(image, ExifConstants.SOI);
        visit(context, ExifConstants.SOI, ByteBufferHelper.sliceLength(image, 0, 2));
        int marker = ExifConstants.SOI;
        while (marker != ExifConstants.SOS)
        {
            int pos = image.position();
            ByteBufferHelper.consumeByte(image, ExifConstants.MARKER_PREFIX);
            marker = image.get() & 0xff;
            int len = image.getShort() & 0xffff;
            ByteBuffer segment = ByteBufferHelper.sliceLength(image, pos, len+2);
            image.position(pos+len+2);
            visit(context, marker, segment);
        }
        int imageLen = image.limit()-image.position()-2;
        visit(context, 0, ByteBufferHelper.sliceLength(image, imageLen));
        visit(context, ExifConstants.EOI, ByteBufferHelper.sliceLength(image, image.limit()-2, 2));
    }

    public abstract void visit(T context, int marker, ByteBuffer segment) throws ExifException;

    public static final String nameOf(int marker)
    {
        switch (marker)
        {
            case ExifConstants.SOI:
                return "SOI";
            case ExifConstants.APP0:
                return "APP0";
            case ExifConstants.APP1:
                return "APP1";
            case ExifConstants.APP2:
                return "APP2";
            case ExifConstants.APPF:
                return "APPF";
            case ExifConstants.DQT:
                return "DQT";
            case ExifConstants.DHT:
                return "DHT";
            case ExifConstants.DRI:
                return "DRI";
            case ExifConstants.SOF:
                return "SOF";
            case ExifConstants.SOS:
                return "SOS";
            case ExifConstants.EOI:
                return "EOI";
            case 0:
                return "Image";
            default:
                return "0x"+Integer.toHexString(marker);
        }
    }
    public static final String descriptionOf(int marker)
    {
        switch (marker)
        {
            case ExifConstants.SOI:
                return "Start of Image FFD8.H Start of compressed data";
            case ExifConstants.APP0:
                return "Application Segment 0 FFE0.H Jfif attribute information";
            case ExifConstants.APP1:
                return "Application Segment 1 FFE1.H Exif attribute information";
            case ExifConstants.APP2:
                return "Application Segment 2 FFE2.H Exif extended data";
            case ExifConstants.APPF:
                return "Application Segment F FFEF.H Exif extended data";
            case ExifConstants.DQT:
                return "Define Quantization Table FFDB.H Quantization table definition";
            case ExifConstants.DHT:
                return "Define Huffman Table FFC4.H Huffman table definition";
            case ExifConstants.DRI:
                return "Define Restart Interoperability FFDD.H Restart Interoperability definition";
            case ExifConstants.SOF:
                return "Start of Frame FFC0.H Parameter data relating to frame";
            case ExifConstants.SOS:
                return "Start of Scan FFDA.H Parameters relating to components";
            case ExifConstants.EOI:
                return "End of Image FFD9.H End of compressed data";
            case 0:
                return "Image Data";
            default:
                return "0x"+Integer.toHexString(marker);
        }
    }
}
