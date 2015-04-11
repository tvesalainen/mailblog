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
import java.util.ArrayList;
import java.util.List;

/**
 * Indicates the Opto-Electric Conversion Function (OECF) specified in ISO 14524.
 * OECF is the relationship between the camera optical input and the image values.
 * @author tkv
 * @deprecated Not found in files. Not able to test
 */
public class OECF
{
    private int columns;
    private int rows;
    private List<String> columnItemName;
    private Rational[][] value;

    public OECF(ByteBuffer buffer) throws IOException
    {
        buffer.rewind();
        if (buffer.remaining() >= 4)
        {
            columnItemName = new ArrayList<String>();
            columns = buffer.getShort();
            rows = buffer.getShort();
            for (int ii=0;ii<columns;ii++)
            {
                columnItemName.add(ByteBufferHelper.readASCII(buffer));
            }
            value = new Rational[columns][rows];
            for (int rr=0;rr<rows;rr++)
            {
                for (int cc=0;cc<columns;cc++)
                {
                    int numerator = buffer.getInt() & 0xffffffff;
                    int denominator = buffer.getInt() & 0xffffffff;
                    value[cc][rr] = new Rational(numerator, denominator);
                }
            }
        }
    }

    public String getColumnItemName(int column)
    {
        return columnItemName.get(column);
    }

    public Rational getValue(int column, int row)
    {
        return value[column][row];
    }

    /**
     * @return the columns
     */
    public int getColumns()
    {
        return columns;
    }

    /**
     * @return the rows
     */
    public int getRows()
    {
        return rows;
    }
}
