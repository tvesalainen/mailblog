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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import static org.vesalainen.mailblog.BlogConstants.NicknameProperty;

/**
 * @author Timo Vesalainen
 */
public class Bloggers implements BlogConstants
{
    public static final String Blogger = "Blogger";
    public static final String Nickname = "Nickname";
    public static final String DontSendEmail = "DontSendEmail";
    private Key parent;

    public Bloggers(Key parent)
    {
        this.parent = parent;
    }

    public String getInputTable()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<div><table>");
        sb.append("<tr><th>Blogger</th><th>Nickname</th><th>No Email</th></tr>");
        for (Entity entity : getBloggers())
        {
            sb.append("<tr>");
            sb.append("<td>");
            String email = entity.getKey().getName();
            sb.append("<input  type=\"email\" name=\""+Blogger+"\" value=\""+email+"\" size=\"40\" title=\"Clear to delete\"/>");
            sb.append("</td>");
            sb.append("<td>");
            String nickname = (String) entity.getProperty(NicknameProperty);
            if (nickname == null)
            {
                nickname = "";
            }
            sb.append("<input  type=\"text\" name=\""+email+"#"+Nickname+"\" value=\""+nickname+"\" title=\"Nickname\"/>");
            sb.append("</td>");
            sb.append("<td>");
            boolean noEmail = false;
            Boolean dontSendEmail = (Boolean) entity.getProperty(DontSendEmailProperty);
            if (dontSendEmail != null)
            {
                noEmail = dontSendEmail.booleanValue();
            }
            String dsen = email+"#"+DontSendEmail;
            String checked = "";
            if (noEmail)
            {
                checked = "checked ";
            }
            sb.append("<input  type=\"checkbox\" name=\""+dsen+"\" "+checked+"value=\""+dsen+"\" title=\"Dont send email\"/>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("<tr class=\"editable\"><td>");
        sb.append("<input  type=\"email\" name=\""+Blogger+"\" value=\"\" size=\"40\" placeholder=\"New Blogger Email Address\"/>");
        sb.append("</td></tr>");
        sb.append("</table></div>");
        return sb.toString();
    }
    public void update(HttpServletRequest req)
    {
        DatastoreService datastore = DS.get();
        Set<Key> newBloggers = new HashSet<>();
        String[] emails = req.getParameterValues(Blogger);
        if (emails != null)
        {
            for (String email : emails)
            {
                if (email != null && !email.isEmpty())
                {
                    newBloggers.add(KeyFactory.createKey(parent, SettingsKind, email));
                }
            }
        }
        Set<Key> removedBloggers = new HashSet<>();
        for (Entity entity : getBloggers())
        {
            Key key = entity.getKey();
            if (newBloggers.contains(key))
            {
                newBloggers.remove(key);
                String email = entity.getKey().getName();
                String nickname = req.getParameter(email+"#"+Nickname);
                entity.setProperty(NicknameProperty, nickname);
                String dontSendEmail = req.getParameter(email+"#"+DontSendEmail);
                boolean noEmail = dontSendEmail != null;
                entity.setProperty(DontSendEmailProperty, noEmail);
                entity.setProperty(NicknameProperty, nickname);
                datastore.put(entity);
            }
            else
            {
                removedBloggers.add(key);
            }
        }
        for (Key key : newBloggers)
        {
            datastore.put(new Entity(key));
        }
        datastore.delete(removedBloggers);
   }
    private List<Entity> getBloggers()
    {
        DatastoreService datastore = DS.get();
        Query query = new Query(SettingsKind);
        query.setAncestor(parent);
        List<Entity> list = new ArrayList<Entity>();
        PreparedQuery prepared = datastore.prepare(query);
        for (Entity entity : prepared.asIterable())
        {
            if (!parent.equals(entity.getKey()))
            {
                list.add(entity);
            }
        }
        return list;
    }
}
