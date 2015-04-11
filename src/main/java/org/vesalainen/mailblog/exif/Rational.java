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

/**
 *
 * @author  tkv
 */
public class Rational
{
    private long _numerator = 0;
    private long _denominator = 0;
    /** Creates a new instance of Rational */
    public Rational(long numerator, long denominator)
    {
        _numerator = numerator;
        _denominator = denominator;
    }
    
    public long numerator()
    {
        return _numerator;
    }

    public long denominator()
    {
        return _denominator;
    }
    
    public double doubleValue()
    {
        return (double)_numerator / (double)_denominator;
    }
    
    public String toString()
    {
        if (_denominator == 1)
        {
            return String.valueOf(_numerator);
        }
        else
        {
            return _numerator+"/"+_denominator;
        }
    }
}
