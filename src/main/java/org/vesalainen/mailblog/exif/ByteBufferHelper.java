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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author tkv
 */
public class ByteBufferHelper
{
    public static final ByteBuffer createCopy(ByteBuffer buffer)
    {
        buffer.rewind();
        ByteBuffer copy = ByteBuffer.allocate(buffer.limit());
        copy.order(buffer.order());
        copy.put(buffer);
        return copy;
    }
    public static final ByteBuffer slicePosition(ByteBuffer base, int position)
    {
        int safe = base.position();
        base.position(position);
        ByteBuffer buffer = base.slice();
        buffer.order(base.order());
        base.position(safe);
        return buffer;
    }
    public static final ByteBuffer sliceLength(ByteBuffer base, int position, int length)
    {
        int safe = base.position();
        base.position(position);
        ByteBuffer buffer = sliceLength(base, length);
        base.position(safe);
        return buffer;
    }
    public static final ByteBuffer sliceLength(ByteBuffer base, int length)
    {
        ByteBuffer buffer = base.slice();
        buffer.order(base.order());
        buffer.limit(length);
        return buffer;
    }
    public static final void consumeByte(ByteBuffer buffer, int expected) throws ExifException
    {
        int b = buffer.get() & 0xff;
        if (expected  != b)
        {
            throw new ExifException("expected "+Integer.toHexString(b)+" instead of "+Integer.toHexString(expected));
        }
    }
    public static final void consumeShort(ByteBuffer buffer, int expected) throws ExifException
    {
        int b = buffer.getShort() & 0xff;
        if (expected  != b)
        {
            throw new ExifException("expected "+Integer.toHexString(b)+" instead of "+Integer.toHexString(expected));
        }
    }
    public static final String readASCII(ByteBuffer buffer) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int cc;

        cc = buffer.get() & 0xff;
        while (cc != 0)
        {
            if (cc == -1)
            {
                throw new IOException("Unexpected EOF");
            }
            sb.append((char)cc);
            cc = buffer.get() & 0xff;
        }
        return sb.toString();
    }

}
