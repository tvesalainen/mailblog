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

import java.io.IOException;
import java.io.StringReader;

/**
 * @author Timo Vesalainen
 */
class WebSafeReader extends StringReader 
{

    public WebSafeReader(String s)
    {
        super(s);
    }

    public String getFieldname() throws IOException
    {
        char[] buf = new char[2];
        int rc = read(buf);
        if (rc == -1)
        {
            return null;
        }
        String l = new String(buf);
        int len = Integer.parseInt(l, 16);
        buf = new char[len];
        rc = read(buf);
        if (rc == -1)
        {
            throw new IOException("eof while reading fieldname");
        }
        return new String(buf);
    }

    public String getWebsafe() throws IOException
    {
        char[] buf = new char[2];
        int rc = read(buf);
        if (rc == -1)
        {
            throw new IOException("eof while reading websafe length");
        }
        String l = new String(buf);
        int len = Integer.parseInt(l, 16);
        buf = new char[len];
        rc = read(buf);
        if (rc == -1)
        {
            throw new IOException("eof while reading websafe content");
        }
        return new String(buf);
    }

}
