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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Timo Vesalainen
 */
public class BlogEditServlet extends EntityServlet implements BlogConstants
{

    public BlogEditServlet()
    {
        super(BlogKind);
        addProperty(SubjectProperty)
                .setAttribute("size", "80")
                .setIndexed(true)
                .setMandatory();
        addProperty(DateProperty)
                .setType(Date.class)
                .setIndexed(true)
                .setMandatory();
        addProperty(SenderProperty)
                .setType(Email.class)
                .setIndexed(true)
                .setAttribute("size", "40")
                .setAttribute("disabled", true)
                .setMandatory();
        addProperty(PublishProperty)
                .setType(Boolean.class)
                .setIndexed(true);
        addProperty(KeywordsProperty)
                .setType(HashSet.class, String.class)
                .setAttribute("size", "80")
                .setIndexed(true);
        addProperty(HtmlProperty)
                .setType(Text.class)
                .setAttribute("rows", "50")
                .setAttribute("cols", "100")
                .setMandatory();
    }

    @Override
    protected String getTitle(Entity entity)
    {
        return (String) entity.getProperty(SubjectProperty);
    }

    @Override
    protected void modifySelectQuery(Query query)
    {
        query.addSort(DateProperty, Query.SortDirection.DESCENDING);
    }

}
