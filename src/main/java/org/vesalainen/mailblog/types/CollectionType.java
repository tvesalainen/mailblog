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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.mailblog.HttpException;

/**
 * @author Timo Vesalainen
 */
public class CollectionType extends PropertyType<Collection>
{
    private Class<? extends Collection> collectionType;
    private PropertyType<?> componentType;

    public CollectionType(Class<? extends Collection> collectionType, Class<?> componentType)
    {
        this.collectionType = collectionType;
        this.componentType = PropertyType.getInstance(componentType);
    }
    
    @Override
    public Collection newInstance(String value) throws HttpException
    {
        if (value != null)
        {
            try
            {
                Collection col = collectionType.newInstance();
                String[] ss = value.split(" ");
                for (String s : ss)
                {
                    col.add(componentType.newInstance(s));
                }
                return col;
            }
            catch (InstantiationException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getString(Object obj)
    {
        Collection col = (Collection) obj;
        if (col != null)
        {
            StringBuilder sb = new StringBuilder();
            for (Object ob : col)
            {
                if (sb.length() > 0)
                {
                    sb.append(' ');
                }
                sb.append(componentType.getString(ob));
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }

}
