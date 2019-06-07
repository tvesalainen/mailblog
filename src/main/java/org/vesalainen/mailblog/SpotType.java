/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import static org.vesalainen.mailblog.BlogConstants.*;

/**
 * @author Timo Vesalainen
 */
public enum SpotType
{
    Unknown(""), 
    Anchored(AnchoredIconProperty), 
    Waypoint(WaypointIconProperty), 
    Destination(DestinationIconProperty) ;
    private String iconProperty;
    
    SpotType(String property)
    {
        this.iconProperty = property;
    }

    public String getIconProperty()
    {
        return iconProperty;
    }
    
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
                return Unknown;
        }
    }
}
