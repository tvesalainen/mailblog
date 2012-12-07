/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
