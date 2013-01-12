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

/**
 * @author Timo Vesalainen
 */
public class GeoPtType extends PropertyType<GeoPt> 
{

    @Override
    public GeoPt newInstance(String value)
    {
        if (value != null && !value.isEmpty())
        {
            String[] ss = value.split(",");
            return new GeoPt(Float.parseFloat(ss[0]), Float.parseFloat(ss[1]));
        }
        return null;
    }

    @Override
    public String getString(Object obj)
    {
        if (obj != null)
        {
            GeoPt pt = (GeoPt) obj;
            return pt.getLatitude()+","+pt.getLongitude();
        }
        return "";
    }

}
