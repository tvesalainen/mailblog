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
    static final String RootKind = "Root";
    static final String BlogKind = "Blog";
    static final String BlobKind = "Blob";
    static final String SettingsKind = "Settings";
    
    static final String TimestampProperty = "Timestamp";
    static final String HtmlProperty = "Html";
    static final String SubjectProperty = "Subject";
    static final String SenderProperty = "Sender";
    static final String SentDateProperty = "SentDate";
    static final String CidsProperty = "Cids";
    static final String BlobsProperty = "Blobs";
    static final String CityProperty = "City";
    static final String CoordinateProperty = "Coordinate";
    static final String FilenameProperty = "Filename";
    static final String ContentTypeProperty = "ContentType";
    static final String EmailProperty = "Email";
    static final String NicknameProperty = "Nickname";
    static final String TemplateProperty = "Template";
    static final String ConfirmEmailProperty = "ConfirmEmail";
    static final String LanguageProperty = "Language";
    
    static final String RemoveParameter = "remove";
    static final String BlogParameter = "blog";
    static final String BlobParameter = "blob-key";
    static final String CalendarParameter = "calendar";
    
    static final String RootKey = "base";
    static final String LatestKey = "latest";
    static final String CalendarKey = "calendar";
}
