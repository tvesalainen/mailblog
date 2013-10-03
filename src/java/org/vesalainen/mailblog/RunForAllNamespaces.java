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
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.base.Objects;

/**
 * @author Timo Vesalainen
 */
public abstract class RunForAllNamespaces
{
    public void start(DS ds)
    {
        String safeNamespace = NamespaceManager.get();
        try
        {
            Query query = new Query(Entities.NAMESPACE_METADATA_KIND);
            PreparedQuery prepared = ds.prepare(query);
            for (Entity entity : prepared.asIterable())
            {
                String namespace = Entities.getNamespaceFromNamespaceKey(entity.getKey());
                NamespaceManager.set(namespace);
                run(namespace);
            }
        }
        finally
        {
            NamespaceManager.set(safeNamespace);
        }
    }
    protected abstract void run(String namespace);
}
