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

import com.google.appengine.api.datastore.Transaction;
import com.google.apphosting.api.ApiProxy;
import java.io.IOException;
import java.util.ConcurrentModificationException;

/**
 * @author Timo Vesalainen
 */
public abstract class Updater<T>
{
    public T start() throws IOException
    {
        while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > 1000)
        {
            try
            {
                return updateWithTransaction();
            }
            catch (ConcurrentModificationException ex)
            {
            }
        }
        throw new IOException("giving up update");
    }

    private T updateWithTransaction() throws IOException
    {
        DS ds = DS.get();
        Transaction tr = ds.beginTransaction();
        try
        {
            T result = update();
            tr.commit();
            return result;
        }
        finally
        {
            if (tr.isActive())
            {
                tr.rollback();
            }
        }
    }
    protected abstract T update() throws IOException;
}
