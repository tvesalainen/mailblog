/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
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
        MemoryEntry.add(pos, "numberOfInteroperability="+numberOfInteroperability);
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
