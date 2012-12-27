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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Timo Vesalainen
 */
public class DB implements BlogConstants
{
    public static DB DB = new DB();
    //private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    //private MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
    //private Key root = KeyFactory.createKey(RootKind, 1);

    private DB()
    {
    }

    public Transaction beginTransaction()
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.beginTransaction(TransactionOptions.Builder.withXG(true));
    }
    public void delete(Key key)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.delete(key);
    }
    public Entity getPageEntity(String path)
    {
        return getPageEntity(KeyFactory.createKey(PageKind, path));
    }
    public Entity getPageEntity(Key key)
    {
        try
        {
            return get(key);
        }
        catch (EntityNotFoundException ex)
        {
            /*
            Entity base = null;
            String namespace = NamespaceManager.get();
            if (namespace != null && !namespace.isEmpty())
            {
                try
                {
                    NamespaceManager.set(null);
                    try
                    {
                        key = KeyFactory.createKey(PageKind, key.getName());
                        base = get(key);
                    }
                    catch (EntityNotFoundException ex1)
                    {
                    }
                }
                finally
                {
                    NamespaceManager.set(namespace);
                }
            }
            if (base != null)
            {
                Entity entity = new Entity(key);
                entity.setPropertiesFrom(base);
                System.err.println(entity);
                return entity;
            }
            */
        }
        return new Entity(key);
    }
    public void setPage(Entity page)
    {
        Transaction tr = beginTransaction();
        try
        {
            Entity backup = new Entity(PageBackupKind, System.currentTimeMillis(), page.getKey());
            backup.setPropertiesFrom(page);
            put(backup);
            putAndCache(page);
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
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key =  getBlogKey(messageId);
        try
        {
            return datastore.get(key);
        }
        catch (EntityNotFoundException ex)
        {
            blogsChanged();
            return new Entity(key);
        }
    }
    public void blogsChanged()
    {
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        cache.delete(FirstPageKey);
        cache.delete(CalendarKey);
    }
    public Key getMetadataKey(String sha1)
    {
        Key root = KeyFactory.createKey(RootKind, 1);
        return KeyFactory.createKey(root, MetadataKind, sha1);
    }
    public Entity getMetadata(String sha1)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        Key metadataKey = getMetadataKey(sha1);
        Entity metadata = (Entity) cache.get(metadataKey);
        if (metadata != null)
        {
            return metadata;
        }
        try
        {
            return datastore.get(metadataKey);
        }
        catch (EntityNotFoundException ex)
        {
            return new Entity(metadataKey);
        }
    }
    
    public Key put(Entity entity)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.put(entity);
    }

    public Key putAndCache(Entity entity)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        Key key = datastore.put(entity);
        cache.put(key, entity);
        return key;
    }

    public Entity get(Key key) throws EntityNotFoundException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        Entity entity = (Entity) cache.get(key);
        if (entity != null)
        {
            return entity;
        }
        return datastore.get(key);
    }

    public void deleteBlog(Key key)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        assert BlogKind.equals(key.getKind());
        cache.delete(key);
        blogsChanged();
        datastore.delete(key);
    }

    public Entity get(String keyString) throws EntityNotFoundException
    {
        return get(KeyFactory.stringToKey(keyString));
    }

    public String getBlogList(String start) throws EntityNotFoundException
    {
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        String str = null;
        if (start == null)
        {
            str = (String) cache.get(FirstPageKey);
        }
        else
        {
            str = (String) cache.get(start);
        }
        if (str == null)
        {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Settings settings = getSettings();
            FetchOptions options = FetchOptions.Builder.withLimit(settings.getShowCount());
            if (start != null)
            {
                options = options.startCursor(Cursor.fromWebSafeString(start));
            }
            StringBuilder sb = new StringBuilder();
            Query query = new Query(BlogKind);
            query.addSort(SentDateProperty, Query.SortDirection.DESCENDING);
            PreparedQuery prepared = datastore.prepare(query);
            QueryResultList<Entity> list = prepared.asQueryResultList(options);
            Cursor cursor = list.getCursor();
            for (Entity entity : list)
            {
                sb.append(getBlog(entity));
            }
            if (list.size() == settings.getShowCount())
            {
                sb.append("<span id=\"nextPage\" class=\"hidden\">"+cursor.toWebSafeString()+"</span>");
            }
            str = sb.toString();
            if (start == null)
            {
                cache.put(FirstPageKey, str);
            }
            else
            {
                cache.put(start, str);
            }
        }
        return str;
    }

    public String getBlog(String keyString) throws EntityNotFoundException
    {
        Entity entity = get(keyString);
        return getBlog(entity);
        
    }
    public String getBlog(Entity entity) throws EntityNotFoundException
    {
        String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
        Date date = (Date) Objects.nonNull(entity.getProperty(SentDateProperty));
        Email sender = (Email) Objects.nonNull(entity.getProperty(SenderProperty));
        Text body = (Text) Objects.nonNull(entity.getProperty(HtmlProperty));
        Settings senderSettings = Objects.nonNull(getSettingsFor(sender));
        return String.format(senderSettings.getTemplate().getValue(), subject, date, senderSettings.getNickname(), body.getValue());
    }
    
    public String getCalendar() throws EntityNotFoundException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        String str = (String) cache.get(CalendarKey);
        if (str == null)
        {
            Settings settings = getSettings();
            Locale locale = settings.getLocale();
            DateFormatSymbols dfs = new DateFormatSymbols(locale);
            String[] months = dfs.getMonths();
            Calendar calendar = Calendar.getInstance(locale);
            Map<Integer,Map<Integer,List<String>>> yearMap = new TreeMap<Integer,Map<Integer,List<String>>>(Collections.reverseOrder());
            Query query = new Query(BlogKind);
            query.addSort(SentDateProperty, Query.SortDirection.DESCENDING);
            PreparedQuery prepared = datastore.prepare(query);
            for (Entity entity : prepared.asIterable(FetchOptions.Builder.withDefaults()))
            {
                String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
                Date date = (Date) Objects.nonNull(entity.getProperty(SentDateProperty));
                Email sender = (Email) Objects.nonNull(entity.getProperty(SenderProperty));
                Text body = (Text) Objects.nonNull(entity.getProperty(HtmlProperty));
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                Map<Integer,List<String>> monthMap = yearMap.get(year);
                if (monthMap == null)
                {
                    monthMap = new TreeMap<Integer,List<String>>(Collections.reverseOrder());
                    yearMap.put(year, monthMap);
                }
                List<String> list = monthMap.get(month);
                if (list == null)
                {
                    list = new ArrayList<String>();
                    monthMap.put(month, list);
                }
                list.add("<div class=\"blog-entry\" id=\""+KeyFactory.keyToString(entity.getKey())+"\">"+subject+"</div>");
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
                Map<Integer,List<String>> monthMap = yearMap.get(year);
                for (int month : monthMap.keySet())
                {
                    sb.append("<div id=\"month"+year+month+"\" class=\"calendar-menu\">"+prettify(months[month])+" (");
                    monthPtr = sb.length();
                    sb.append(")</div>\n");
                    sb.append("<div class=\"hidden month"+year+month+" calendar-indent\">\n");
                    List<String> blogList = monthMap.get(month);
                    for (String a : blogList)
                    {
                        sb.append(a);
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
            str = sb.toString();
            cache.put(CalendarKey, str);
        }
        return str;
    }
    private String prettify(String str)
    {
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }
    public Settings getSettings() throws EntityNotFoundException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        Settings settings = (Settings) cache.get(BaseKey);
        if (settings == null)
        {
            Key key = KeyFactory.createKey(SettingsKind, BaseKey);
            Entity entity = datastore.get(key);
            if (entity != null)
            {
                settings = new Settings(this, entity);
                cache.put(BaseKey, settings);
            }
            else
            {
                throw new IllegalArgumentException("Root Settings not found");
            }
        }
        return settings;
    }
    public Settings getSettingsFor(Email email) throws EntityNotFoundException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        Settings settings = (Settings) cache.get(email);
        if (settings == null)
        {
            Query query = new Query(SettingsKind);
            query.setFilter(new FilterPredicate(EmailProperty, Query.FilterOperator.EQUAL, email));
            PreparedQuery prepared = datastore.prepare(query);
            Entity entity = prepared.asSingleEntity();
            if (entity != null)
            {
                settings = new Settings(this, entity);
                cache.put(email, settings);
            }
            else
            {
                throw new IllegalArgumentException(email.getEmail());
            }
        }
        return settings;
    }

}
