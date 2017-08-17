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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Exif2Entity
{
    private static MapList<Integer,Rule> map = new HashMapList<>();
    private static TSAGeoMag geoMag;
    
    static
    {
        add(new Rule(ImageWidthProperty, 256, IFD_oTH));
        add(new Rule(ImageLengthProperty, 257, IFD_oTH));
        add(new Rule(BitsPerSampleProperty, 258, IFD_oTH));
        add(new Rule(CompressionProperty, 259, IFD_oTH));
        add(new Rule(PhotometricInterpretationProperty, 262, IFD_oTH));
        add(new Rule(OrientationProperty, 274, IFD_oTH));
        add(new Rule(SamplesPerPixelProperty, 277, IFD_oTH));
        add(new Rule(PlanarConfigurationProperty, 284, IFD_oTH));
        add(new Rule(YCbCrSubSamplingProperty, 530, IFD_oTH));
        add(new Rule(YCbCrPositioningProperty, 531, IFD_oTH));
        add(new Rule(XResolutionProperty, 282, IFD_oTH));
        add(new Rule(YResolutionProperty, 283, IFD_oTH));
        add(new Rule(ResolutionUnitProperty, 296, IFD_oTH));
        add(new Rule(TransferFunctionProperty, 301, IFD_oTH));
        add(new Rule(WhitePointProperty, 318, IFD_oTH));
        add(new Rule(PrimaryChromaticitiesProperty, 319, IFD_oTH));
        add(new Rule(YCbCrCoefficientsProperty, 529, IFD_oTH));
        add(new Rule(ReferenceBlackWhiteProperty, 532, IFD_oTH));
        add(new Rule(DateTimeProperty, 306, IFD_oTH));
        add(new Rule(ImageDescriptionProperty, 270, IFD_oTH));
        add(new Rule(MakeProperty, 271, IFD_oTH));
        add(new Rule(ModelProperty, 272, IFD_oTH));
        add(new Rule(SoftwareProperty, 305, IFD_oTH));
        add(new Rule(ArtistProperty, 315, IFD_oTH));
        add(new Rule(CopyrightProperty, 33432, IFD_oTH));
        add(new Rule(ExifVersionProperty, 36864, EXIFIFDPOINTER));
        add(new Rule(FlashpixVersionProperty, 40960, EXIFIFDPOINTER));
        add(new Rule(ColorSpaceProperty, 40961, EXIFIFDPOINTER));
        add(new Rule(ComponentsConfigurationProperty, 37121, EXIFIFDPOINTER));
        add(new Rule(CompressedBitsPerPixelProperty, 37122, EXIFIFDPOINTER));
        add(new Rule(PixelXDimensionProperty, 40962, EXIFIFDPOINTER));
        add(new Rule(PixelYDimensionProperty, 40963, EXIFIFDPOINTER));
        add(new Rule(MakerNoteProperty, 37500, EXIFIFDPOINTER));
        add(new Rule(UserCommentProperty, 37510, EXIFIFDPOINTER));
        add(new Rule(RelatedSoundFileProperty, 40964, EXIFIFDPOINTER));
        add(new Rule(DateTimeOriginalProperty, 36867, EXIFIFDPOINTER));
        add(new Rule(DateTimeDigitizedProperty, 36868, EXIFIFDPOINTER));
        add(new Rule(ExposureTimeProperty, 33434, EXIFIFDPOINTER));
        add(new Rule(FNumberProperty, 33437, EXIFIFDPOINTER));
        add(new Rule(ExposureProgramProperty, 34850, EXIFIFDPOINTER));
        add(new Rule(SpectralSensitivityProperty, 34852, EXIFIFDPOINTER));
        add(new Rule(ISOSpeedRatingsProperty, 34855, EXIFIFDPOINTER));
        //add(new Rule(OECFProperty, 34856, EXIFIFDPOINTER));
        add(new Rule(ShutterSpeedValueProperty, 37377, EXIFIFDPOINTER));
        add(new Rule(ApertureValueProperty, 37378, EXIFIFDPOINTER));
        add(new Rule(BrightnessValueProperty, 37379, EXIFIFDPOINTER));
        add(new Rule(ExposureBiasValueProperty, 37380, EXIFIFDPOINTER));
        add(new Rule(MaxApertureValueProperty, 37381, EXIFIFDPOINTER));
        add(new Rule(SubjectDistanceProperty, 37382, EXIFIFDPOINTER));
        add(new Rule(MeteringModeProperty, 37383, EXIFIFDPOINTER));
        add(new Rule(LightSourceProperty, 37384, EXIFIFDPOINTER));
        add(new Rule(FlashProperty, 37385, EXIFIFDPOINTER));
        add(new Rule(FocalLengthProperty, 37386, EXIFIFDPOINTER));
        add(new Rule(SubjectAreaProperty, 37396, EXIFIFDPOINTER));
        add(new Rule(FlashEnergyProperty, 41483, EXIFIFDPOINTER));
        //add(new Rule(SpatialFrequencyResponse, 41484, EXIFIFDPOINTER));
        add(new Rule(FocalPlaneXResolutionProperty, 41486, EXIFIFDPOINTER));
        add(new Rule(FocalPlaneYResolutionProperty, 41487, EXIFIFDPOINTER));
        add(new Rule(FocalPlaneResolutionUnitProperty, 41488, EXIFIFDPOINTER));
        add(new Rule(SubjectLocationProperty, 41492, EXIFIFDPOINTER));
        add(new Rule(ExposureIndexProperty, 41493, EXIFIFDPOINTER));
        add(new Rule(SensingMethodProperty, 41495, EXIFIFDPOINTER));
        add(new Rule(FileSourceProperty, 41728, EXIFIFDPOINTER));
        add(new Rule(SceneTypeProperty, 41729, EXIFIFDPOINTER));
        //add(new Rule(CFAPatternProperty, 41730, EXIFIFDPOINTER));
        add(new Rule(CustomRenderedProperty, 41985, EXIFIFDPOINTER));
        add(new Rule(ExposureModeProperty, 41986, EXIFIFDPOINTER));
        add(new Rule(WhiteBalanceProperty, 41987, EXIFIFDPOINTER));
        add(new Rule(DigitalZoomRatioProperty, 41988, EXIFIFDPOINTER));
        add(new Rule(FocalLengthIn35mmFilmProperty, 41989, EXIFIFDPOINTER));
        add(new Rule(SceneCaptureTypeProperty, 41990, EXIFIFDPOINTER));
        add(new Rule(GainControlProperty, 41991, EXIFIFDPOINTER));
        add(new Rule(ContrastProperty, 41992, EXIFIFDPOINTER));
        add(new Rule(SaturationProperty, 41993, EXIFIFDPOINTER));
        add(new Rule(SharpnessProperty, 41994, EXIFIFDPOINTER));
        add(new Rule(DeviceSettingDescriptionProperty, 41995, EXIFIFDPOINTER));
        add(new Rule(SubjectDistanceRangeProperty, 41996, EXIFIFDPOINTER));
        add(new Rule(ImageUniqueIDProperty, 42016, EXIFIFDPOINTER));
        add(new Rule(GPSVersionIDProperty, 0, GPSINFOIFDPOINTER));
        add(new Rule(GPSSatellitesProperty, 8, GPSINFOIFDPOINTER));
        add(new Rule(GPSStatusProperty, 9, GPSINFOIFDPOINTER));
        add(new Rule(GPSMeasureModeProperty, 10, GPSINFOIFDPOINTER));
        add(new Rule(GPSDOPProperty, 11, GPSINFOIFDPOINTER));
        add(new Rule(GPSSpeedRefProperty, 12, GPSINFOIFDPOINTER));
        add(new Rule(GPSSpeedProperty, 13, GPSINFOIFDPOINTER));
        add(new Rule(GPSTrackRefProperty, 14, GPSINFOIFDPOINTER));
        add(new Rule(GPSTrackProperty, 15, GPSINFOIFDPOINTER));
        add(new Rule(GPSMapDatumProperty, 18, GPSINFOIFDPOINTER));
        add(new Rule(GPSDestLatitudeProperty, 20, GPSINFOIFDPOINTER));
        add(new Rule(GPSDestLongitudeProperty, 22, GPSINFOIFDPOINTER));
        add(new Rule(GPSDestBearingRefProperty, 23, GPSINFOIFDPOINTER));
        add(new Rule(GPSDestBearingProperty, 24, GPSINFOIFDPOINTER));
        add(new Rule(GPSDestDistanceRefProperty, 25, GPSINFOIFDPOINTER));
        add(new Rule(GPSDestDistanceProperty, 26, GPSINFOIFDPOINTER));
        add(new Rule(GPSProcessingMethodProperty, 27, GPSINFOIFDPOINTER));
        add(new Rule(GPSAreaInformationProperty, 28, GPSINFOIFDPOINTER));
        add(new Rule(GPSDifferentialProperty, 30, GPSINFOIFDPOINTER));
        add(new Rule(SerialNumberProperty, CANONCAMERASERIALNUMBER, MAKERNOTECANON));
        add(new Rule(CreatorProperty, CANONOWNERNAME, MAKERNOTECANON));
        add(new Rule(ImageUniqueIDProperty, CANONIMAGENUMBER, MAKERNOTECANON));
        add(new Rule(SerialNumberProperty, PENTAXSERIALNUMBER, MAKERNOTEPENTAX));
        add(new Rule(ImageUniqueIDProperty, PENTAXFRAMENUMBER, MAKERNOTEPENTAX));
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
            case DateTimeProperty:
            case DateTimeOriginalProperty:
            case DateTimeDigitizedProperty:
                Object date = ioa[0].getValue();
                if (date instanceof Date)
                {
                    if (DateTimeOriginalProperty.equals(rule.property))
                    {
                        entity.setProperty(rule.property, date);
                    }
                    else
                    {
                        entity.setUnindexedProperty(rule.property, date);
                    }
                }
                break;
            case GPSVersionIDProperty:
                {
                    Byte[] bb = (Byte[]) ioa[0].getValue();
                    String id = String.format("%d.%d.%d.%d", bb[0], bb[1], bb[2], bb[3]);
                    entity.setUnindexedProperty(rule.property, id);
                }
                break;
            case FlashProperty:
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
            System.err.println("public static final String "+property+"Property = \""+property+"\";");
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
