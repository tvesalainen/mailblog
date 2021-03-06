/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.mailblog.types;

import com.google.appengine.api.datastore.GeoPt;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Locale.Builder;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Timo Vesalainen
 */
public class Util 
{
    public static Locale getLocale(HttpServletRequest request)
    {
        String language = request.getHeader("Accept-Language");
        language = language.substring(0, 2);
        String country = request.getHeader("X-AppEngine-Country");
        String region = request.getHeader("X-AppEngine-Region");
        Builder builder = new Builder();
        try
        {
            builder.setLanguage(language);
            builder.setRegion(country);
            builder.setVariant(region);
            return builder.build();
        }
        catch (IllformedLocaleException ex)
        {
            ex.printStackTrace();
            return new Locale(language, country);
        }
    }
    public static String getCity(HttpServletRequest request)
    {
        return request.getHeader("X-AppEngine-City");
    }
    public static GeoPt getCoordinate(HttpServletRequest request)
    {
        String header = request.getHeader("X-AppEngine-CityLatLong");
        if (header != null)
        {
            String[] ss = header.split(",");
            if (ss.length == 2)
            {
                float lat = Float.parseFloat(ss[0]);
                float lon = Float.parseFloat(ss[1]);
                return new GeoPt(lat, lon);
            }
        }
        return null;
    }
    /**
     * @deprecated Not ready!!!
     * @param request
     * @return 
     */
    public static String getMaidenheadLocator(HttpServletRequest request)
    {
        GeoPt coordinate = getCoordinate(request);
        if (coordinate != null)
        {
            int lond = (int) coordinate.getLongitude();
            int latd = (int) coordinate.getLatitude();
            int lonm = (int) (60*(coordinate.getLongitude()-lond));
            int latm = (int) (60*(coordinate.getLatitude()-latd));
            return new String(
                    new char[] {
                        (char)((lond+180)/20+'A'),
                        (char)((latd+90)/10+'A'),
                        (char)(((lond+180) % 20)+'0'),
                        (char)(((latd+90) % 10)+'0'),
                        (char)((lonm / 5)+'A'),
                        (char)((2*latm / 5)+'A')
                    }
                    );
        }
        return null;
    }
    public static String getRefererParameter(HttpServletRequest request, String parameter)
    {
        String referrer = request.getHeader("referer");
        if (referrer != null)
        {
            int begin = referrer.indexOf(parameter);
            if (begin != -1)
            {
                int end = referrer.indexOf('&', begin);
                if (end != -1)
                {
                    return referrer.substring(begin+parameter.length()+1, end);
                }
                else
                {
                    return referrer.substring(begin+parameter.length()+1);
                }
            }
        }
        return null;
    }
}
