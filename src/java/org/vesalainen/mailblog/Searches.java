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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * @author Timo Vesalainen
 */
public class Searches implements BlogConstants
{

    static void saveBlog(Entity blog)
    {
        DB db = DB.DB;
        Settings settings = db.getSettings();
        Locale locale = settings.getLocale();
        String subject = (String) blog.getProperty(SubjectProperty);
        Text html = (Text) blog.getProperty(HtmlProperty);
        Date date = (Date) blog.getProperty(DateProperty);
        Set<String> keywords = (Set) blog.getProperty(KeywordsProperty);
        Document.Builder builder = Document.newBuilder();
        builder.setId(blog.getKey().getName());
        builder.setLocale(locale);
        builder.addField(Field.newBuilder()
                .setName(SubjectProperty)
                .setText(subject));
        builder.addField(Field.newBuilder()
                .setName(HtmlProperty)
                .setHTML(html.getValue()));
        builder.addField(Field.newBuilder()
                .setName(DateProperty)
                .setDate(date));
        if (keywords != null)
        {
            for (String kw : keywords)
            {
                builder.addField(Field.newBuilder()
                        .setName("Keyword")
                        .setText(kw));
            }
        }
        Document document = builder.build();
        SearchService searchService = SearchServiceFactory.getSearchService();
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName("BlogIndex"));        
        index.put(document);
    }

    public static String getBlogListFromSearch(BlogCursor bc) throws EntityNotFoundException
    {
        DB db = DB.DB;
        StringBuilder sb = new StringBuilder();
        SearchService searchService = SearchServiceFactory.getSearchService();
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName("BlogIndex"));        
        Results<ScoredDocument> result = index.search(bc.getSearch());
        for (ScoredDocument sd : result)
        {
            String id = sd.getId();
            Entity blog = db.getBlogFromMessageId(id);
            sb.append(db.getBlog(blog));
        }
        return sb.toString();
    }

}
