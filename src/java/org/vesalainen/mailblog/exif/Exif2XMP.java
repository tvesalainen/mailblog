/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.options.PropertyOptions;
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
public class Exif2XMP
{
    private static Map<Integer,Map<Integer,List<Rule>>> map = new HashMap<Integer,Map<Integer,List<Rule>>>();
    
    static
    {
        add(new Rule(XMPConst.NS_TIFF, "ImageWidth", 256, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "ImageLength", 257, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "BitsPerSample", 258, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Compression", 259, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "PhotometricInterpretation", 262, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Orientation", 274, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "SamplesPerPixel", 277, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "PlanarConfiguration", 284, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "YCbCrSubSampling", 530, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "YCbCrPositioning", 531, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "XResolution", 282, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "YResolution", 283, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "ResolutionUnit", 296, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "TransferFunction", 301, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "WhitePoint", 318, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "PrimaryChromaticities", 319, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "YCbCrCoefficients", 529, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "ReferenceBlackWhite", 532, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "DateTime", 306, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_XMP, "ModifyDate", 306, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "ImageDescription", 270, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_DC, "description", 270, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Make", 271, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Model", 272, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Software", 305, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_XMP, "CreatorTool", 305, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Artist", 315, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_DC, "creator", 315, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_TIFF, "Copyright", 33432, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_DC, "rights", 33432, ExifConstants.IFD_oTH));
        add(new Rule(XMPConst.NS_EXIF, "ExifVersion", 36864, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FlashpixVersion", 40960, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ColorSpace", 40961, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ComponentsConfiguration", 37121, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "CompressedBitsPerPixel", 37122, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "PixelXDimension", 40962, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "PixelYDimension", 40963, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "UserComment", 37510, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "RelatedSoundFile", 40964, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "DateTimeOriginal", 36867, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "DateTimeDigitized", 36868, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ExposureTime", 33434, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FNumber", 33437, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ExposureProgram", 34850, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SpectralSensitivity", 34852, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ISOSpeedRatings", 34855, ExifConstants.EXIFIFDPOINTER));
        //add(new Rule(XMPConst.NS_EXIF, "OECF", 34856, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ShutterSpeedValue", 37377, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ApertureValue", 37378, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "BrightnessValue", 37379, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ExposureBiasValue", 37380, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "MaxApertureValue", 37381, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SubjectDistance", 37382, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "MeteringMode", 37383, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "LightSource", 37384, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "Flash", 37385, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FocalLength", 37386, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SubjectArea", 37396, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FlashEnergy", 41483, ExifConstants.EXIFIFDPOINTER));
        //add(new Rule(XMPConst.NS_EXIF, "SpatialFrequencyResponse", 41484, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FocalPlaneXResolution", 41486, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FocalPlaneYResolution", 41487, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FocalPlaneResolutionUnit", 41488, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SubjectLocation", 41492, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ExposureIndex", 41493, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SensingMethod", 41495, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FileSource", 41728, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SceneType", 41729, ExifConstants.EXIFIFDPOINTER));
        //add(new Rule(XMPConst.NS_EXIF, "CFAPattern", 41730, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "CustomRendered", 41985, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ExposureMode", 41986, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "WhiteBalance", 41987, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "DigitalZoomRatio", 41988, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "FocalLengthIn35mmFilm", 41989, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SceneCaptureType", 41990, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GainControl", 41991, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "Contrast", 41992, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "Saturation", 41993, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "Sharpness", 41994, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "DeviceSettingDescription", 41995, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "SubjectDistanceRange", 41996, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "ImageUniqueID", 42016, ExifConstants.EXIFIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSVersionID", 0, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSLatitude", 2, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSLongitude", 4, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSAltitudeRef", 5, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSAltitude", 6, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSTimeStamp", 29, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSSatellites", 8, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSStatus", 9, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSMeasureMode", 10, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDOP", 11, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSSpeedRef", 12, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSSpeed", 13, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSTrackRef", 14, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSTrack", 15, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSImgDirectionRef", 16, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSImgDirection", 17, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSMapDatum", 18, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDestLatitude", 20, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDestLongitude", 22, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDestBearingRef", 23, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDestBearing", 24, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDestDistanceRef", 25, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDestDistance", 26, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSProcessingMethod", 27, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSAreaInformation", 28, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF, "GPSDifferential", 30, ExifConstants.GPSINFOIFDPOINTER));
        add(new Rule(XMPConst.NS_EXIF_AUX, "SerialNumber", ExifConstants.CANONCAMERASERIALNUMBER, ExifConstants.MAKERNOTECANON));
        add(new Rule(XMPConst.NS_DC, "creator", ExifConstants.CANONOWNERNAME, ExifConstants.MAKERNOTECANON));
        add(new Rule(XMPConst.NS_EXIF, "ImageUniqueID", ExifConstants.CANONIMAGENUMBER, ExifConstants.MAKERNOTECANON));
        add(new Rule(XMPConst.NS_EXIF, "SerialNumber", ExifConstants.PENTAXSERIALNUMBER, ExifConstants.MAKERNOTEPENTAX));
        add(new Rule(XMPConst.NS_EXIF, "ImageUniqueID", ExifConstants.PENTAXFRAMENUMBER, ExifConstants.MAKERNOTEPENTAX));
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

    public static void populate(ExifAPP1 exif, XMPAPP1 xmpApp1) throws XMPException, IOException
    {
        XMPMeta xmp = xmpApp1.getXmp();
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
                            handle(xmp, rule, ioa);
                        }
                    }
                }
            }
        }
    }

    private static void handle(XMPMeta xmp, Rule rule, Interoperability ioa) throws XMPException, IOException
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
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date) date);
                        xmp.setPropertyCalendar(rule.namespace, rule.property, cal);
                    }
                    break;
                case 270:
                case 33432:
                case 37510:
                    xmp.setLocalizedText(rule.namespace, rule.property, null, "x-default", strValue);
                    break;
                case 0:
                    if (rule.ifd == ExifConstants.GPSINFOIFDPOINTER)
                    {
                        Byte[] bb = (Byte[]) value;
                        String id = String.format("%d.%d.%d.%d", bb[0], bb[1], bb[2], bb[3]);
                        xmp.setProperty(rule.namespace, rule.property, id);
                    }
                    break;
                case 315:
                case ExifConstants.CANONOWNERNAME:
                    if (rule.ifd == ExifConstants.MAKERNOTE)
                    {
                        PropertyOptions arrOpt = new PropertyOptions(PropertyOptions.ARRAY_ORDERED);
                        xmp.appendArrayItem(rule.namespace, rule.property, arrOpt, strValue, null);
                    }
                    break;
                case ExifConstants.FLASH:
                {
                    int pp = ioa.getShortValue();
                    switch (pp & 0x1)
                    {
                        case 1 :
                            xmp.setStructField(rule.namespace, rule.property, rule.namespace, "Fired", "True");
                            break;
                        case 0 :
                            xmp.setStructField(rule.namespace, rule.property, rule.namespace, "Fired", "False");
                            break;
                    }
                    switch ((pp>>4) & 0x1)
                    {
                        case 0 :
                            xmp.setStructField(rule.namespace, rule.property, rule.namespace, "Function", "True");
                            break;
                        default:
                            xmp.setStructField(rule.namespace, rule.property, rule.namespace, "Function", "False");
                            break;
                    }
                    xmp.setStructField(rule.namespace, rule.property, rule.namespace, "Mode", String.valueOf((pp>>3) & 0x3));
                    switch ((pp>>5) & 0x1)
                    {
                        case 0 :
                            xmp.setStructField(rule.namespace, rule.property, rule.namespace, "RedEyeMode", "False");
                            break;
                        case 1 :
                            xmp.setStructField(rule.namespace, rule.property, rule.namespace, "RedEyeMode", "True");
                            break;
                    }
                    xmp.setStructField(rule.namespace, rule.property, rule.namespace, "Return", String.valueOf((pp>>1) & 0x3));
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
                                xmp.setProperty(rule.namespace, rule.property, strValue);
                            }
                            else
                            {
                                PropertyOptions arrOpt = new PropertyOptions(PropertyOptions.ARRAY_ORDERED);
                                /*
                                switch (tag)
                                {
                                    case 258:
                                    case 301:
                                    case 318:
                                    case 319:
                                    case 529:
                                    case 532:
                                    case 34855:
                                    case 37396:
                                    case 41492:
                                        arrOpt = new PropertyOptions(PropertyOptions.ARRAY);
                                }
                                 */
                                Object[] arr = (Object[]) ioa.getValue();
                                for (Object oo : arr)
                                {
                                    xmp.appendArrayItem(rule.namespace, rule.property, arrOpt, oo.toString(), null);
                                }
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
        String namespace;
        String property;
        int tag;
        int ifd;

        public Rule(String namespace, String property, int tag, int ifd)
        {
            this.namespace = namespace;
            this.property = property;
            this.tag = tag;
            this.ifd = ifd;
        }
    }
}
