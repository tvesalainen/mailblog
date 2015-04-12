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
import com.google.appengine.api.datastore.GeoPt;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import static org.vesalainen.mailblog.BlogConstants.*;
import static org.vesalainen.mailblog.exif.ExifConstants.*;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import d3.env.TSAGeoMag;

/**
 *
 * @author tkv
 */
public class Exif2Entity
{
    private static MapList<Integer,Rule> map = new HashMapList<>();
    private static TSAGeoMag geoMag;
    
    static
    {
        add(new Rule("ImageWidth", 256, IFD_oTH));
        add(new Rule("ImageLength", 257, IFD_oTH));
        add(new Rule("BitsPerSample", 258, IFD_oTH));
        add(new Rule("Compression", 259, IFD_oTH));
        add(new Rule("PhotometricInterpretation", 262, IFD_oTH));
        add(new Rule("Orientation", 274, IFD_oTH));
        add(new Rule("SamplesPerPixel", 277, IFD_oTH));
        add(new Rule("PlanarConfiguration", 284, IFD_oTH));
        add(new Rule("YCbCrSubSampling", 530, IFD_oTH));
        add(new Rule("YCbCrPositioning", 531, IFD_oTH));
        add(new Rule("XResolution", 282, IFD_oTH));
        add(new Rule("YResolution", 283, IFD_oTH));
        add(new Rule("ResolutionUnit", 296, IFD_oTH));
        add(new Rule("TransferFunction", 301, IFD_oTH));
        add(new Rule("WhitePoint", 318, IFD_oTH));
        add(new Rule("PrimaryChromaticities", 319, IFD_oTH));
        add(new Rule("YCbCrCoefficients", 529, IFD_oTH));
        add(new Rule("ReferenceBlackWhite", 532, IFD_oTH));
        add(new Rule("DateTime", 306, IFD_oTH));
        add(new Rule("ImageDescription", 270, IFD_oTH));
        add(new Rule("Make", 271, IFD_oTH));
        add(new Rule("Model", 272, IFD_oTH));
        add(new Rule("Software", 305, IFD_oTH));
        add(new Rule("Artist", 315, IFD_oTH));
        add(new Rule("Copyright", 33432, IFD_oTH));
        add(new Rule("ExifVersion", 36864, EXIFIFDPOINTER));
        add(new Rule("FlashpixVersion", 40960, EXIFIFDPOINTER));
        add(new Rule("ColorSpace", 40961, EXIFIFDPOINTER));
        add(new Rule("ComponentsConfiguration", 37121, EXIFIFDPOINTER));
        add(new Rule("CompressedBitsPerPixel", 37122, EXIFIFDPOINTER));
        add(new Rule("PixelXDimension", 40962, EXIFIFDPOINTER));
        add(new Rule("PixelYDimension", 40963, EXIFIFDPOINTER));
        add(new Rule("MakerNote", 37500, EXIFIFDPOINTER));
        add(new Rule("UserComment", 37510, EXIFIFDPOINTER));
        add(new Rule("RelatedSoundFile", 40964, EXIFIFDPOINTER));
        add(new Rule("DateTimeOriginal", 36867, EXIFIFDPOINTER));
        add(new Rule("DateTimeDigitized", 36868, EXIFIFDPOINTER));
        add(new Rule("ExposureTime", 33434, EXIFIFDPOINTER));
        add(new Rule("FNumber", 33437, EXIFIFDPOINTER));
        add(new Rule("ExposureProgram", 34850, EXIFIFDPOINTER));
        add(new Rule("SpectralSensitivity", 34852, EXIFIFDPOINTER));
        add(new Rule("ISOSpeedRatings", 34855, EXIFIFDPOINTER));
        //add(new Rule("OECF", 34856, EXIFIFDPOINTER));
        add(new Rule("ShutterSpeedValue", 37377, EXIFIFDPOINTER));
        add(new Rule("ApertureValue", 37378, EXIFIFDPOINTER));
        add(new Rule("BrightnessValue", 37379, EXIFIFDPOINTER));
        add(new Rule("ExposureBiasValue", 37380, EXIFIFDPOINTER));
        add(new Rule("MaxApertureValue", 37381, EXIFIFDPOINTER));
        add(new Rule("SubjectDistance", 37382, EXIFIFDPOINTER));
        add(new Rule("MeteringMode", 37383, EXIFIFDPOINTER));
        add(new Rule("LightSource", 37384, EXIFIFDPOINTER));
        add(new Rule("Flash", 37385, EXIFIFDPOINTER));
        add(new Rule("FocalLength", 37386, EXIFIFDPOINTER));
        add(new Rule("SubjectArea", 37396, EXIFIFDPOINTER));
        add(new Rule("FlashEnergy", 41483, EXIFIFDPOINTER));
        //add(new Rule("SpatialFrequencyResponse", 41484, EXIFIFDPOINTER));
        add(new Rule("FocalPlaneXResolution", 41486, EXIFIFDPOINTER));
        add(new Rule("FocalPlaneYResolution", 41487, EXIFIFDPOINTER));
        add(new Rule("FocalPlaneResolutionUnit", 41488, EXIFIFDPOINTER));
        add(new Rule("SubjectLocation", 41492, EXIFIFDPOINTER));
        add(new Rule("ExposureIndex", 41493, EXIFIFDPOINTER));
        add(new Rule("SensingMethod", 41495, EXIFIFDPOINTER));
        add(new Rule("FileSource", 41728, EXIFIFDPOINTER));
        add(new Rule("SceneType", 41729, EXIFIFDPOINTER));
        //add(new Rule("CFAPattern", 41730, EXIFIFDPOINTER));
        add(new Rule("CustomRendered", 41985, EXIFIFDPOINTER));
        add(new Rule("ExposureMode", 41986, EXIFIFDPOINTER));
        add(new Rule("WhiteBalance", 41987, EXIFIFDPOINTER));
        add(new Rule("DigitalZoomRatio", 41988, EXIFIFDPOINTER));
        add(new Rule("FocalLengthIn35mmFilm", 41989, EXIFIFDPOINTER));
        add(new Rule("SceneCaptureType", 41990, EXIFIFDPOINTER));
        add(new Rule("GainControl", 41991, EXIFIFDPOINTER));
        add(new Rule("Contrast", 41992, EXIFIFDPOINTER));
        add(new Rule("Saturation", 41993, EXIFIFDPOINTER));
        add(new Rule("Sharpness", 41994, EXIFIFDPOINTER));
        add(new Rule("DeviceSettingDescription", 41995, EXIFIFDPOINTER));
        add(new Rule("SubjectDistanceRange", 41996, EXIFIFDPOINTER));
        add(new Rule("ImageUniqueID", 42016, EXIFIFDPOINTER));
        add(new Rule("GPSVersionID", 0, GPSINFOIFDPOINTER));
        add(new Rule("GPSSatellites", 8, GPSINFOIFDPOINTER));
        add(new Rule("GPSStatus", 9, GPSINFOIFDPOINTER));
        add(new Rule("GPSMeasureMode", 10, GPSINFOIFDPOINTER));
        add(new Rule("GPSDOP", 11, GPSINFOIFDPOINTER));
        add(new Rule("GPSSpeedRef", 12, GPSINFOIFDPOINTER));
        add(new Rule("GPSSpeed", 13, GPSINFOIFDPOINTER));
        add(new Rule("GPSTrackRef", 14, GPSINFOIFDPOINTER));
        add(new Rule("GPSTrack", 15, GPSINFOIFDPOINTER));
        add(new Rule("GPSMapDatum", 18, GPSINFOIFDPOINTER));
        add(new Rule("GPSDestLatitude", 20, GPSINFOIFDPOINTER));
        add(new Rule("GPSDestLongitude", 22, GPSINFOIFDPOINTER));
        add(new Rule("GPSDestBearingRef", 23, GPSINFOIFDPOINTER));
        add(new Rule("GPSDestBearing", 24, GPSINFOIFDPOINTER));
        add(new Rule("GPSDestDistanceRef", 25, GPSINFOIFDPOINTER));
        add(new Rule("GPSDestDistance", 26, GPSINFOIFDPOINTER));
        add(new Rule("GPSProcessingMethod", 27, GPSINFOIFDPOINTER));
        add(new Rule("GPSAreaInformation", 28, GPSINFOIFDPOINTER));
        add(new Rule("GPSDifferential", 30, GPSINFOIFDPOINTER));
        add(new Rule("SerialNumber", CANONCAMERASERIALNUMBER, MAKERNOTECANON));
        add(new Rule("Creator", CANONOWNERNAME, MAKERNOTECANON));
        add(new Rule("ImageUniqueID", CANONIMAGENUMBER, MAKERNOTECANON));
        add(new Rule("SerialNumber", PENTAXSERIALNUMBER, MAKERNOTEPENTAX));
        add(new Rule("ImageUniqueID", PENTAXFRAMENUMBER, MAKERNOTEPENTAX));
        add(new Rule(GPSINFOIFDPOINTER, LocationProperty, 1, 2, 3, 4));
        add(new Rule(GPSINFOIFDPOINTER, AltitudeProperty, 5, 6));
        add(new Rule(GPSINFOIFDPOINTER, GPSTimeProperty, 7, 29));
        add(new Rule(GPSINFOIFDPOINTER, ImgDirectionProperty, 16, 17));
    }

    private static void add(Rule rule)
    {
        map.add(rule.ifd, rule);
    }

    public static void populate(ExifAPP1 exif, Entity entity) throws IOException
    {
        for (IFD ifd : exif.getIFDs())
        {
            List<Rule> list = map.get(ifd.getIfdNum());
            for (Rule rule : list)
            {
                Interoperability[] ios = rule.get(ifd);
                if (ios != null)
                {
                    handle(entity, rule, ios);
                }
            }
        }
    }

    private static void handle(Entity entity, Rule rule, Interoperability... ioa) throws IOException
    {
        switch (rule.property)
        {
            case LocationProperty:
                handleLocation(entity, rule, ioa);
                break;
            case AltitudeProperty:
                handleAltitude(entity, rule, ioa);
                break;
            case GPSTimeProperty:
                handleGPSTime(entity, rule, ioa);
                break;
            case ImgDirectionProperty:
                handleImgDirection(entity, rule, ioa);
                break;
            case "DateTime":
            case "DateTimeOriginal":
            case "DateTimeDigitized":
                Object date = ioa[0].getValue();
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
            case "GPSVersionID":
                {
                    Byte[] bb = (Byte[]) ioa[0].getValue();
                    String id = String.format("%d.%d.%d.%d", bb[0], bb[1], bb[2], bb[3]);
                    entity.setUnindexedProperty(rule.property, id);
                }
                break;
            case "Flash":
            {
                int pp = ioa[0].getShortValue();
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
                if (ioa.length == 1)
                {
                    switch (ioa[0].type())
                    {
                        case BYTE:      // An 8-bit unsigned integer.,
                        case ASCII:     // An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL.,
                        case SHORT:     // A 16-bit (2-byte) unsigned integer,
                        case LONG:      // A 32-bit (4-byte) unsigned integer,
                        case SLONG:     // A 32-bit (4-byte) signed integer (2's complement notation),
                        case RATIONAL:  // Two LONGs. The first LONG is the numerator and the second LONG expresses the denominator.,
                        case SRATIONAL: // Two SLONGs. The first SLONG is the numerator and the second SLONG is the denominator.
                        case UNDEFINED: // An 8-bit byte that can take any value depending on the field definition,
                        {
                            if (ioa[0].count() == 1 || ioa[0].getValue() instanceof String)
                            {
                                entity.setUnindexedProperty(rule.property, ioa[0].getValue());
                            }
                        }
                        break;
                    default:
                        System.err.println(ioa[0]+" not supported");
                    }
                }
                else
                {
                    System.err.println(Arrays.toString(ioa)+" not supported");
                }
                break;
        }
    }

    private static void handleLocation(Entity entity, Rule rule, Interoperability[] ioa) throws IOException
    {
        if (rule.check(ioa, ASCII, RATIONAL, ASCII, RATIONAL))
        {
            String ns = (String) ioa[0].getValue();
            if (!("N".equals(ns) || "S".equals(ns)))
            {
                System.err.println("expecting N/S in "+rule);
                return;
            }
            Double[] lat = (Double[]) ioa[1].getValue();
            if (lat.length != 3)
            {
                System.err.println("Wrong latitude array length in "+rule);
                return;
            }
            double latitude = lat[0]+lat[1]/60+lat[2]/3600;
            if ("S".equals(ns))
            {
                latitude = -latitude;
            }
            String ew = (String) ioa[2].getValue();
            if (!("E".equals(ew) || "W".equals(ew)))
            {
                System.err.println("expecting E/W in "+rule);
                return;
            }
            Double[] lon = (Double[]) ioa[3].getValue();
            if (lon.length != 3)
            {
                System.err.println("Wrong longitude array length in "+rule);
                return;
            }
            double longitude = lon[0]+lon[1]/60+lon[2]/3600;
            if ("W".equals(ew))
            {
                longitude = -longitude;
            }
            GeoPt pt = new GeoPt((float)latitude, (float)longitude);
            entity.setProperty(rule.property, pt);
        }
    }

    private static void handleAltitude(Entity entity, Rule rule, Interoperability[] ioa) throws IOException
    {
        if (rule.check(ioa, BYTE, RATIONAL))
        {
            int ref = ioa[0].getShortValue();
            Double alt = (Double) ioa[1].getValue();
            switch (ref)
            {
                case 0:
                    break;
                case 1:
                    alt = -alt;
                    break;
                default:
                    System.err.println("expecting 0/1 in "+rule);
                    return;
            }
            entity.setProperty(rule.property, alt);
        }
    }

    private static void handleGPSTime(Entity entity, Rule rule, Interoperability[] ioa) throws IOException
    {
        if (rule.check(ioa, RATIONAL, ASCII))
        {
            Double[] time = (Double[]) ioa[0].getValue();
            if (time.length != 3)
            {
                System.err.println("Wrong time array length in "+rule);
                return;
            }
            String date = (String) ioa[1].getValue();
            if (date.length() != 10)
            {
                System.err.println("Wrong date '"+date+"' length in "+rule);
                return;
            }
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8));
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month-1);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, time[0].intValue());
            cal.set(Calendar.MINUTE, time[1].intValue());
            cal.set(Calendar.SECOND, time[2].intValue());
            entity.setProperty(rule.property, cal.getTime());
        }
    }

    private static void handleImgDirection(Entity entity, Rule rule, Interoperability[] ioa) throws IOException
    {
        if (rule.check(ioa, ASCII, RATIONAL))
        {
            String ref = (String) ioa[0].getValue();
            if (!("T".equals(ref) || "M".equals(ref)))
            {
                System.err.println("expecting M/T in "+rule);
                return;
            }
            Double dir = (Double) ioa[1].getValue();
            if ("M".equals(ref))
            {
                GeoPt location = (GeoPt) entity.getProperty(LocationProperty);
                if (location != null)
                {
                    if (geoMag == null)
                    {
                        geoMag = new TSAGeoMag();
                    }
                    double declination = geoMag.getDeclination(location.getLatitude(), location.getLongitude());
                    dir += declination;
                    dir %= 360;
                }
            }
            entity.setProperty(rule.property, dir);
        }
    }

    private static class Rule
    {
        String property;
        int[] tags;
        int ifd;

        public Rule(String property, int tag, int ifd)
        {
            this.property = property;
            this.tags = new int[] {tag};
            this.ifd = ifd;
        }

        public Rule(int ifd, String property, int... tags)
        {
            this.property = property;
            this.tags = tags;
            this.ifd = ifd;
        }

        public Interoperability[] get(IFD ifd)
        {
            Interoperability[] result = new Interoperability[tags.length];
            int ii = 0;
            for (int tag : tags)
            {
                result[ii] = ifd.get(tag);
                if (result[ii] == null)
                {
                    return null;
                }
                ii++;
            }
            return result;
        }
        public boolean check(Interoperability[] ioa, int... types)
        {
            if (ioa.length != tags.length)
            {
                System.err.println("wrong number of interoperabilities in "+this);
                return false;
            }
            if (types.length != tags.length)
            {
                System.err.println("wrong number of types in "+this);
                return false;
            }
            for (int ii=0;ii<ioa.length;ii++)
            {
                if (types[ii] != ioa[ii].type())
                {
                    System.err.println("wrong type "+ioa[ii]+" in "+ii+" ioa of "+this);
                    return false;
                }
            }
            return true;
        }
        @Override
        public String toString()
        {
            return property + "(" + Arrays.toString(tags) + ")";
        }
        
    }
}
