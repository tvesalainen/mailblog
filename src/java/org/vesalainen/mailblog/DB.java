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

import com.google.appengine.api.datastore.Blob;
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
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

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
        return datastore.beginTransaction();
    }
    public Key createBlogKey(String messageId)
    {
        return KeyFactory.createKey(root, BlogKind, messageId);
    }
    public void put(Entity entity)
    {
        cache.put(entity.getKey(), entity);
        cache.delete(CacheListKey);
        datastore.put(entity);
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

    public void delete(Key key)
    {
        cache.delete(key);
        cache.delete(CacheListKey);
        datastore.delete(key);
    }

    public Entity get(String keyString) throws EntityNotFoundException
    {
        return get(KeyFactory.stringToKey(keyString));
    }

    public String getBlogList(Locale locale)
    {
        String list = (String) cache.get(CacheListKey);
        if (list == null)
        {
            DateFormat dateParser = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            StringBuilder sb = new StringBuilder();
            Query query = new Query(BlogKind);
            query.addSort(SentDateProperty, Query.SortDirection.DESCENDING);
            PreparedQuery prepared = datastore.prepare(query);
            sb.append("<table>");
            for (Entity entity : prepared.asIterable(FetchOptions.Builder.withChunkSize(CHUNKSIZE)))
            {
                String subject = (String) entity.getProperty(SubjectProperty);
                Date date = (Date) entity.getProperty(SentDateProperty);
                Email sender = (Email) entity.getProperty(SenderProperty);
                sb.append("<tr>");
                sb.append("<td>");
                sb.append("<a class=\"blog-link\" href=\"/?blog="+KeyFactory.keyToString(entity.getKey()) +"\">");
                sb.append(subject);
                sb.append("</a>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append(dateParser.format(date));
                sb.append("</td>");
                sb.append("<td>");
                sb.append(sender.getEmail());
                sb.append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            list = sb.toString();
            cache.put(CacheListKey, list);
        }
        return list;
    }

    public String addBlob(Key blogKey, String filename, String contentType, byte[] toByteArray)
    {
        Entity blob = new Entity(BlobKind, blogKey);
        blob.setProperty(TimestampProperty, new Date());
        blob.setProperty(FilenameProperty, filename);
        blob.setProperty(ContentTypeProperty, contentType);
        blob.setProperty(BlobProperty, new Blob(toByteArray));
        Key key = datastore.put(blob);
        cache.put(key, blob);
        return KeyFactory.keyToString(key);
    }

}
