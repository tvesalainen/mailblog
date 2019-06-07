/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.GeoPt;
import org.vesalainen.util.navi.AbstractLocationSupport;
import org.vesalainen.util.navi.LocationSupport;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoPtSupport extends AbstractLocationSupport<GeoPt>
{
    public static final GeoPtSupport LOCATION_SUPPORT = new GeoPtSupport();

    public GeoPtSupport()
    {
        super((l)->l.getLongitude(), (l)->l.getLatitude(), (lat,lon)->new GeoPt((float)lat, (float)lon));
    }
    
}
