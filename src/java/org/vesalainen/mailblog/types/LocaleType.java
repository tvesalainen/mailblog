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

package org.vesalainen.mailblog.types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class LocaleType extends PropertyType<String>
{

    @Override
    public String newInstance(String languageTag)
    {
        return languageTag;
    }

    @Override
    public String getHtmlInput(Map attributes, Object value)
    {
        Locale locale = null;
        String languageTag = (String) value;
        if (languageTag != null)
        {
            locale = LocaleHelp.toLocale(languageTag);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<select");
        appendAttributes(sb, attributes);
        sb.append(">");
        Locale[] availableLocales = Locale.getAvailableLocales();
        Arrays.sort(availableLocales, new LocaleComp());
        for (Locale loc : availableLocales)
        {
            sb.append("<option");
            appendAttribute(sb, "value", LocaleHelp.toLanguageTag(loc));
            if (loc.equals(locale))
            {
                appendAttribute(sb, "selected", true);
            }
            sb.append(">");
            sb.append(loc.getDisplayName());
            sb.append("</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

    @Override
    public String getString(Object obj)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class LocaleComp implements Comparator<Locale>
    {

        @Override
        public int compare(Locale o1, Locale o2)
        {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
        
    }
}
