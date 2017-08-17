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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferInputStream extends InputStream
{
    private ByteBuffer byteBuffer;
    private Closeable closeable;
    public ByteBufferInputStream(ByteBuffer byteBuffer)
    {
        this.byteBuffer = byteBuffer;
    }

    public ByteBufferInputStream(File file) throws FileNotFoundException, IOException
    {
        this(file, 0, file.length());
    }

    public ByteBufferInputStream(File file, long position, long size) throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(file);
        closeable = fis;
        this.byteBuffer = fis.getChannel().map(MapMode.READ_ONLY, position, size);
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            return byteBuffer.get() & 0xff;
        }
        catch (BufferUnderflowException ex)
        {
            return -1;
        }
    }

    @Override
    public void close() throws IOException
    {
        if (closeable != null)
        {
            closeable.close();
            System.gc();
        }
    }

    public ByteBuffer getByteBuffer()
    {
        return byteBuffer;
    }

}
