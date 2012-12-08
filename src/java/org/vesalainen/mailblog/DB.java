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
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
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
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
    private Key root = KeyFactory.createKey(RootKind, 1);

    public DB()
    {
    }

    public Transaction beginTransaction()
    {
        return datastore.beginTransaction(TransactionOptions.Builder.withXG(true));
    }
    public Entity getBlogFromMessageId(String messageId)
    {
        Key key =  KeyFactory.createKey(root, BlogKind, messageId);
        try
        {
            return datastore.get(key);
        }
        catch (EntityNotFoundException ex)
        {
            cache.delete(LatestKey);
            cache.delete(CalendarKey);
            return new Entity(key);
        }
    }
    public Entity getMetadataFromDate(Date date)
    {
        Key timelineKey =  KeyFactory.createKey(root, TimelineKind, date.getTime());
        Entity timeline = new Entity(timelineKey);
        datastore.put(timeline);
        return new Entity(MetadataKind, timelineKey);
    }
    
    public Key put(Entity entity)
    {
        cache.put(entity.getKey(), entity);
        return datastore.put(entity);
    }

    public Entity get(Key key) throws EntityNotFoundException
    {
        Entity entity = (Entity) cache.get(key);
        if (entity != null)
        {
            return entity;
        }
        return datastore.get(key);
    }

    public void deleteBlog(Key key)
    {
        assert BlogKind.equals(key.getKind());
        cache.delete(key);
        cache.delete(LatestKey);
        cache.delete(CalendarKey);
        datastore.delete(key);
    }

    public Entity get(String keyString) throws EntityNotFoundException
    {
        return get(KeyFactory.stringToKey(keyString));
    }

    public String getBlogList() throws EntityNotFoundException
    {
        String str = (String) cache.get(LatestKey);
        if (str == null)
        {
            Settings settings = getSettings();
            StringBuilder sb = new StringBuilder();
            Query query = new Query(BlogKind);
            query.addSort(SentDateProperty, Query.SortDirection.DESCENDING);
            PreparedQuery prepared = datastore.prepare(query);
            for (Entity entity : prepared.asIterable(FetchOptions.Builder.withLimit(settings.getShowCount())))
            {
                sb.append(getBlog(entity));
            }
            str = sb.toString();
            cache.put(LatestKey, str);
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
        String str = (String) cache.get(CalendarKey);
        if (str == null)
        {
            Settings settings = getSettings();
            Locale locale = settings.getLocale();
            DateFormatSymbols dfs = new DateFormatSymbols(locale);
            String[] months = dfs.getMonths();
            Calendar calendar = Calendar.getInstance(locale);
            Map<Integer,Map<Integer,List<String>>> yearMap = new TreeMap<Integer,Map<Integer,List<String>>>();
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
                    monthMap = new TreeMap<Integer,List<String>>();
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
                sb.append("<div class=\"year"+year+" calendar-indent\">\n");
                Map<Integer,List<String>> monthMap = yearMap.get(year);
                for (int month : monthMap.keySet())
                {
                    sb.append("<div id=\"month"+year+month+"\" class=\"calendar-menu\">"+prettify(months[month])+" (");
                    monthPtr = sb.length();
                    sb.append(")</div>\n");
                    sb.append("<div class=\"month"+year+month+" calendar-indent\">\n");
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
        Settings settings = (Settings) cache.get(RootKey);
        if (settings == null)
        {
            Key key = KeyFactory.createKey(SettingsKind, RootKey);
            Entity entity = datastore.get(key);
            if (entity != null)
            {
                settings = new Settings(this, entity);
                cache.put(RootKey, settings);
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
