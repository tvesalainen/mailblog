/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog.types;

import javax.servlet.http.HttpServletResponse;
import org.vesalainen.mailblog.HttpException;

/**
 *
 * @author tkv
 */
public class ColorType extends PropertyType<Long> 
{

    @Override
    public Long newInstance(String value) throws HttpException
    {
        if (value == null || value.length() != 7)
        {
            throw new HttpException(HttpServletResponse.SC_CONFLICT, "value");
        }
        if (value.charAt(0) != '#')
        {
            throw new HttpException(HttpServletResponse.SC_CONFLICT, "value");
        }
        return (long)Integer.parseInt(value.substring(1), 16);
    }

    @Override
    public String getString(Object obj)
    {
        Long l = (Long) obj;
        if (l != null)
        {
            return String.format("#%06x", l.intValue());
        }
        else
        {
            return "";
        }
    }
    
    @Override
    public String getDefaultInputType()
    {
        return "color";
    }

}
