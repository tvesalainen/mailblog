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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Timo Vesalainen
 */
public abstract class SettingsServlet extends EntityServlet 
{

    public SettingsServlet(String kind)
    {
        super(kind);
    }

    @Override
    protected String getInputTable(Entity entity)
    {
        String i1 = super.getInputTable(entity);
        Bloggers bloggers = new Bloggers(entity.getKey());
        String i2 = bloggers.getInputTable();
        return i1 + i2;
    }

    @Override
    protected void update(HttpServletRequest req) throws HttpException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction tr = datastore.beginTransaction();
        try
        {
            super.update(req);
            com.google.appengine.api.datastore.Key key = getKey(req);
            Bloggers bloggers = new Bloggers(key);
            bloggers.update(req);
            tr.commit();
        }
        catch (Exception ex)
        {
            throw new HttpException(HttpServletResponse.SC_CONFLICT, "rollbacked", ex);
        }
        finally
        {
            if (tr.isActive())
            {
                tr.rollback();
            }
        }
    }

}
