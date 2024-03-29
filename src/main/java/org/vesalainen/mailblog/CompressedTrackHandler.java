/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.util.UUID;
import org.vesalainen.nmea.util.TrackInput;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedTrackHandler extends BaseTrackHandler
{

    public CompressedTrackHandler(DS ds)
    {
        super(ds);
    }
    
    public void handle(TrackInput trackInput) throws IOException
    {
        UUID uuid = trackInput.getUuid();
        startTrack(null, uuid.toString());
        startTrackSeq();
        while (trackInput.read())
        {
            trackPoint((float)trackInput.getLatitude(), (float)trackInput.getLongitude(), trackInput.getTime());
        }
        endTrackSeq();
        endTrack();
    }
    
}
