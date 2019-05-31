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
    Ok, Waypoint, Help, SOS, None, Destination ;
    
    public static String getSpotStyleId(SpotType type)
    {
        return type.name()+"Style";
    }
    public static SpotType getSpotType(String type)
    {
        switch (type)
        {
            case "Destination":
                return Destination;
            case "Check-in/OK":
                return Ok;
            case "Custom":
                return Waypoint;
            case "Help":
                return Help;
            case "SOS":
                return SOS;
            default:
                return None;
        }
    }
}
