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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.QueryOptions.Builder;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import static org.vesalainen.mailblog.BlogConstants.*;
import org.vesalainen.mailblog.DS.CacheWriter;

/**
 * @author Timo Vesalainen
 */
public class Searches
{

    public static void saveBlog(Entity blog)
    {
        SearchService searchService = SearchServiceFactory.getSearchService();
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName(BlogIndex));        
        String id = KeyFactory.keyToString(blog.getKey());
        Boolean publish = (Boolean) blog.getProperty(PublishProperty);
        if (publish != null && publish)
        {
            DS ds = DS.get();
            Settings settings = ds.getSettings();
            Locale locale = settings.getLocale();
            String subject = (String) blog.getProperty(SubjectProperty);
            Email sender = (Email) blog.getProperty(SenderProperty);
            Text html = (Text) blog.getProperty(HtmlProperty);
            Date date = (Date) blog.getProperty(DateProperty);
            GeoPt location = (GeoPt) blog.getProperty(LocationProperty);
            Set<String> keywords = (Set) blog.getProperty(KeywordsProperty);
            Document.Builder builder = Document.newBuilder()
                .setId(id)
                .setLocale(locale)
                .addField(Field.newBuilder()
                        .setName(SubjectProperty)
                        .setText(subject));
            if (sender != null)
            {
                String senderEmail = sender.getEmail();
                builder.addField(Field.newBuilder()
                        .setName(SenderProperty)
                        .setText(senderEmail));
            }
            String htmlValue = html.getValue();
            if (htmlValue != null)
            {
                builder.addField(Field.newBuilder()
                        .setName(HtmlProperty)
                        .setHTML(htmlValue));
            }
            builder.addField(Field.newBuilder()
                    .setName(DateProperty)
                    .setDate(date));
            if (keywords != null)
            {
                for (String kw : keywords)
                {
                    builder.addField(Field.newBuilder()
                            .setName(KeywordProperty)
                            .setAtom(kw));
                }
            }
            if (location != null)
            {
                builder.addField(Field.newBuilder()
                        .setName(LocationProperty)
                        .setGeoPoint(new GeoPoint(location.getLatitude(), location.getLongitude())));
            }
            Document document = builder.build();
            index.put(document);
        }
        else
        {
            index.delete(id);
        }
    }

    public static void deleteBlog(Key blogKey)
    {
        SearchService searchService = SearchServiceFactory.getSearchService();
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName(BlogIndex));        
        String id = KeyFactory.keyToString(blogKey);
        index.delete(id);
    }
    
    public static void getBlogListFromSearch(BlogCursor bc, URL base, CacheWriter sb) throws HttpException, IOException
    {
        DS ds = DS.get();
        Settings settings = ds.getSettings();
        SearchService searchService = SearchServiceFactory.getSearchService();
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName(BlogIndex));
        Builder optionsBuilder = QueryOptions.newBuilder();
        //optionsBuilder.setLimit(settings.getShowCount());
        optionsBuilder.setFieldsToReturn(SubjectProperty, DateProperty, SenderProperty);
        optionsBuilder.setFieldsToSnippet(HtmlProperty);
        Cursor searchCursor = bc.getSearchCursor();
        if (searchCursor != null)
        {
            optionsBuilder.setCursor(searchCursor);
        }
        System.err.println(bc.getSearch());
        Query query = Query.newBuilder().setOptions(optionsBuilder.build()).build(bc.getSearch());
        Results<ScoredDocument> result = index.search(query);
        for (ScoredDocument sd : result)
        {
            String id = sd.getId();
            Entity blog = ds.getEntityForKey(id);
            if (blog != null)
            {
                sb.append(ds.getSearchResults(
                        sd.getOnlyField(SenderProperty).getText(), 
                        sd.getOnlyField(SubjectProperty).getText(), 
                        sd.getOnlyField(DateProperty).getDate(), 
                        sd.getExpressions().get(0).getHTML(),
                        id,
                        base
                        ));
            }
            else
            {
                System.err.println("deleting missing blog id="+id+" in search");
                index.delete(id);
            }
        }
        bc.setSearchCursor(result.getCursor());
        sb.append("<span id=\"nextPage\" class=\"hidden\">"+bc.getWebSafe()+"</span>");
        sb.cache();
    }

}
