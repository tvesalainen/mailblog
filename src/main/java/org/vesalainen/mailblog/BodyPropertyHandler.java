/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.Date;
import static java.util.logging.Level.SEVERE;
import static org.vesalainen.mailblog.GeoPtParser.GEO_PT_PARSER;
import org.vesalainen.parsers.date.Dates;
import org.vesalainen.regex.SyntaxErrorException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BodyPropertyHandler extends BodyPropertyFinder
{

    public BodyPropertyHandler()
    {
        super(BodyPropertyHandler.class);
    }

    @Override
    public String replace(String text)
    {
        String replaced = super.replace(text);
        handlePlacemark();
        return replaced;
    }

    private void handlePlacemark()
    {
        if (
                map.containsKey("dateTime") &&
                map.containsKey("location") &&
                map.containsKey("messenger") &&
                map.containsKey("type")
                )
        {
            String dtStr = map.get("dateTime");
            String locStr = map.get("location");
            String messenger = map.get("messenger");
            String type = map.get("type");
            try
            {
                GeoPt location = GEO_PT_PARSER.parseCoordinate(locStr);
                Date time = Dates.parseRMSExpress(dtStr.replace('.', ':'));
                DS ds = DS.get();
                ds.addPlacemark(time, location, messenger, type);
            }
            catch (SyntaxErrorException ex)
            {
                log(SEVERE, ex, "handlePlacemark %s", ex.getMessage());
            }
        }
        else
        {
            if (
                    map.containsKey("dateTime") ||
                    map.containsKey("location") ||
                    map.containsKey("messenger") ||
                    map.containsKey("type")
                    )
            {
                warning("something is missing %s", map);
            }
        }
    }
    
}
