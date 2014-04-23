/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

/**
 * @author Timo Vesalainen
 */
public enum SpotType
{
    Ok, Custom, Help, SOS, None ;
    
    public static String getSpotStyleId(SpotType type)
    {
        return type.name()+"Style";
    }
    public static SpotType getSpotType(String type)
    {
        switch (type)
        {
            case "Check-in/OK":
                return Ok;
            case "Custom":
                return Custom;
            case "Help":
                return Help;
            case "SOS":
                return SOS;
            default:
                return None;
        }
    }
}
