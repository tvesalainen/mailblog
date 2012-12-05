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

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

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
            Locale locale = new Locale("fi");
            DateFormatSymbols dfs = new DateFormatSymbols(locale);
            System.err.println(Arrays.toString(dfs.getMonths()));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
