/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author tkv
 * @deprecated This is unfinished. horizontal and vertical values not in same
 * byteorder as others???
 */
public class CFA
{
    private int horizontal;
    private int vertical;
    private byte[][] value;

    public CFA(ByteBuffer buffer) throws IOException
    {
        buffer.rewind();
        if (buffer.remaining() >= 4)
        {
            horizontal = buffer.getShort() & 0xffff;
            vertical = buffer.getShort() & 0xffff;
        }
    }
}
