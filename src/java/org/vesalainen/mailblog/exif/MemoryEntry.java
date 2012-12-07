/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.exif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class MemoryEntry implements Comparable<MemoryEntry>
{
    private static Map<Integer,MemoryEntry[]> map = new HashMap<Integer,MemoryEntry[]>();
    private static int passCount;

    private Integer address;
    private String description;

    public MemoryEntry(int address, String description)
    {
        this.address = address;
        this.description = description;
    }

    public static void add(int address, String description)
    {
        MemoryEntry[] me = map.get(address);
        if (me == null)
        {
            me = new MemoryEntry[2];
            map.put(address, me);
        }
        boolean dup = false;
        if (me[passCount] != null)
        {
            dup = true;
            System.err.print(String.format("DUPLICATE(%d) %04X %-40s ", passCount, me[passCount].address, me[passCount].description));
        }
        me[passCount] = new MemoryEntry(address, description);
        if (dup)
        {
            System.err.println(String.format(" <== %04X %-40s ", me[passCount].address, me[passCount].description));
        }
    }

    public static void clear()
    {
        map.clear();
        passCount = 0;
    }
    public static void nextPass()
    {
        passCount++;
    }
    public static void dump()
    {
        List<Integer> keys = new ArrayList<Integer>();
        keys.addAll(map.keySet());
        Collections.sort(keys);
        for (Integer address : keys)
        {
            MemoryEntry[] me = map.get(address);
            for (MemoryEntry entry : me)
            {
                if (entry != null)
                {
                    System.err.print(String.format("%04X %-40s ", entry.address, entry.description));
                }
                else
                {
                    System.err.print("                                              ");
                }
            }
            System.err.println();
        }
    }

    public int compareTo(MemoryEntry o)
    {
        return address.compareTo(o.address);
    }

}
