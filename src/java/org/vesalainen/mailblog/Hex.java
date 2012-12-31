/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tkv
 */
public class Hex
{
    public static String convertToHex(String str)
    {
        try
        {
            return convert(str.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static String convertFromHex(String hex)
    {
        try
        {
            return new String(convert(hex), "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static byte[] convert(String str)
    {
        if ((str.length() % 2) != 0)
        {
            throw new IllegalArgumentException(str+" length is not dividable by 2");
        }
        byte[] bytes = new byte[str.length()/2];
        for (int ii=0;ii<bytes.length;ii++)
        {
            bytes[ii] = (byte)Integer.parseInt(str.substring(2*ii, 2*ii+2), 16);
        }
        return bytes;
    }

    public static String convert(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<bytes.length;ii++)
        {
            sb.append(String.format("%02x", bytes[ii]));
        }
        return sb.toString();
    }
}
