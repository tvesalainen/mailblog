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

package org.vesalainen.mailblog;

/**
 * @author Timo Vesalainen
 */
public interface BlogConstants 
{
    static final int CHUNKSIZE = 500;
    
    static final String BlogRipper = "X-BlogRipper-";
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
    
    static final String TitleProperty = "Title";
    static final String DescriptionProperty = "Description";
    static final String TimestampProperty = "Timestamp";
    static final String HtmlProperty = "Html";
    static final String SubjectProperty = "Subject";
    static final String SenderProperty = "Sender";
    static final String DateProperty = "Date";
    static final String CityProperty = "City";
    static final String CoordinateProperty = "Coordinate";
    static final String FilenameProperty = "Filename";
    static final String ContentTypeProperty = "ContentType";
    static final String EmailProperty = "Email";
    static final String NicknameProperty = "Nickname";
    static final String BlogAreaTemplateProperty = "BlogAreaTemplate";
    static final String BlogTemplateProperty = "BlogTemplate";
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
    static final String OriginalSizeProperty = "OriginalSize";
    static final String AttachmentsProperty = "Attachments";
    static final String PublishProperty = "Publish";
    static final String KeywordsProperty = "Keywords";
    static final String KeywordProperty = "Keyword";
    static final String CommentProperty = "Comment";
    static final String UserProperty = "User";
    static final String CoordinatesProperty = "Location";
    static final String FileProperty = "File";
    static final String CommonPlacemarksProperty = "CommonPlacemarks";
    static final String BlogIconProperty = "BlogIcon";
    static final String PlacemarkIconProperty = "PlacemarkIcon";
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
    
    static final String BaseKey = "base";
    
    static final String BlogIndex = "BlogIndex";
}
