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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Transaction;
import org.junit.Test;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrackSeqFirstLastAdder extends RemoteHelper
{
    
    @Test
    public void convert()
    {
        DS ds = DS.get();
        Iterable<Entity> fetchTrackSeqs = ds.fetchTrackSeqs();
        for (Entity trackSeq : fetchTrackSeqs)
        {
            GeoPt first = null;
            GeoPt last = null;
            if (!trackSeq.hasProperty(FirstProperty))
            {
                for (Entity trackPoint: ds.fetchTrackPoints(trackSeq.getKey()))
                {
                    if (first == null)
                    {
                        first = (GeoPt) trackPoint.getProperty(LocationProperty);
                    }
                    last = (GeoPt) trackPoint.getProperty(LocationProperty);
                }
                trackSeq.setProperty(FirstProperty, first);
                trackSeq.setProperty(LastProperty, last);
                ds.put(trackSeq);
            }
        }
    }
    
}
