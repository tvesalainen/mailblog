package org.vesalainen.mailblog.types;

import org.vesalainen.parser.GenClassFactory;
import static org.vesalainen.parser.ParserConstants.*;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.mailblog.types.ContentCounterImpl")
@GrammarDef()
public abstract class ContentCounter
{
    public static ContentCounter getInstance()
    {
        return (ContentCounter) GenClassFactory.loadGenInstance(ContentCounter.class);
    }

    public static int countChars(String text)
    {
        ContentCounter counter = getInstance();
        return counter.count(text);
    }
    @ParseMethod(start="tokens", whiteSpace="whiteSpace")
    protected abstract int count(String text);

    @Rule()
    protected int tokens()
    {
        return 0;
    }
    @Rule("tokens token")
    protected int tokens(int tokens, int token)
    {
        return tokens + token;
    }
    @Rules({
    @Rule("tag"),
    @Rule("word")
    })
    protected int token(int token)
    {
        return token;
    }
    @Terminal(expression = "<[^>]+>")
    protected int tag()
    {
        return 0;
    }
    
    @Terminal(expression = "[^< \t\r\n]+")
    protected int word(@ParserContext(INPUTREADER) InputReader reader)
    {
        return reader.getLength();
    }
    
    @Terminal(expression = "[ \t\r\n]+")
    protected abstract void whiteSpace();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            ContentCounter cp = ContentCounter.getInstance();
            int count = cp.count("<bold attr=\"123\">qwerty</blod> asdfgh");
            System.err.println(count);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
