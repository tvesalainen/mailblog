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
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferOutputStream extends OutputStream
{
    private ByteBuffer byteBuffer;
    public ByteBufferOutputStream(ByteBuffer byteBuffer)
    {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public void write(int b) throws IOException
    {
        try
        {
            byteBuffer.put((byte)b);
        }
        catch (BufferOverflowException ex)
        {
            throw new IOException(ex);
        }
    }
}
