/*
 * Copyright (C) 2013 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.mailblog.types;

import com.google.appengine.api.datastore.GeoPt;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.mailblog.HttpException;
import org.vesalainen.parser.util.LineLocatorException;

/**
 * @author Timo Vesalainen
 */
public class GeoPtType extends PropertyType<GeoPt> 
{
    private static GeoPtParser parser = GeoPtParser.getInstance();
    @Override
    public GeoPt newInstance(String value) throws HttpException
    {
        if (value != null && !value.isEmpty())
        {
            try
            {
                return parser.parseCoordinate(value);
            }
            catch (LineLocatorException lle)
            {
                throw new HttpException(HttpServletResponse.SC_BAD_REQUEST, lle.getMessage());
            }
        }
        return null;
    }

    @Override
    public String getString(Object obj)
    {
        if (obj != null)
        {
            GeoPt pt = (GeoPt) obj;
            float lat = pt.getLatitude();
            char ns = lat > 0 ? 'N' : 'S';
            lat = Math.abs(lat);
            int lati = (int) lat;
            lat = lat-lati;
            float lon = pt.getLongitude();
            char we = lon > 0 ? 'E' : 'W';
            lon = Math.abs(lon);
            int loni = (int) lon;
            lon = lon-loni;
            return String.format(Locale.US,
                    "%c %d\u00b0 %.3f', %c %d\u00b0 %.3f'", 
                    ns,
                    lati,
                    lat*60,
                    we,
                    loni,
                    lon*60
                    );
        }
        return "";
    }

}
