/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.vesalainen.gpx.TrackHandler;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.types.TimeSpan;
import org.w3c.dom.Element;

/**
 * @author Timo Vesalainen
 */
public class OpenCPNTrackHandler extends BaseTrackHandler implements TrackHandler
{

    public OpenCPNTrackHandler(DS ds)
    {
        super(ds);
    }
    
    @Override
    public boolean startTrack(String name, Collection<Object> extensions)
    {
        return startTrack(name, getGuid(extensions));
    }

    @Override
    public void trackPoint(double latitude, double longitude, long time)
    {
        trackPoint((float)latitude, (float)longitude, time);
    }
    
    public static String getGuid(Collection<Object> extensions)
    {
        for (Object ob : extensions)
        {
            if (ob instanceof Element)
            {
                Element el = (Element) ob;
                if (
                        "http://www.opencpn.org".equals(el.getNamespaceURI()) &&
                        "guid".equals(el.getLocalName())
                        )
                {
                    return el.getTextContent();
                }
            }
        }
        return null;
    }

}
