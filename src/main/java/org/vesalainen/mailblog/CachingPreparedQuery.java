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

package org.vesalainen.mailblog;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @deprecated This unnecessary
 * @author Timo Vesalainen
 */
public class CachingPreparedQuery implements PreparedQuery, Serializable
{
    private static final long serialVersionUID = 1L;
    private transient DatastoreService datastore;
    private Query query;
    private int count = -1;
    private List<Entity> list;
    private Iterable<Entity> iterable;
    private Iterator<Entity> iterator;
    private QueryResultList<Entity> queryResultList;
    private QueryResultIterator<Entity> queryResultIterator;
    private QueryResultIterable<Entity> queryResultIterable;
    
    public CachingPreparedQuery(DatastoreService datastore, Query query)
    {
        this.datastore = datastore;
        this.query = query;
    }

    public void setDatastore(DatastoreService datastore)
    {
        this.datastore = datastore;
    }

    private PreparedQuery getPreparedQuery()
    {
        return datastore.prepare(query);
    }
    public int countEntities()
    {
        if (count == -1)
        {
            count = getPreparedQuery().countEntities();
        }
        return count;
    }

    public int countEntities(FetchOptions fo)
    {
        if (count == -1)
        {
            count = getPreparedQuery().countEntities(fo);
        }
        return count;
    }

    public Entity asSingleEntity() throws TooManyResultsException
    {
        if (list == null)
        {
            list = new ArrayList<Entity>();
            list.add(getPreparedQuery().asSingleEntity());
        }
        switch (list.size())
        {
            case 0:
                return null;
            case 1:
                return list.get(0);
            default:
                throw new TooManyResultsException();
        }
    }

    public QueryResultList<Entity> asQueryResultList(FetchOptions fo)
    {
        if (queryResultList == null)
        {
            queryResultList = getPreparedQuery().asQueryResultList(fo);
        }
        return queryResultList;
    }

    public QueryResultIterator<Entity> asQueryResultIterator()
    {
        if (queryResultIterator == null)
        {
            queryResultIterator = getPreparedQuery().asQueryResultIterator();
        }
        return queryResultIterator;
    }

    public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fo)
    {
        if (queryResultIterator == null)
        {
            queryResultIterator = getPreparedQuery().asQueryResultIterator(fo);
        }
        return queryResultIterator;
    }

    public QueryResultIterable<Entity> asQueryResultIterable()
    {
        if (queryResultIterable == null)
        {
            queryResultIterable = getPreparedQuery().asQueryResultIterable();
        }
        return queryResultIterable;
    }

    public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fo)
    {
        if (queryResultIterable == null)
        {
            queryResultIterable = getPreparedQuery().asQueryResultIterable(fo);
        }
        return queryResultIterable;
    }

    public List<Entity> asList(FetchOptions fo)
    {
        if (list == null)
        {
            list = getPreparedQuery().asList(fo);
        }
        return list;
    }

    public Iterator<Entity> asIterator()
    {
        if (iterator == null)
        {
            iterator = getPreparedQuery().asIterator();
        }
        return iterator;
    }

    public Iterator<Entity> asIterator(FetchOptions fo)
    {
        if (iterator == null)
        {
            iterator = getPreparedQuery().asIterator(fo);
        }
        return iterator;
    }

    public Iterable<Entity> asIterable()
    {
        if (iterable == null)
        {
            iterable = getPreparedQuery().asIterable();
        }
        return iterable;
    }

    public Iterable<Entity> asIterable(FetchOptions fo)
    {
        if (iterable == null)
        {
            iterable = getPreparedQuery().asIterable(fo);
        }
        return iterable;
    }
    
}
