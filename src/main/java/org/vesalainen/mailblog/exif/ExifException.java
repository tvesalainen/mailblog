/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

/**
 *
 * @author tkv
 */
public class ExifException extends Exception
{

    /**
     * Creates a new instance of <code>ExifException</code> without detail message.
     */
    public ExifException()
    {
    }

    /**
     * Constructs an instance of <code>ExifException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ExifException(String msg)
    {
        super(msg);
    }

    ExifException(Throwable ex)
    {
        super(ex);
    }
}
