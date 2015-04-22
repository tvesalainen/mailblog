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

/**
 *
 * @author tkv
 */
public class ExifConstants
{
    public static final String EXIF = "Exif";
    public static final String XMP = "http://ns.adobe.com/xap/1.0/";
    public static final int MARKER_PREFIX = 0xFF;
    public static final int SOI = 0xD8;     // Start of Image FFD8.H Start of compressed data
    public static final int APP0 = 0xE0;     //  Application Segment 0 FFE0.H Jfif attribute information
    public static final int APP1 = 0xE1;     //  Application Segment 1 FFE1.H Exif attribute information
    public static final int APP2 = 0xE2;     //  Application Segment 2 FFE2.H Exif extended data
    public static final int APPF = 0xEF;     //  Application Segment F FFEF.H Exif extended data
    public static final int DQT = 0xDB;     //  Define Quantization Table FFDB.H Quantization table definition
    public static final int DHT = 0xC4;     //  Define Huffman Table FFC4.H Huffman table definition
    public static final int DRI = 0xDD;     //  Define Restart Interoperability FFDD.H Restart Interoperability definition
    public static final int SOF = 0xC0;     //  Start of Frame FFC0.H Parameter data relating to frame
    public static final int SOS = 0xDA;     //  Start of Scan FFDA.H Parameters relating to components
    public static final int EOI = 0xD9;     //  End of Image FFD9.H End of compressed data

    static final int LITTLE_ENDIAN = 0x4949;
    static final int BIG_ENDIAN = 0x4D4D;

    static final int BYTE = 1;          // An 8-bit unsigned integer.,
    static final int ASCII = 2;         // An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL.,
    static final int SHORT = 3;         // A 16-bit (2-byte) unsigned integer,
    static final int LONG = 4;          // A 32-bit (4-byte) unsigned integer,
    static final int RATIONAL = 5;      // Two LONGs. The first LONG is the numerator and the second LONG expresses the denominator.,
    static final int UNDEFINED = 7;     // An 8-bit byte that can take any value depending on the field definition,
    static final int SLONG = 9;           // A 32-bit (4-byte) signed integer (2's complement notation),
    static final int SRATIONAL = 10;    // Two SLONGs. The first SLONG is the numerator and the second SLONG is the denominator.

    public static final int EXIFIFDPOINTER = 34665; //Exif IFD Pointer
    public static final int GPSINFOIFDPOINTER = 34853; //GPS Info IFD Pointer
    public static final int INTEROPERABILITYIFDPOINTER = 40965;     //Interoperability IFD Pointer

    public static final int IMAGEWIDTH = 256;       //Image width
    public static final int IMAGELENGTH = 257;      //Image height
    public static final int BITSPERSAMPLE = 258;    //Number of bits per component
    public static final int COMPRESSION = 259;      //Compression scheme
    public static final int PHOTOMETRICINTERPRETATION = 262;        //Pixel composition
    public static final int ORIENTATION = 274;      //Orientation of image
    public static final int SAMPLESPERPIXEL = 277;  //Number of components
    public static final int PLANARCONFIGURATION = 284;      //Image data arrangement
    public static final int YCBCRSUBSAMPLING = 530; //Subsampling ratio of Y to C
    public static final int YCBCRPOSITIONING = 531; //Y and C positioning
    public static final int XRESOLUTION = 282;      //Image resolution in width direction
    public static final int YRESOLUTION = 283;      //Image resolution in height direction
    public static final int RESOLUTIONUNIT = 296;   //Unit of X and Y resolution
    public static final int STRIPOFFSETS = 273;     //Image data location
    public static final int ROWSPERSTRIP = 278;     //Number of rows per strip
    public static final int STRIPBYTECOUNTS = 279;  //Bytes per compressed strip
    public static final int JPEGINTERCHANGEFORMAT = 513;    //Offset to JPEG SOI
    public static final int JPEGINTERCHANGEFORMATLENGTH = 514;      //Bytes of JPEG data
    public static final int TRANSFERFUNCTION = 301; //Transfer function
    public static final int WHITEPOINT = 318;       //White point chromaticity
    public static final int PRIMARYCHROMATICITIES = 319;    //Chromaticities of primaries
    public static final int YCBCRCOEFFICIENTS = 529;        //Color space transformation matrix coefficients
    public static final int REFERENCEBLACKWHITE = 532;      //Pair of black and white reference values
    public static final int DATETIME = 306; //File change date and time
    public static final int IMAGEDESCRIPTION = 270; //Image title
    public static final int MAKE = 271;     //Image input equipment manufacturer
    public static final int MODEL = 272;    //Image input equipment model
    public static final int SOFTWARE = 305; //Software used
    public static final int ARTIST = 315;   //Person who created the image
    public static final int COPYRIGHT = 33432;      //Copyright holder
    public static final int EXIFVERSION = 36864;    //Exif version
    public static final int FLASHPIXVERSION = 40960;        //Supported Flashpix version
    public static final int COLORSPACE = 40961;     //Color space information
    public static final int COMPONENTSCONFIGURATION = 37121;        //Meaning of each component
    public static final int COMPRESSEDBITSPERPIXEL = 37122; //Image compression mode
    public static final int PIXELXDIMENSION = 40962;        //Valid image width
    public static final int PIXELYDIMENSION = 40963;        //Valid image height
    public static final int MAKERNOTE = 37500;      //Manufacturer notes
    public static final int USERCOMMENT = 37510;    //User comments
    public static final int RELATEDSOUNDFILE = 40964;       //Related audio file
    public static final int DATETIMEORIGINAL = 36867;       //Date and time of original data generation
    public static final int DATETIMEDIGITIZED = 36868;      //Date and time of digital data generation
    public static final int SUBSECTIME = 37520;     //DateTime subseconds
    public static final int SUBSECTIMEORIGINAL = 37521;     //DateTimeOriginal subseconds
    public static final int SUBSECTIMEDIGITIZED = 37522;    //DateTimeDigitized subseconds
    public static final int IMAGEUNIQUEID = 42016;  //Unique image ID
    public static final int EXPOSURETIME = 33434;   //Exposure time
    public static final int FNUMBER = 33437;        //F number
    public static final int EXPOSUREPROGRAM = 34850;        //Exposure program
    public static final int SPECTRALSENSITIVITY = 34852;    //Spectral sensitivity
    public static final int ISOSPEEDRATINGS = 34855;        //ISO speed rating
    public static final int OECF = 34856;   //Optoelectric conversion factor
    public static final int SHUTTERSPEEDVALUE = 37377;      //Shutter speed
    public static final int APERTUREVALUE = 37378;  //Aperture
    public static final int BRIGHTNESSVALUE = 37379;        //Brightness
    public static final int EXPOSUREBIASVALUE = 37380;      //Exposure bias
    public static final int MAXAPERTUREVALUE = 37381;       //Maximum lens aperture
    public static final int SUBJECTDISTANCE = 37382;        //Subject distance
    public static final int METERINGMODE = 37383;   //Metering mode
    public static final int LIGHTSOURCE = 37384;    //Light source
    public static final int FLASH = 37385;  //Flash
    public static final int FOCALLENGTH = 37386;    //Lens focal length
    public static final int SUBJECTAREA = 37396;    //Subject area
    public static final int FLASHENERGY = 41483;    //Flash energy
    public static final int SPATIALFREQUENCYRESPONSE = 41484;       //Spatial frequency response
    public static final int FOCALPLANEXRESOLUTION = 41486;  //Focal plane X resolution
    public static final int FOCALPLANEYRESOLUTION = 41487;  //Focal plane Y resolution
    public static final int FOCALPLANERESOLUTIONUNIT = 41488;       //Focal plane resolution unit
    public static final int SUBJECTLOCATION = 41492;        //Subject location
    public static final int EXPOSUREINDEX = 41493;  //Exposure index
    public static final int SENSINGMETHOD = 41495;  //Sensing method
    public static final int FILESOURCE = 41728;     //File source
    public static final int SCENETYPE = 41729;      //Scene type
    public static final int CFAPATTERN = 41730;     //CFA pattern
    public static final int CUSTOMRENDERED = 41985; //Custom image processing
    public static final int EXPOSUREMODE = 41986;   //Exposure mode
    public static final int WHITEBALANCE = 41987;   //White balance
    public static final int DIGITALZOOMRATIO = 41988;       //Digital zoom ratio
    public static final int FOCALLENGTHIN35MMFILM = 41989;  //Focal length in 35 mm film
    public static final int SCENECAPTURETYPE = 41990;       //Scene capture type
    public static final int GAINCONTROL = 41991;    //Gain control
    public static final int CONTRAST = 41992;       //Contrast
    public static final int SATURATION = 41993;     //Saturation
    public static final int SHARPNESS = 41994;      //Sharpness
    public static final int DEVICESETTINGDESCRIPTION = 41995;       //Device settings description
    public static final int SUBJECTDISTANCERANGE = 41996;   //Subject distance range
    public static final int GPSVERSIONID = 0;       //GPS tag version
    public static final int GPSLATITUDEREF = 1;     //North or South Latitude
    public static final int GPSLATITUDE = 2;        //Latitude
    public static final int GPSLONGITUDEREF = 3;    //East or West Longitude
    public static final int GPSLONGITUDE = 4;       //Longitude
    public static final int GPSALTITUDEREF = 5;     //Altitude reference
    public static final int GPSALTITUDE = 6;        //Altitude
    public static final int GPSTIMESTAMP = 7;       //GPS time (atomic clock)
    public static final int GPSSATELLITES = 8;      //GPS satellites used for measurement
    public static final int GPSSTATUS = 9;  //GPS receiver status
    public static final int GPSMEASUREMODE = 10;    //GPS measurement mode
    public static final int GPSDOP = 11;    //Measurement precision
    public static final int GPSSPEEDREF = 12;       //Speed unit
    public static final int GPSSPEED = 13;  //Speed of GPS receiver
    public static final int GPSTRACKREF = 14;       //Reference for direction of movement
    public static final int GPSTRACK = 15;  //Direction of movement
    public static final int GPSIMGDIRECTIONREF = 16;        //Reference for direction of image
    public static final int GPSIMGDIRECTION = 17;   //Direction of image
    public static final int GPSMAPDATUM = 18;       //Geodetic survey data used
    public static final int GPSDESTLATITUDEREF = 19;        //Reference for latitude of destination
    public static final int GPSDESTLATITUDE = 20;   //Latitude of destination
    public static final int GPSDESTLONGITUDEREF = 21;       //Reference for longitude of destination
    public static final int GPSDESTLONGITUDE = 22;  //Longitude of destination
    public static final int GPSDESTBEARINGREF = 23; //Reference for bearing of destination
    public static final int GPSDESTBEARING = 24;    //Bearing of destination
    public static final int GPSDESTDISTANCEREF = 25;        //Reference for distance to destination
    public static final int GPSDESTDISTANCE = 26;   //Distance to destination
    public static final int GPSPROCESSINGMETHOD = 27;       //Name of GPS processing method
    public static final int GPSAREAINFORMATION = 28;        //Name of GPS area
    public static final int GPSDATESTAMP = 29;      //GPS date
    public static final int GPSDIFFERENTIAL = 30;   //GPS differential correction


    // Canon MakerNote Tags
    public static final String MAKECANON = "Canon";
    public static final int MAKERNOTECANON = 0x10001;

    public static final int CANONIMAGETYPE = 0x6;     // ASCII
    public static final int CANONFIRMWAREVERSION = 0x7;   // ASCII
    public static final int CANONIMAGENUMBER = 0x8;     // LONG
    public static final int CANONOWNERNAME = 0x9;   // ASCII
    public static final int CANONCAMERASERIALNUMBER = 0xc;  // LONG

    // PENTAX MakerNote Tags
    public static final String MAKEPENTAX = "PENTAX Corporation";
    public static final int MAKERNOTEPENTAX = 0x10002;

    public static final int PENTAXFRAMENUMBER = 0x0029;
    public static final int PENTAXSERIALNUMBER = 0x0229;

    public static final int IFD_oTH = -2;
    public static final int IFD_1ST = -1;
    
    
}
