/*
 * Copyright (C) 2013 Timo Vesalainen
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

import com.google.appengine.api.NamespaceManager;

/**
 * @author Timo Vesalainen
 */
public abstract class RunInNamespace<T>
{
    public T doIt(String namespace)
    {
        return doIt(namespace, true);
    }
    public T doIt(String namespace, boolean change)
    {
        if (change)
        {
            String safeNamespace = NamespaceManager.get();
            try
            {
                NamespaceManager.set(namespace);
                return run();
            }
            finally
            {
                NamespaceManager.set(safeNamespace);
            }
        }
        else
        {
            return run();
        }
    }
    protected abstract T run(); 
}
