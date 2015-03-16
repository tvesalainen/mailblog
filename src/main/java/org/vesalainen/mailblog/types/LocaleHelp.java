/*
 * Copyright (C) 2012 Timo Vesalainen
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

package org.vesalainen.mailblog.types;

import java.util.Locale;

/**
 * Helper to Java 6 -> 7 migration
 * // TODO change in java 7
 * @author Timo Vesalainen
 */
public class LocaleHelp 
{
    public static String toLanguageTag(Locale locale)
    {
        StringBuilder sb = new StringBuilder();
        String language = locale.getLanguage();
        if (language != null)
        {
            sb.append(language);
        }
        String country = locale.getCountry();
        if (country != null)
        {
            sb.append('-');
            sb.append(country);
            String variant = locale.getVariant();
            if (variant != null)
            {
                sb.append('-');
                sb.append(variant);
            }
        }
        return sb.toString();
    }
    public static Locale toLocale(String languageTag)
    {
        String[] ss = languageTag.split("-");
        switch (ss.length)
        {
            case 1:
                return new Locale(ss[0]);
            case 2:
                return new Locale(ss[0], ss[1]);
            case 3:
                return new Locale(ss[0], ss[1], ss[2]);
            default:
                throw new IllegalArgumentException("bad languageTag "+languageTag);
        }
    }
}
