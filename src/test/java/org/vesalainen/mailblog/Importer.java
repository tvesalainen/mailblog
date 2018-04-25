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
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Importer extends RemoteHelper
{
    @Test
    public void load() throws IOException
    {
        DS ds = DS.get();
        List<Entity> ents = new ArrayList<>();
        for (Entity e : ds.fetchTracks())
        {
            ents.add(e);
        }
        for (Entity e : ds.fetchTrackSeqs())
        {
            ents.add(e);
            for (Entity e2 : ds.fetchTrackPoints(e.getKey()))
            {
                ents.add(e2);
            }
        }
        for (Entity e : ds.fetchPlacemarks())
        {
            ents.add(e);
        }
        try (ObjectOutputStream dos = new ObjectOutputStream(new FileOutputStream("tracks.ser")))
        {
            dos.writeObject(ents);
        }
    }
}
