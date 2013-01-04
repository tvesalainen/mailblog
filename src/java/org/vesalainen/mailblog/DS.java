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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.rss.Channel;
import org.vesalainen.rss.Item;
import org.vesalainen.rss.RSS;

/**
 * @author Timo Vesalainen
 */
public class DS extends CachingDatastoreService implements BlogConstants
{
    private DS()
    {
    }

    public static DS get()
    {
        return new DS();
    }
    
    public Entity getPageEntity(String path) throws EntityNotFoundException
    {
        return get(KeyFactory.createKey(Root, PageKind, path));
    }
    public void setPage(Entity page)
    {
        Transaction tr = beginTransaction();
        try
        {
            Entity backup = new Entity(PageBackupKind, System.currentTimeMillis(), page.getKey());
            backup.setPropertiesFrom(page);
            put(backup);
            put(page);
            tr.commit();
        }
        finally
        {
            if (tr.isActive())
            {
                tr.rollback();
            }
        }
    }
    public Key getBlogKey(String messageId)
    {
        Key root = KeyFactory.createKey(RootKind, 1);
        return KeyFactory.createKey(root, BlogKind, messageId);
    }
    public Entity getBlogFromMessageId(String messageId)
    {
        Key key =  getBlogKey(messageId);
        try
        {
            return get(key);
        }
        catch (EntityNotFoundException ex)
        {
            return new Entity(key);
        }
    }
    public Key getMetadataKey(String sha1)
    {
        Key root = KeyFactory.createKey(RootKind, 1);
        return KeyFactory.createKey(root, MetadataKind, sha1);
    }
    public Entity getMetadata(String sha1)
    {
        Key metadataKey = getMetadataKey(sha1);
        try
        {
            return get(metadataKey);
        }
        catch (EntityNotFoundException ex)
        {
            return new Entity(metadataKey);
        }
    }
    
    public void writeRSS(URL base, HttpServletResponse response) throws IOException
    {
        try
        {
            URI baseUri = base.toURI();
            Settings settings = getSettings();
            RSS rss = new RSS();
            Channel channel = new Channel();
            rss.getChannel().add(channel);
            channel.setTitle(settings.getTitle());
            channel.setDescription(settings.getDescription());
            channel.setLanguage(settings.getLocale().getLanguage());
            Date now = new Date();
            channel.setPubDate(now);
            channel.setLastBuildDate(now);
            Query query = new Query(BlogKind);
            query.addSort(DateProperty, Query.SortDirection.DESCENDING);
            PreparedQuery prepared = prepare(query);
            for (Entity blog : prepared.asIterable())
            {
                String subject = (String) Objects.nonNull(blog.getProperty(SubjectProperty));
                Date date = (Date) Objects.nonNull(blog.getProperty(DateProperty));
                Email sender = (Email) Objects.nonNull(blog.getProperty(SenderProperty));
                String nickname = getNickname(sender);
                try
                {
                    Settings bloggerSettings = getSettingsFor(sender);
                    nickname = bloggerSettings.getNickname();
                }
                catch (EntityNotFoundException ex)
                {
                }
                Item item = new Item();
                item.setAuthor(nickname);
                item.setTitle(subject);
                item.setPubDate(date);
                URI uri = baseUri.resolve("/blog?blog="+KeyFactory.keyToString(blog.getKey()));
                item.setLink(uri);
                channel.getItem().add(item);
            }
            response.setContentType("application/rss+xml");
            rss.marshall(response.getWriter());
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public String getBlogList(String blogCursor, URL base) throws EntityNotFoundException, IOException
    {
        Date begin = null;
        Date end = null;
        Cursor cursor = null;
        if (blogCursor != null)
        {
            System.err.println(blogCursor);
            BlogCursor bc = new BlogCursor(blogCursor);
            if (bc.isSearch())
            {
                return Searches.getBlogListFromSearch(bc, base);
            }
            begin = bc.getBegin();
            end = bc.getEnd();
            cursor = bc.getDatastoreCursor();
        }
        Settings settings = getSettings();
        FetchOptions options = FetchOptions.Builder.withLimit(settings.getShowCount());
        if (cursor != null)
        {
            options = options.startCursor(cursor);
        }
        StringBuilder sb = new StringBuilder();
        Query query = new Query(BlogKind);
        query.addSort(DateProperty, Query.SortDirection.DESCENDING);
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
        if (begin != null)
        {
            filters.add(new FilterPredicate(DateProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, begin));
        }
        if (end != null)
        {
            filters.add(new FilterPredicate(DateProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, end));
        }
        if (filters.size() == 1)
        {
            query.setFilter(filters.get(0));
        }
        else
        {
            if (filters.size() > 1)
            {
                CompositeFilter filter = new CompositeFilter(CompositeFilterOperator.AND, filters);
                query.setFilter(filter);
            }
        }
        System.err.println(query);
        PreparedQuery prepared = prepare(query);
        QueryResultList<Entity> list = prepared.asQueryResultList(options);
        cursor = list.getCursor();
        BlogCursor bc = new BlogCursor()
                .setDatastoreCursor(cursor)
                .setBegin(begin)
                .setEnd(end);
        for (Entity entity : list)
        {
            sb.append(getBlog(entity, base));
        }
        if (list.size() == settings.getShowCount())
        {
            sb.append("<span id=\"nextPage\" class=\"hidden\">"+bc.getWebSafe()+"</span>");
        }
        return sb.toString();
    }

    public String getComments(Key blogKey, User loggedUser) throws EntityNotFoundException
    {
        Entity blog = get(blogKey);
        Email sender = (Email) Objects.nonNull(blog.getProperty(SenderProperty));
        Settings senderSettings = Objects.nonNull(getSettingsFor(sender));
        String commentTemplate = senderSettings.getCommentTemplate();
        Locale locale = senderSettings.getLocale();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        StringBuilder sb = new StringBuilder();
        Query query = new Query(CommentsKind);
        query.setAncestor(blogKey);
        query.addSort(TimestampProperty, Query.SortDirection.DESCENDING);
        PreparedQuery prepared = prepare(query);
        for (Entity comment : prepared.asIterable())
        {
            User user = (User) comment.getProperty(UserProperty);
            Date date = (Date) comment.getProperty(TimestampProperty);
            Text text = (Text) comment.getProperty(CommentProperty);
            String hidden = "hidden";
            if (user.equals(loggedUser))
            {
                hidden = "";
            }
            if (user != null && text != null && date != null)
            {
                String nickname = getNickname(user);
                String dateString = dateFormat.format(date);
                sb.append(String.format(locale, commentTemplate, nickname, dateString, text.getValue(), hidden, KeyFactory.keyToString(comment.getKey())));
            }
        }
        return sb.toString();
    }
    public void addComment(Key blogKey, User user, String text)
    {
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        Entity comment = new Entity(CommentsKind, blogKey);
        comment.setProperty(UserProperty, user);
        comment.setProperty(CommentProperty, new Text(text));
        comment.setProperty(TimestampProperty, new Date());
        put(comment);
    }
    public void removeComment(Key commentKey, User currentUser) throws HttpException
    {
        try
        {
            Entity comment = get(commentKey);
            User user = (User) comment.getProperty(UserProperty);
            if (currentUser.equals(user))
            {
                delete(commentKey);
            }
            else
            {
                throw new HttpException(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        catch (EntityNotFoundException ex)
        {
            throw new HttpException(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getNickname(Email email)
    {
        return getNickname(email.getEmail());
    }
    private String getNickname(User user)
    {
        return getNickname(user.getEmail());
    }
    private String getNickname(String email)
    {
        int idx = email.indexOf('@');
        if (idx == -1)
        {
            throw new IllegalArgumentException(email+" not email");
        }
        String name = email.substring(0, idx);
        idx = name.lastIndexOf('.');
        if (idx != -1)
        {
            return name.substring(0, idx);
        }
        else
        {
            return name;
        }
    }
    public String getBlog(Key key, URL base) throws EntityNotFoundException
    {
        Entity entity = get(key);
        return getBlog(entity, base);
        
    }
    public String getBlog(Entity entity, URL base) throws EntityNotFoundException
    {
        String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
        Date date = (Date) Objects.nonNull(entity.getProperty(DateProperty));
        Email sender = (Email) Objects.nonNull(entity.getProperty(SenderProperty));
        Text body = (Text) Objects.nonNull(entity.getProperty(HtmlProperty));
        return getBlog(sender, subject, date, body.getValue(), KeyFactory.keyToString(entity.getKey()), base);
    }
    public String getBlog(String sender, String subject, Date date, String body, String key, URL base) throws EntityNotFoundException
    {
        return getBlog(new Email(sender), subject, date, body, key, base);
    }
    public String getBlog(Email sender, String subject, Date date, String body, String key, URL base) throws EntityNotFoundException
    {
        Settings senderSettings = Objects.nonNull(getSettingsFor(sender));
        Locale locale = senderSettings.getLocale();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        String dateString = dateFormat.format(date);
        return String.format(locale, senderSettings.getBlogTemplate(), subject, dateString, senderSettings.getNickname(), body, base.toString(), key);
    }
    public String getCalendar() throws EntityNotFoundException, IOException
    {
        Settings settings = getSettings();
        Locale locale = settings.getLocale();
        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        String[] months = dfs.getMonths();
        Calendar calendar = Calendar.getInstance(locale);
        Map<Integer,Map<Integer,List<Entity>>> yearMap = new TreeMap<Integer,Map<Integer,List<Entity>>>(Collections.reverseOrder());
        Query query = new Query(BlogKind);
        query.setFilter(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
        query.addSort(DateProperty, Query.SortDirection.DESCENDING);
        PreparedQuery prepared = prepare(query);
        for (Entity entity : prepared.asIterable(FetchOptions.Builder.withDefaults()))
        {
            String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
            Date date = (Date) Objects.nonNull(entity.getProperty(DateProperty));
            Email sender = (Email) Objects.nonNull(entity.getProperty(SenderProperty));
            Text body = (Text) Objects.nonNull(entity.getProperty(HtmlProperty));
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            Map<Integer,List<Entity>> monthMap = yearMap.get(year);
            if (monthMap == null)
            {
                monthMap = new TreeMap<Integer,List<Entity>>(Collections.reverseOrder());
                yearMap.put(year, monthMap);
            }
            List<Entity> list = monthMap.get(month);
            if (list == null)
            {
                list = new ArrayList<Entity>();
                monthMap.put(month, list);
            }
            list.add(entity);
            //list.add("<div class=\"blog-entry\" id=\""+KeyFactory.keyToString(entity.getKey())+"\">"+subject+"</div>");
        }
        StringBuilder sb = new StringBuilder();
        int yearCount = 0;
        int monthCount = 0;
        int yearPtr = 0;
        int monthPtr = 0;
        for (int year : yearMap.keySet())
        {
            sb.append("<div id=\"year"+year+"\" class=\"calendar-menu\">"+year+" (");
            yearPtr = sb.length();
            sb.append(")</div>\n");
            sb.append("<div class=\"hidden year"+year+" calendar-indent\">\n");
            Map<Integer,List<Entity>> monthMap = yearMap.get(year);
            for (int month : monthMap.keySet())
            {
                List<Entity> blogList = monthMap.get(month);
                Date end = (Date) Objects.nonNull(blogList.get(0).getProperty(DateProperty));
                Date begin = (Date) Objects.nonNull(blogList.get(blogList.size()-1).getProperty(DateProperty));
                BlogCursor bc = new BlogCursor()
                        .setBegin(begin)
                        .setEnd(end);
                String monthId = bc.getWebSafe();
                sb.append("<div id=\""+monthId+"\" class=\"calendar-menu\">"+prettify(months[month])+" (");
                monthPtr = sb.length();
                sb.append(")</div>\n");
                sb.append("<div class=\"hidden "+monthId+" calendar-indent\">\n");
                for (Entity entity : blogList)
                {
                    String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
                    sb.append("<div class=\"blog-entry\" id=\""+KeyFactory.keyToString(entity.getKey())+"\">"+subject+"</div>");
                    yearCount++;
                    monthCount++;
                }
                sb.insert(monthPtr, monthCount);
                monthCount = 0;
                sb.append("</div>\n");
            }
            sb.append("</div>\n");
            sb.insert(yearPtr, yearCount);
            yearCount = 0;
        }
        return sb.toString();
    }
    private String prettify(String str)
    {
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }
    public Settings getSettings()
    {
        try
        {
            Key key = KeyFactory.createKey(Root, SettingsKind, BaseKey);
            Entity entity = get(key);
            if (entity != null)
            {
                return new Settings(this, entity);
            }
            else
            {
                throw new IllegalArgumentException("Root Settings not found");
            }
        }
        catch (EntityNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public Settings getSettingsFor(Email email) throws EntityNotFoundException
    {
        Key parent = KeyFactory.createKey(Root, SettingsKind, BaseKey);
        Key key = KeyFactory.createKey(parent, SettingsKind, email.getEmail());
        Entity entity = get(key);
        if (entity != null)
        {
            return new Settings(this, entity);
        }
        else
        {
            throw new IllegalArgumentException(email.getEmail());
        }
    }

    public void deleteWithChilds(Key key)
    {
        Transaction tr = beginTransaction(TransactionOptions.Builder.withXG(true));
        try
        {
            Query query = new Query();
            query.setAncestor(key);
            query.setKeysOnly();
            PreparedQuery prepared = prepare(query);
            for (Entity entity : prepared.asIterable())
            {
                delete(entity.getKey());
                System.err.println("delete "+entity.getKey());
            }
            delete(key);
            System.err.println("delete "+key);
            tr.commit();
        }
        finally
        {
            if (tr.isActive())
            {
                tr.rollback();
            }
        }
    }

    public void saveBlog(Entity blog)
    {
        assert BlogKind.equals(blog.getKind());
        Searches.saveBlog(blog);
    }

    public String getKeywordSelect()
    {   // TODO performance
        StringBuilder sb = new StringBuilder();
        Query query = new Query(BlogKind);

        PreparedQuery prepared = prepare(query);
        Map<String,Integer> map = new TreeMap<String, Integer>();
        for (Entity blog : prepared.asIterable())
        {
            Collection<String> kwSet = (Collection<String>) blog.getProperty(KeywordsProperty);
            if (kwSet != null)
            {
                for (String kw : kwSet)
                {
                    if (!kw.isEmpty())
                    {
                        Integer count = map.get(kw);
                        if (count == null)
                        {
                            map.put(kw, 1);
                        }
                        else
                        {
                            map.put(kw, count+1);
                        }
                    }
                }
            }
        }
        sb.append("<select id=\"keywordSelect\">");
        sb.append("<option>------</option>");
        for (String kw : map.keySet())
        {
            sb.append("<option value=\"Keyword:"+kw+"\">"+kw+"("+map.get(kw) +")</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

}
