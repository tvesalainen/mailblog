/*
 * Copyright (C) 2013 Timo Vesalainen
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

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Timo Vesalainen
 */
public class CachedContent implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private byte[] content;
    private String contentType;
    private String charset;
    private String eTag;
    private boolean isPrivate;

    public CachedContent(String content, String contentType, String charset, String eTag, boolean isPrivate) throws UnsupportedEncodingException
    {
        this(content.getBytes(charset), contentType, charset, eTag, isPrivate);
    }
    public CachedContent(byte[] content, String contentType, String charset, String eTag, boolean isPrivate)
    {
        this.content = content;
        this.contentType = contentType;
        this.charset = charset;
        this.eTag = eTag;
        this.isPrivate = isPrivate;
    }

    public void serve(HttpServletResponse res) throws IOException
    {
        res.setCharacterEncoding(charset);
        res.setContentType(contentType);
        res.setHeader("ETag", eTag);
        if (isPrivate)
        {
            res.setHeader("Cache-Control", "private");
        }
        else
        {
            res.setHeader("Cache-Control", "public");
        }
        res.getOutputStream().write(content);
    }

}
