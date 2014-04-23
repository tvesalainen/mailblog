/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

/**
 *
 * @author tkv
 */
public class UnknownAPP1Exception extends ExifException
{

    /**
     * Creates a new instance of <code>UnknownAPP1Exception</code> without detail message.
     */
    public UnknownAPP1Exception()
    {
    }

    /**
     * Constructs an instance of <code>UnknownAPP1Exception</code> with the specified detail message.
     * @param msg the detail message.
     */
    public UnknownAPP1Exception(String msg)
    {
        super(msg);
    }
}
