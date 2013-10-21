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

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Index.IndexState;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class CachingDatastoreService implements DatastoreService, BlogConstants
{
    protected DatastoreService datastore;
    protected MemcacheService cache;
    private long version;
    private Key entityGroupKey;

    protected CachingDatastoreService()
    {
        this.datastore = DatastoreServiceFactory.getDatastoreService();
        this.cache = MemcacheServiceFactory.getMemcacheService();
        this.entityGroupKey = Entities.createEntityGroupKey(getRootKey());
    }

    public static Key getRootKey()
    {
        return KeyFactory.createKey(RootKind, 1);
    }
    
    private void checkEntities(Iterable<Entity> itrbl)
    {
        for (Entity entity : itrbl)
        {
            check(entity);
        }
    }
    private void checkKeys(Iterable<Key> itrbl)
    {
        for (Key key : itrbl)
        {
            check(key);
        }
    }
    private void check(Entity entity)
    {
        check(entity.getKey());
    }
    private void check(Key... keys)
    {
        for (Key k : keys)
        {
            check(k);
        }
    }
    private void check(Key key)
    {
        if (key.getParent() == null)
        {
            if (!RootKind.equals(key.getKind()))
            {
                throw new IllegalArgumentException(key+" not root parented");
            }
        }
        else
        {
            check(key.getParent());
        }
    }
    private void check()
    {
        try
        {
            long v = Entities.getVersionProperty(datastore.get(entityGroupKey));
            if (v > version)
            {
                // remove all cache
                cache.clearAll();
                version = v;
            }
        }
        catch (EntityNotFoundException ex)
        {
            ex.printStackTrace();   // because this should not happen!!!!
            cache.clearAll();
            version = 0;
        }
    }
    public String getETag()
    {
        check();
        return String.valueOf(version);
    }
    public boolean changedETAG(String etag)
    {
        long et = Long.parseLong(etag);
        check();
        return version != et;
    }
    @Override
    public PreparedQuery prepare(Transaction t, Query query)
    {
        return datastore.prepare(t, query);
    }

    @Override
    public PreparedQuery prepare(Query query)
    {
        return datastore.prepare(query);
    }

    @Override
    public Transaction getCurrentTransaction(Transaction t)
    {
        return datastore.getCurrentTransaction(t);
    }

    @Override
    public Transaction getCurrentTransaction()
    {
        return datastore.getCurrentTransaction();
    }

    @Override
    public Collection<Transaction> getActiveTransactions()
    {
        return datastore.getActiveTransactions();
    }

    @Override
    public List<Key> put(Transaction t, Iterable<Entity> itrbl)
    {
        checkEntities(itrbl);
        return datastore.put(t, itrbl);
    }

    @Override
    public List<Key> put(Iterable<Entity> itrbl)
    {
        checkEntities(itrbl);
        return datastore.put(itrbl);
    }

    @Override
    public Key put(Transaction t, Entity entity)
    {
        check(entity);
        return datastore.put(t, entity);
    }

    @Override
    public Key put(Entity entity)
    {
        check(entity);
        return datastore.put(entity);
    }

    @Override
    public Map<Index, IndexState> getIndexes()
    {
        check();
        return datastore.getIndexes();
    }

    @Override
    public DatastoreAttributes getDatastoreAttributes()
    {
        return datastore.getDatastoreAttributes();
    }

    @Override
    public Map<Key, Entity> get(Transaction t, Iterable<Key> itrbl)
    {
        checkKeys(itrbl);
        return datastore.get(t, itrbl);
    }

    @Override
    public Map<Key, Entity> get(Iterable<Key> itrbl)
    {
        checkKeys(itrbl);
        return datastore.get(itrbl);
    }

    @Override
    public Entity get(Transaction t, Key key) throws EntityNotFoundException
    {
        return datastore.get(t, key);
    }

    @Override
    public Entity get(Key key) throws EntityNotFoundException
    {
        check();
        check(key);
        Entity e = (Entity) cache.get(key);
        if (e == null)
        {
            e = datastore.get(key);
            cache.put(key, e);
        }
        return e;
    }

    @Override
    public void delete(Transaction t, Iterable<Key> itrbl)
    {
        checkKeys(itrbl);
        datastore.delete(t, itrbl);
    }

    @Override
    public void delete(Iterable<Key> itrbl)
    {
        checkKeys(itrbl);
        datastore.delete(itrbl);
    }

    @Override
    public void delete(Transaction t, Key... keys)
    {
        check(keys);
        datastore.delete(t, keys);
    }

    @Override
    public void delete(Key... keys)
    {
        check(keys);
        datastore.delete(keys);
    }

    @Override
    public Transaction beginTransaction(TransactionOptions to)
    {
        return datastore.beginTransaction(to);
    }

    @Override
    public Transaction beginTransaction()
    {
        return datastore.beginTransaction();
    }

    @Override
    public KeyRange allocateIds(Key key, String string, long l)
    {
        return datastore.allocateIds(key, string, l);
    }

    @Override
    public KeyRange allocateIds(String string, long l)
    {
        return datastore.allocateIds(string, l);
    }

    @Override
    public KeyRangeState allocateIdRange(KeyRange kr)
    {
        return datastore.allocateIdRange(kr);
    }
    
}
