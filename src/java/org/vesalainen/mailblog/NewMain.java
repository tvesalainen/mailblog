/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.mailblog;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Timo Vesalainen
 */
public class NewMain
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            /*
            for (Locale l : Locale.getAvailableLocales())
            {
                System.err.println(l+" "+l.getDisplayName());
            }
            */
            Locale fi = new Locale("fi", "FI");
            System.err.println(fi.getDisplayName());
            Calendar cal = Calendar.getInstance(fi);
            TimeZone timeZone = cal.getTimeZone();
            System.err.println(timeZone);
            //TimeZone tz = TimeZone.
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
