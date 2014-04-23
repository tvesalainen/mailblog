/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author tkv
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
