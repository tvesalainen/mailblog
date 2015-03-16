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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.vesalainen.mailblog.BlogConstants.*;

/**
 *
 * @author Timo Vesalainen
 */
public class PageServlet extends EntityServlet
{
    
    public PageServlet()
    {
        super(PageKind);
        addProperty(PageProperty)
                .setType(Text.class)
                .setMandatory()
                .setAttribute("rows", "50")
                .setAttribute("cols", "100");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String backup = req.getParameter(BackupParameter);
        if (backup != null)
        {
            DatastoreService datastore = DS.get();
            Key key = KeyFactory.stringToKey(backup);
            try
            {
                Entity entity = datastore.get(key);
                Text text = (Text) entity.getProperty(PageProperty);
                if (text != null)
                {
                    resp.setContentType("text/plain ;charset=utf-8");
                    resp.getWriter().write(text.getValue());
                }
                else
                {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, entity.toString());
                }
            }
            catch (EntityNotFoundException ex)
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, key+" not found");
            }
        }
        else
        {
            super.doGet(req, resp);
        }
    }

    @Override
    protected String getInputTable(Entity entity)
    {
        String i1 = super.getInputTable(entity);
        String i2 = getSelectBackup(entity.getKey());
        return i1 + i2;
    }

    private String getSelectBackup(Key parent)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<select class=\"backupSelect\">");
        sb.append("<option value=\"" + KeyFactory.keyToString(parent) + "\">Current</option>");
        for (Entity entity : getBackups(parent))
        {
            Date date = new Date(entity.getKey().getId());
            sb.append("<option value=\"" + KeyFactory.keyToString(entity.getKey()) + "\">" + date + "</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }
    private List<Entity> getBackups(Key parent)
    {
        log(parent.toString());
        log(parent.getNamespace());
        DatastoreService datastore = DS.get();
        Query query = new Query(PageBackupKind);
        log(query.toString());
        query.setAncestor(parent);
        query.setKeysOnly();
        List<Entity> list = new ArrayList<Entity>();
        PreparedQuery prepared = datastore.prepare(query);
        for (Entity entity : prepared.asIterable())
        {
            if (!parent.equals(entity.getKey()))
            {
                list.add(entity);
            }
        }
        Collections.reverse(list);
        return list;
    }

}
