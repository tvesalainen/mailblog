/*
 * Rational.java
 *
 * Created on 29. lokakuuta 2004, 13:02
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
