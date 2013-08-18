/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.mailblog.types;

import java.util.Calendar;
import java.util.Date;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.mailblog.types.HTML5DatetimeImpl")
@GrammarDef()
public abstract class HTML5Datetime
{
    public static HTML5Datetime getInstance()
    {
        return (HTML5Datetime) GenClassFactory.loadGenInstance(HTML5Datetime.class);
    }
    /**
     *
     * @param str
     * @return
     * @see <a href="http://www.w3.org/html/wg/drafts/html/master/infrastructure.html#concept-datetime">HTML5 Datetime</a>
     */
    public Date parse(String str)
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        parse(str, cal);
        return cal.getTime();
    }
    @ParseMethod(start="datetime")
    protected abstract void parse(String str, @ParserContext("cal") Calendar cal);
    
    @Rule("year '\\-' month '\\-' day ( '[T ]' time tz? )?")
    protected abstract void datetime();
    
    @Rule("hour ':' minute ( ':' second millis? )?")
    protected abstract void time();
    
    @Rule("integer")
    protected void year(int year, @ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.YEAR, year);
    }
    @Rule("integer")
    protected void month(int month, @ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.MONTH, month-1);
    }
    @Rule("integer")
    protected void day(int day, @ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.DAY_OF_MONTH, day);
    }
    @Rule("integer")
    protected void hour(int hour, @ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.HOUR_OF_DAY, hour);
    }
    @Rule("integer")
    protected void minute(int minute, @ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.MINUTE, minute);
    }
    @Rule("integer")
    protected void second(int second, @ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.SECOND, second);
    }
    @Rule("'\\.' string")
    protected void millis(String millis, @ParserContext("cal") Calendar cal)
    {
        int mls = Integer.parseInt(millis);
        switch (millis.length())
        {
            case 1:
                mls = mls*100;
                break;
            case 2:
                mls = mls*10;
                break;
            case 3:
                break;
            default:
                throw new IllegalArgumentException(millis+" not valid millisecond");
        }
        cal.set(Calendar.MILLISECOND, mls);
    }
    @Rules({
    @Rule("z"),
    @Rule("offsetTz")
    })
    protected abstract void tz();
    
    @Rule("'Z'")
    protected void z(@ParserContext("cal") Calendar cal)
    {
        cal.set(Calendar.ZONE_OFFSET, 0);
    }
    @Rule("sign integer ':' integer")
    protected void offsetTz(int sign, int hour, int minute, @ParserContext("cal") Calendar cal)
    {
        int offset = hour*60*60*1000;
        offset += minute*60*1000;
        cal.set(Calendar.ZONE_OFFSET, sign*offset);
    }
    @Rules({
    @Rule("plus"),
    @Rule("minus")
    })
    protected abstract int sign(int sign);
    
    @Rule("'\\+'")
    protected int plus()
    {
        return 1;
    }
    @Rule("'\\-'")
    protected int minus()
    {
        return -1;
    }
    @Terminal(expression="[0-9]+")
    protected int integer(int i)
    {
        return i;
    }
    @Terminal(expression="[0-9]+")
    protected String string(String s)
    {
        return s;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            HTML5Datetime dt = HTML5Datetime.getInstance();
            Date date = dt.parse("0037-12-13 00:00Z");
            System.err.println(date);
            date = dt.parse("1979-10-14T12:00:00.001-04:00");
            System.err.println(date);
            date = dt.parse("8592-01-01T02:09+02:09");
            System.err.println(date);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
