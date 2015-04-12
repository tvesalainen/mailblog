/*
 * Copyright (C) 2012 Timo Vesalainen
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

package org.vesalainen.mailblog;

/**
 * @author Timo Vesalainen
 */
public interface BlogConstants 
{
    static final int CHUNKSIZE = 500;
    
    static final String BlogRipper = "X-BlogRipper-";
    static final String ISO8601Format = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
    static final String RFC1123Format = "EEE, dd MMM yyyy HH:mm:ss z";
    
    static final String RootKind = "Root";
    static final String BlogKind = "Blog";
    static final String BlobKind = "Blob";
    static final String PageKind = "Page";
    static final String PageBackupKind = "PageBackup";
    static final String MetadataKind = "Metadata";
    static final String SettingsKind = "Settings";
    static final String KeywordKind = "Settings";
    static final String CommentsKind = "Comments";
    static final String PlacemarkKind = "Placemarks";
    static final String TrackKind = "Track";
    static final String TrackSeqKind = "TrackSeq";
    static final String TrackPointKind = "TrackPoint";
    static final String AttachmentsKind = "Attachments";
    
    static final String TitleProperty = "Title";
    static final String DescriptionProperty = "Description";
    static final String ImageProperty = "Image";
    static final String TimestampProperty = "Timestamp";
    static final String HtmlProperty = "Html";
    static final String SubjectProperty = "Subject";
    static final String SenderProperty = "Sender";
    static final String DateProperty = "Date";
    static final String CityProperty = "City";
    static final String FilenameProperty = "Filename";
    static final String ContentTypeProperty = "ContentType";
    static final String EmailProperty = "Email";
    static final String NicknameProperty = "Nickname";
    static final String BlogTemplateProperty = "BlogTemplate";
    static final String SearchResultsTemplateProperty = "SearchResultsTemplate";
    static final String CommentTemplateProperty = "CommentTemplate";
    static final String PublishImmediatelyProperty = "PublishImmediately";
    static final String LocaleProperty = "Locale";
    static final String TimeZoneProperty = "TimeZone";
    static final String PicMaxHeightProperty = "PicMaxHeight";
    static final String PicMaxWidthProperty = "PicMaxWidth";
    static final String FixPicProperty = "FixPic";
    static final String ShowCountProperty = "ShowCount";
    static final String PageProperty = "Page";
    static final String WebSizeProperty = "WebSize";
    static final String WebSizeWidthProperty = "WebSizeWidth";
    static final String WebSizeHeightProperty = "WebSizeHeight";
    static final String OriginalSizeProperty = "OriginalSize";
    static final String OriginalSizeWidthProperty = "OriginalSizeWidth";
    static final String OriginalSizeHeightProperty = "OriginalSizeHeight";
    static final String AttachmentsProperty = "Attachments";
    static final String PublishProperty = "Publish";
    static final String KeywordsProperty = "Keywords";
    static final String KeywordProperty = "Keyword";
    static final String CommentProperty = "Comment";
    static final String UserProperty = "User";
    static final String LocationProperty = "Location";
    static final String AltitudeProperty = "Altitude";
    static final String FileProperty = "File";
    static final String CommonPlacemarksProperty = "CommonPlacemarks";
    static final String BlogIconProperty = "BlogIcon";
    static final String ImageIconProperty = "ImageIcon";
    static final String HiLiteIconProperty = "HiLiteIcon";
    static final String SpotOkIconProperty = "SpotOkIcon";
    static final String SpotCustomIconProperty = "SpotCustomIcon";
    static final String SpotHelpIconProperty = "SpotHelpIcon";
    static final String DontSendEmailProperty = "DontSendEmail";
    static final String SouthWestProperty = "SouthWest";
    static final String NorthEastProperty = "NorthEast";
    static final String TrackBearingToleranceProperty = "TrackBearingTolerance";
    static final String TrackMinDistanceProperty = "TrackMinDistance";
    static final String TrackMaxSpeedProperty = "TrackMaxSpeed";
    static final String EyeAltitudeProperty = "EyeAltitude";
    static final String BeginProperty = "Begin";
    static final String EndProperty = "End";
    static final String PathColorProperty = "PathColor";
    static final String TrackColorProperty = "TrackColor";
    static final String MinOpaqueProperty = "MinOpaque";
    static final String NameProperty = "Name";
    static final String GPSTimeProperty = "GPSTime";
    static final String ImgDirectionProperty = "ImgDirection";
    // Maidenhead properties
    static final String FieldProperty = "Field";
    static final String SquareProperty = "Square";
    static final String SubsquareProperty = "Subsquare";
    
    static final String RemoveParameter = "remove";
    static final String BlogParameter = "blog";
    static final String BlobParameter = "blob-key";
    static final String CalendarParameter = "calendar";
    static final String OriginalParameter = "original";
    static final String PathParameter = "path";
    static final String PageParameter = "page";
    static final String AddParameter = "add";
    static final String Sha1Parameter = "sha1";
    static final String MetadataParameter = "metadata";
    static final String SizeParameter = "size";
    static final String WidthParameter = "width";
    static final String HeightParameter = "height";
    static final String NamespaceParameter = "namespace";
    static final String CursorParameter = "cursor";
    static final String NewParameter = "new";
    static final String BackupParameter = "backup";
    static final String SearchParameter = "search";
    static final String KeywordsParameter = "keywords";
    static final String CommentParameter = "comment";
    static final String CommentsParameter = "comments";
    static final String RemoveCommentParameter = "removecomment";
    static final String AllParameter = "all";
    static final String ActionParameter = "action";
    static final String AuthParameter = "auth";
    static final String PublishParameter = "publish";
    static final String BoundingBoxParameter = "bbox";
    static final String EmailParameter = "email";
    static final String KeyParameter = "key";
    static final String LookAtParameter = "lookAt";
    static final String JSONParameter = "json";
    static final String LatitudeParameter = "latitude";
    static final String LongitudeParameter = "longitude";
    static final String TimestampParameter = "timestamp";
    static final String MaidenheadLocatorParameter = "maidenhead";
    
    static final String StylePath = "/style.kmz";
    static final String PlacemarkPath = "/placemarkPath.kmz";
    static final String TrackSeqPath = "/trackSeq.kmz";
    static final String BlogLocationPath = "/blogLocation.kmz";
    
    static final String BaseKey = "base";
    
    static final String BlogIndex = "BlogIndex";
}
