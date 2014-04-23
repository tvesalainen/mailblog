/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class TagHelper
{
    private static Map<Integer,String> tags = new HashMap<Integer,String>();

    static
    {
        put("GPS Info IFD Pointer", "GpsIFDPointer", 34853);
        put("Exif IFD Pointer", "ExitIFDPointer", 34665);
        put("Interoperability IFD Pointer", "InteroperabilityIFDPointer", 40965);
        put("Image width", "ImageWidth", 256);
        put("Image height", "ImageLength", 257);
        put("Number of bits per component", "BitsPerSample", 258);
        put("Compression scheme", "Compression", 259);
        put("Pixel composition", "PhotometricInterpretation", 262);
        put("Orientation of image", "Orientation", 274);
        put("Number of components", "SamplesPerPixel", 277);
        put("Image data arrangement", "PlanarConfiguration", 284);
        put("Subsampling ratio of Y to C", "YCbCrSubSampling", 530);
        put("Y and C positioning", "YCbCrPositioning", 531);
        put("Image resolution in width direction", "XResolution", 282);
        put("Image resolution in height direction", "YResolution", 283);
        put("Unit of X and Y resolution", "ResolutionUnit", 296);
        put("Image data location", "StripOffsets", 273);
        put("Number of rows per strip", "RowsPerStrip", 278);
        put("Bytes per compressed strip", "StripByteCounts", 279);
        put("Offset to JPEG SOI", "JPEGInterchangeFormat", 513);
        put("Bytes of JPEG data", "JPEGInterchangeFormatLength", 514);
        put("Transfer function", "TransferFunction", 301);
        put("White point chromaticity", "WhitePoint", 318);
        put("Chromaticities of primaries", "PrimaryChromaticities", 319);
        put("Color space transformation matrix coefficients", "YCbCrCoefficients", 529);
        put("Pair of black and white reference values", "ReferenceBlackWhite", 532);
        put("File change date and time", "DateTime", 306);
        put("Image title", "ImageDescription", 270);
        put("Image input equipment manufacturer", "Make", 271);
        put("Image input equipment model", "Model", 272);
        put("Software used", "Software", 305);
        put("Person who created the image", "Artist", 315);
        put("Copyright holder", "Copyright", 33432);
        put("Exif version", "ExifVersion", 36864);
        put("Supported Flashpix version", "FlashpixVersion", 40960);
        put("Color space information", "ColorSpace", 40961);
        put("Meaning of each component", "ComponentsConfiguration", 37121);
        put("Image compression mode", "CompressedBitsPerPixel", 37122);
        put("Valid image width", "PixelXDimension", 40962);
        put("Valid image height", "PixelYDimension", 40963);
        put("Manufacturer notes", "MakerNote", 37500);
        put("User comments", "UserComment", 37510);
        put("Related audio file", "RelatedSoundFile", 40964);
        put("Date and time of original data generation", "DateTimeOriginal", 36867);
        put("Date and time of digital data generation", "DateTimeDigitized", 36868);
        put("DateTime subseconds", "SubSecTime", 37520);
        put("DateTimeOriginal subseconds", "SubSecTimeOriginal", 37521);
        put("DateTimeDigitized subseconds", "SubSecTimeDigitized", 37522);
        put("Unique image ID", "ImageUniqueID", 42016);
        put("Exposure time", "ExposureTime", 33434);
        put("F number", "FNumber", 33437);
        put("Exposure program", "ExposureProgram", 34850);
        put("Spectral sensitivity", "SpectralSensitivity", 34852);
        put("ISO speed rating", "ISOSpeedRatings", 34855);
        put("Optoelectric conversion factor", "OECF", 34856);
        put("Shutter speed", "ShutterSpeedValue", 37377);
        put("Aperture", "ApertureValue", 37378);
        put("Brightness", "BrightnessValue", 37379);
        put("Exposure bias", "ExposureBiasValue", 37380);
        put("Maximum lens aperture", "MaxApertureValue", 37381);
        put("Subject distance", "SubjectDistance", 37382);
        put("Metering mode", "MeteringMode", 37383);
        put("Light source", "LightSource", 37384);
        put("Flash", "Flash", 37385);
        put("Lens focal length", "FocalLength", 37386);
        put("Subject area", "SubjectArea", 37396);
        put("Flash energy", "FlashEnergy", 41483);
        put("Spatial frequency response", "SpatialFrequencyResponse", 41484);
        put("Focal plane X resolution", "FocalPlaneXResolution", 41486);
        put("Focal plane Y resolution", "FocalPlaneYResolution", 41487);
        put("Focal plane resolution unit", "FocalPlaneResolutionUnit", 41488);
        put("Subject location", "SubjectLocation", 41492);
        put("Exposure index", "ExposureIndex", 41493);
        put("Sensing method", "SensingMethod", 41495);
        put("File source", "FileSource", 41728);
        put("Scene type", "SceneType", 41729);
        put("CFA pattern", "CFAPattern", 41730);
        put("Custom image processing", "CustomRendered", 41985);
        put("Exposure mode", "ExposureMode", 41986);
        put("White balance", "WhiteBalance", 41987);
        put("Digital zoom ratio", "DigitalZoomRatio", 41988);
        put("Focal length in 35 mm film", "FocalLengthIn35mmFilm", 41989);
        put("Scene capture type", "SceneCaptureType", 41990);
        put("Gain control", "GainControl", 41991);
        put("Contrast", "Contrast", 41992);
        put("Saturation", "Saturation", 41993);
        put("Sharpness", "Sharpness", 41994);
        put("Device settings description", "DeviceSettingDescription", 41995);
        put("Subject distance range", "SubjectDistanceRange", 41996);
        put("GPS tag version", "GPSVersionID", 0);
        put("North or South Latitude", "GPSLatitudeRef", 1);
        put("Latitude", "GPSLatitude", 2);
        put("East or West Longitude", "GPSLongitudeRef", 3);
        put("Longitude", "GPSLongitude", 4);
        put("Altitude reference", "GPSAltitudeRef", 5);
        put("Altitude", "GPSAltitude", 6);
        put("GPS time (atomic clock)", "GPSTimeStamp", 7);
        put("GPS satellites used for measurement", "GPSSatellites", 8);
        put("GPS receiver status", "GPSStatus", 9);
        put("GPS measurement mode", "GPSMeasureMode", 10);
        put("Measurement precision", "GPSDOP", 11);
        put("Speed unit", "GPSSpeedRef", 12);
        put("Speed of GPS receiver", "GPSSpeed", 13);
        put("Reference for direction of movement", "GPSTrackRef", 14);
        put("Direction of movement", "GPSTrack", 15);
        put("Reference for direction of image", "GPSImgDirectionRef", 16);
        put("Direction of image", "GPSImgDirection", 17);
        put("Geodetic survey data used", "GPSMapDatum", 18);
        put("Reference for latitude of destination", "GPSDestLatitudeRef", 19);
        put("Latitude of destination", "GPSDestLatitude", 20);
        put("Reference for longitude of destination", "GPSDestLongitudeRef", 21);
        put("Longitude of destination", "GPSDestLongitude", 22);
        put("Reference for bearing of destination", "GPSDestBearingRef", 23);
        put("Bearing of destination", "GPSDestBearing", 24);
        put("Reference for distance to destination", "GPSDestDistanceRef", 25);
        put("Distance to destination", "GPSDestDistance", 26);
        put("Name of GPS processing method", "GPSProcessingMethod", 27);
        put("Name of GPS area", "GPSAreaInformation", 28);
        put("GPS date", "GPSDateStamp", 29);
        put("GPS differential correction", "GPSDifferential", 30);

    }

    public static void put(String tagname, String fieldname, int tag)
    {
        String old = tags.put(tag, tagname);
        if (old != null)
        {
            System.err.println(old+" DUPLICATE");
        }
    }

    public static String getName(int tag)
    {
        String name = tags.get(tag);
        if (name != null)
        {
            return name;
        }
        return "tag "+tag+" is unknown!";
    }
}
