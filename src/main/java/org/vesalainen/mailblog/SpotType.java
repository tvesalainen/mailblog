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
    Anchored, Waypoint, None, Destination ;
    
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
            case "Anchored":
            case "Check-in/OK":
                return Anchored;
            case "Waypoint":
            case "Custom":
                return Waypoint;
            default:
                return None;
        }
    }
}
