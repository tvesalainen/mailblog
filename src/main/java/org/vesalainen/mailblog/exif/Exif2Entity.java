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

import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class Exif2Entity
{
    private static Map<Integer,Map<Integer,List<Rule>>> map = new HashMap<Integer,Map<Integer,List<Rule>>>();
    
    static
    {
        add(new Rule("ImageWidth", 256, ExifConstants.IFD_oTH));
        add(new Rule("ImageLength", 257, ExifConstants.IFD_oTH));
        add(new Rule("BitsPerSample", 258, ExifConstants.IFD_oTH));
        add(new Rule("Compression", 259, ExifConstants.IFD_oTH));
        add(new Rule("PhotometricInterpretation", 262, ExifConstants.IFD_oTH));
        add(new Rule("Orientation", 274, ExifConstants.IFD_oTH));
        add(new Rule("SamplesPerPixel", 277, ExifConstants.IFD_oTH));
        add(new Rule("PlanarConfiguration", 284, ExifConstants.IFD_oTH));
        add(new Rule("YCbCrSubSampling", 530, ExifConstants.IFD_oTH));
        add(new Rule("YCbCrPositioning", 531, ExifConstants.IFD_oTH));
        add(new Rule("XResolution", 282, ExifConstants.IFD_oTH));
        add(new Rule("YResolution", 283, ExifConstants.IFD_oTH));
        add(new Rule("ResolutionUnit", 296, ExifConstants.IFD_oTH));
        add(new Rule("TransferFunction", 301, ExifConstants.IFD_oTH));
        add(new Rule("WhitePoint", 318, ExifConstants.IFD_oTH));
        add(new Rule("PrimaryChromaticities", 319, ExifConstants.IFD_oTH));
        add(new Rule("YCbCrCoefficients", 529, ExifConstants.IFD_oTH));
        add(new Rule("ReferenceBlackWhite", 532, ExifConstants.IFD_oTH));
        add(new Rule("DateTime", 306, ExifConstants.IFD_oTH));
        add(new Rule("ModifyDate", 306, ExifConstants.IFD_oTH));
        add(new Rule("ImageDescription", 270, ExifConstants.IFD_oTH));
        add(new Rule("Description", 270, ExifConstants.IFD_oTH));
        add(new Rule("Make", 271, ExifConstants.IFD_oTH));
        add(new Rule("Model", 272, ExifConstants.IFD_oTH));
        add(new Rule("Software", 305, ExifConstants.IFD_oTH));
        add(new Rule("CreatorTool", 305, ExifConstants.IFD_oTH));
        add(new Rule("Artist", 315, ExifConstants.IFD_oTH));
        add(new Rule("Creator", 315, ExifConstants.IFD_oTH));
        add(new Rule("Copyright", 33432, ExifConstants.IFD_oTH));
        add(new Rule("Rights", 33432, ExifConstants.IFD_oTH));
        add(new Rule("ExifVersion", 36864, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FlashpixVersion", 40960, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ColorSpace", 40961, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ComponentsConfiguration", 37121, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("CompressedBitsPerPixel", 37122, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("PixelXDimension", 40962, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("PixelYDimension", 40963, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("UserComment", 37510, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("RelatedSoundFile", 40964, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("DateTimeOriginal", 36867, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("DateTimeDigitized", 36868, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ExposureTime", 33434, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FNumber", 33437, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ExposureProgram", 34850, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SpectralSensitivity", 34852, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ISOSpeedRatings", 34855, ExifConstants.EXIFIFDPOINTER));
        //add(new Rule("OECF", 34856, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ShutterSpeedValue", 37377, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ApertureValue", 37378, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("BrightnessValue", 37379, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ExposureBiasValue", 37380, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("MaxApertureValue", 37381, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SubjectDistance", 37382, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("MeteringMode", 37383, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("LightSource", 37384, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("Flash", 37385, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FocalLength", 37386, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SubjectArea", 37396, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FlashEnergy", 41483, ExifConstants.EXIFIFDPOINTER));
        //add(new Rule("SpatialFrequencyResponse", 41484, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FocalPlaneXResolution", 41486, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FocalPlaneYResolution", 41487, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FocalPlaneResolutionUnit", 41488, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SubjectLocation", 41492, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ExposureIndex", 41493, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SensingMethod", 41495, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FileSource", 41728, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SceneType", 41729, ExifConstants.EXIFIFDPOINTER));
        //add(new Rule("CFAPattern", 41730, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("CustomRendered", 41985, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ExposureMode", 41986, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("WhiteBalance", 41987, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("DigitalZoomRatio", 41988, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("FocalLengthIn35mmFilm", 41989, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SceneCaptureType", 41990, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("GainControl", 41991, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("Contrast", 41992, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("Saturation", 41993, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("Sharpness", 41994, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("DeviceSettingDescription", 41995, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("SubjectDistanceRange", 41996, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("ImageUniqueID", 42016, ExifConstants.EXIFIFDPOINTER));
        add(new Rule("GPSVersionID", 0, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSLatitude", 2, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSLongitude", 4, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSAltitudeRef", 5, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSAltitude", 6, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSTimeStamp", 29, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSSatellites", 8, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSStatus", 9, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSMeasureMode", 10, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDOP", 11, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSSpeedRef", 12, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSSpeed", 13, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSTrackRef", 14, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSTrack", 15, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSImgDirectionRef", 16, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSImgDirection", 17, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSMapDatum", 18, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDestLatitude", 20, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDestLongitude", 22, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDestBearingRef", 23, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDestBearing", 24, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDestDistanceRef", 25, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDestDistance", 26, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSProcessingMethod", 27, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSAreaInformation", 28, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("GPSDifferential", 30, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule("SerialNumber", ExifConstants.CANONCAMERASERIALNUMBER, ExifConstants.MAKERNOTECANON));
        add(new Rule("Creator", ExifConstants.CANONOWNERNAME, ExifConstants.MAKERNOTECANON));
        add(new Rule("ImageUniqueID", ExifConstants.CANONIMAGENUMBER, ExifConstants.MAKERNOTECANON));
        add(new Rule("SerialNumber", ExifConstants.PENTAXSERIALNUMBER, ExifConstants.MAKERNOTEPENTAX));
        add(new Rule("ImageUniqueID", ExifConstants.PENTAXFRAMENUMBER, ExifConstants.MAKERNOTEPENTAX));
    }

    private static void add(Rule rule)
    {
        Map<Integer, List<Rule>> ifdMap = map.get(rule.ifd);
        if (ifdMap == null)
        {
            ifdMap = new HashMap<Integer, List<Rule>>();
            map.put(rule.ifd, ifdMap);
        }
        List<Rule> list = ifdMap.get(rule.tag);
        if (list == null)
        {
            list = new ArrayList<Rule>();
            ifdMap.put(rule.tag, list);
        }
        list.add(rule);
    }

    public static void populate(ExifAPP1 exif, Entity entity) throws IOException
    {
        for (IFD ifd : exif.getIFDs())
        {
            Map<Integer, List<Rule>> ifdMap = map.get(ifd.getIfdNum());
            if (ifdMap != null)
            {
                for (Interoperability ioa : ifd.getAll())
                {
                    List<Rule> list = ifdMap.get(ioa.tag());
                    if (list != null)
                    {
                        for (Rule rule : list)
                        {
                            handle(entity, rule, ioa);
                        }
                    }
                    else
                    {
                        System.err.println("Tag "+ioa.tag()+" Ifd "+ifd.getIfdNum()+" not found");
                    }
                }
            }
        }
    }

    private static void handle(Entity entity, Rule rule, Interoperability ioa) throws IOException
    {
        Object value = ioa.getValue();
        String strValue = value.toString().trim();
        int tag = ioa.tag();
        if (!strValue.isEmpty())
        {
            switch (tag)
            {
                case 306:
                case 36867:
                case 36868:
                    Object date = ioa.getValue();
                    if (date instanceof Date)
                    {
                        if ("DateTimeOriginal".equals(rule.property))
                        {
                            entity.setProperty(rule.property, date);
                        }
                        else
                        {
                            entity.setUnindexedProperty(rule.property, date);
                        }
                    }
                    break;
                case 270:
                case 33432:
                case 37510:
                    entity.setUnindexedProperty(rule.property, strValue);
                    break;
                case 0:
                    if (rule.ifd == ExifConstants.GPSINFOIFDPOINTER)
                    {
                        Byte[] bb = (Byte[]) value;
                        String id = String.format("%d.%d.%d.%d", bb[0], bb[1], bb[2], bb[3]);
                        entity.setUnindexedProperty(rule.property, id);
                    }
                    break;
                case 315:
                case ExifConstants.CANONOWNERNAME:
                    if (rule.ifd == ExifConstants.MAKERNOTE)
                    {
                        entity.setUnindexedProperty(rule.property, strValue);
                    }
                    break;
                case ExifConstants.FLASH:
                {
                    int pp = ioa.getShortValue();
                    switch (pp & 0x1)
                    {
                        case 1 :
                            entity.setUnindexedProperty(rule.property+"Fired", true);
                            break;
                        case 0 :
                            entity.setUnindexedProperty(rule.property+"Fired", false);
                            break;
                    }
                    switch ((pp>>4) & 0x1)
                    {
                        case 0 :
                            entity.setUnindexedProperty(rule.property+"Function", true);
                            break;
                        default:
                            entity.setUnindexedProperty(rule.property+"Function", false);
                            break;
                    }
                    entity.setUnindexedProperty(rule.property+"Mode", String.valueOf((pp>>3) & 0x3));
                    switch ((pp>>5) & 0x1)
                    {
                        case 0 :
                            entity.setUnindexedProperty(rule.property+"RedEyeMode", false);
                            break;
                        case 1 :
                            entity.setUnindexedProperty(rule.property+"RedEyeMode", true);
                            break;
                    }
                    entity.setUnindexedProperty(rule.property+"Return", String.valueOf((pp>>1) & 0x3));
                }
                    break;
                default:
                    switch (ioa.type())
                    {
                        case ExifConstants.BYTE:      // An 8-bit unsigned integer.,
                        case ExifConstants.ASCII:     // An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL.,
                        case ExifConstants.SHORT:     // A 16-bit (2-byte) unsigned integer,
                        case ExifConstants.LONG:      // A 32-bit (4-byte) unsigned integer,
                        case ExifConstants.SLONG:     // A 32-bit (4-byte) signed integer (2's complement notation),
                        case ExifConstants.RATIONAL:  // Two LONGs. The first LONG is the numerator and the second LONG expresses the denominator.,
                        case ExifConstants.SRATIONAL: // Two SLONGs. The first SLONG is the numerator and the second SLONG is the denominator.
                        case ExifConstants.UNDEFINED: // An 8-bit byte that can take any value depending on the field definition,
                        {
                            if (ioa.count() == 1 || ioa.getValue() instanceof String)
                            {
                                entity.setUnindexedProperty(rule.property, strValue);
                            }
                        }
                            break;
                        default:
                            throw new UnsupportedOperationException(ioa+" not supported");
                    }
                    break;
            }
        }
    }

    private static class Rule
    {
        String property;
        int tag;
        int ifd;

        public Rule(String property, int tag, int ifd)
        {
            this.property = property;
            this.tag = tag;
            this.ifd = ifd;
        }
    }
}
