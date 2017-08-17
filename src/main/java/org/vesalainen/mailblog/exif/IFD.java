/*
 * Copyright (C) 2004 Timo Vesalainen
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
package org.vesalainen.mailblog.exif;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IFD
{
    private Map<Integer,Interoperability> interoperabilities;
    private int ifdNum;

    public IFD(ByteBuffer app1Body, int ifdNum, ExifAPP1 app1) throws IOException
    {
        if (ifdNum == ExifConstants.MAKERNOTE)
        {
            this.ifdNum = app1.makerNoteNum();
        }
        else
        {
            this.ifdNum = ifdNum;
        }
        app1.addIFD(this.ifdNum, this);
        int pos = app1Body.position();
        int numberOfInteroperability = app1Body.getShort();
        interoperabilities = new HashMap<Integer,Interoperability>();
        for (int ii=0;ii<numberOfInteroperability;ii++)
        {
            Interoperability ioa = new Interoperability(app1Body, app1);
            app1Body.position(app1Body.position()+12);
            interoperabilities.put(ioa.tag(), ioa);
        }
    }

    public Interoperability get(int tag)
    {
        return interoperabilities.get(tag);
    }

    public Collection<Interoperability> getAll()
    {
        return interoperabilities.values();
    }

    /**
     * @return the ifdNum
     */
    public int getIfdNum()
    {
        return ifdNum;
    }
}
