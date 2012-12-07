/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 *
 * @author tkv
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
