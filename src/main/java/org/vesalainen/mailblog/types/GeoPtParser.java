/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog.types;

import com.google.appengine.api.datastore.GeoPt;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.Regex;
 

/**
 * @author Timo Vesalainen
 * @see <a href="doc-files/GeoPtParser-coordinate.html#BNF">BNF Syntax for Geological Coordinate</a>
 */
@GenClassname("org.vesalainen.mailblog.types.GeoPtParserImpl")
@GrammarDef()
public abstract class GeoPtParser 
{
    public static GeoPtParser getInstance()
    {
        return (GeoPtParser) GenClassFactory.loadGenInstance(GeoPtParser.class);
    }
    /**
     * 
     * @param text
     * @return 
     * @see <a href="doc-files/GeoPtParser-coordinate.html#BNF">BNF Syntax for Geological Coordinate</a>
     */
    @ParseMethod(start="coordinate", whiteSpace ="whiteSpace")
    public abstract GeoPt parseCoordinate(String text);
    
    @Rule("(decimal|integer) '\\,' (decimal|integer)")
    protected GeoPt coordinate(Number lat, Number lon)
    {
        return new GeoPt(lat.floatValue(), lon.floatValue());
    }

    @Rule("ns latitude '\\,' we longitude")
    protected GeoPt coordinate(int ns, Number lat, int we, Number lon)
    {
        return new GeoPt(ns*lat.floatValue(), we*lon.floatValue());
    }
    
    @Rule("integer degreeChar? decimal secondChar?")
    protected Number latitude(Number degree, Number minutes,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double d = deg + min/60.0;
        if (d < 0 || d > 90 || min < 0 || min > 60)
        {
            reader.throwSyntaxErrorException("latitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? integer secondChar? integer minuteChar?")
    protected Number latitude(Number degree, Number minutes, Number seconds,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double sec = seconds.doubleValue();
        double d = deg + min/60.0 + sec/3600.0;
        if (d < 0 || d > 90 || min < 0 || min > 60 || sec < 0 || sec > 60)
        {
            reader.throwSyntaxErrorException("latitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? decimal secondChar?")
    protected Number longitude(Number degree, Number minutes,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double d = deg + min/60.0;
        if (d < 0 || d > 180 || min < 0 || min > 60)
        {
            reader.throwSyntaxErrorException("longitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }
    
    @Rule("integer degreeChar? integer secondChar? integer minuteChar?")
    protected Number longitude(Number degree, Number minutes, Number seconds,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        double deg = degree.doubleValue();
        double min = minutes.doubleValue();
        double sec = seconds.doubleValue();
        double d = deg + min/60.0 + sec/3600.0;
        if (d < 0 || d > 180 || min < 0 || min > 60 || sec < 0 || sec > 60)
        {
            reader.throwSyntaxErrorException("longitude coordinate", String.valueOf(d));
        }
        return new Double(d);
    }

    @Terminal(expression="\u00b0")
    protected abstract void degreeChar();
    
    @Terminal(expression="\"")
    protected abstract void minuteChar();
    
    @Terminal(expression="'")
    protected abstract void secondChar();
    
    @Rules({
        @Rule("north"),
        @Rule("south")
    })
    protected abstract int ns(int sign);
    
    @Rules({
        @Rule("west"),
        @Rule("east")
    })
    protected abstract int we(int sign);
    
    @Rule("n")
    protected int north()
    {
        return 1;
    }
    
    @Rule("e")
    protected int east()
    {
        return 1;
    }
    
    @Rule("s")
    protected int south()
    {
        return -1;
    }
    
    @Rule("w")
    protected int west()
    {
        return -1;
    }
    
    @Terminal(expression = "[\\+\\-]?[0-9]+")
    protected Number integer(String value)
    {
        return Long.parseLong(value);
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected Number decimal(String value)
    {
        return Double.parseDouble(value);
    }

    @Terminal(expression = "[ \t\r\n]+")
    protected abstract void whiteSpace();

    @ReservedWords(value =
    {
        "n",
        "s",
        "w",
        "e"
    },
    options =
    {
        Regex.Option.CASE_INSENSITIVE
    })
    protected void reservedWordsD()
    {
        
    }
}
