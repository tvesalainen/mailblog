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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import com.google.apphosting.api.ApiProxy;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import static org.vesalainen.mailblog.BlogConstants.*;
import static org.vesalainen.mailblog.CachingDatastoreService.getRootKey;
import org.vesalainen.mailblog.GeoJSON.Feature;
import org.vesalainen.mailblog.GeoJSON.LineString;
import org.vesalainen.mailblog.GeoJSON.Point;
import org.vesalainen.mailblog.types.GeoPtType;
import org.vesalainen.mailblog.types.TimeSpan;
import org.vesalainen.rss.Channel;
import org.vesalainen.rss.Item;
import org.vesalainen.rss.RSS;

/**
 * @author Timo Vesalainen
 */
public class DS extends CachingDatastoreService
{
    private static final String Resources = "Resources";
    
    private static Map<String, DS> nsMap = new HashMap<>();
    private String namespace;

    private DS(String namespace)
    {
        this.namespace = namespace;
    }

    public static DS get()
    {
        String ns = NamespaceManager.get();
        if (ns == null)
        {
            ns = "";
        }
        DS ds = nsMap.get(ns);
        if (ds == null)
        {
            ds = new DS(ns);
            nsMap.put(ns, ds);
        }
        return ds;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public List<String> getNamespaceList()
    {
        List<String> list = new ArrayList<>();
        list.add("");
        Query query = new Query(Entities.NAMESPACE_METADATA_KIND);
        PreparedQuery prepared = prepare(query);
        for (Entity entity : prepared.asIterable())
        {
            list.add(Entities.getNamespaceFromNamespaceKey(entity.getKey()));
        }
        return list;
    }

    public String createNamespaceSelect()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<select name=\"namespace\" class=\"namespaceSelect\">");
        Query query = new Query(Entities.NAMESPACE_METADATA_KIND);
        PreparedQuery prepared = prepare(query);
        for (Entity entity : prepared.asIterable())
        {
            String ns = Entities.getNamespaceFromNamespaceKey(entity.getKey());
            if (Objects.equal(namespace, ns))
            {
                sb.append("<option selected value=\"" + ns + "\">" + ns + "</option>");
            }
            else
            {
                sb.append("<option value=\"" + ns + "\">" + ns + "</option>");
            }
        }
        sb.append("</select>");
        return sb.toString();
    }

    public static String getBlogDigest(Entity blog)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            DigestOutputStream dos = new DigestOutputStream(new NullOutputStream(), messageDigest);
            ObjectOutputStream oos = new ObjectOutputStream(dos);
            oos.writeObject(blog.getKey());
            oos.writeObject(blog.getProperty(SubjectProperty));
            oos.writeObject(blog.getProperty(SenderProperty));
            oos.writeObject(blog.getProperty(DateProperty));
            oos.close();
            byte[] digest = messageDigest.digest();
            return Hex.convert(digest);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public static String getDigest(Serializable serializable)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            DigestOutputStream dos = new DigestOutputStream(new NullOutputStream(), messageDigest);
            ObjectOutputStream oos = new ObjectOutputStream(dos);
            oos.writeObject(serializable);
            oos.close();
            byte[] digest = messageDigest.digest();
            return Hex.convert(digest);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public static String getDigest(String str)
    {
        try
        {
            return getDigest(str.getBytes("utf-8"));
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public static String getDigest(byte[] bytes)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(bytes);
            return Hex.convert(digest);
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public CacheWriter createCacheWriter(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        return new CacheWriter(request, response);
    }

    public CacheOutputStream createCacheOutputStream(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        return new CacheOutputStream(request, response);
    }

    public boolean sameETagOrCached(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        return sameETag(request, response) || serveFromCache(request, response);
    }

    public boolean serveFromCache(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String cacheKey = getCacheKey(request);
        CachedContent cachedContent = (CachedContent) cache.get(cacheKey);
        if (cachedContent != null)
        {
            System.err.println("serve " + cacheKey);
            cachedContent.serve(response);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean sameETag(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String ifNoneMatch = request.getHeader("If-None-Match");
        response.setHeader("ETag", getETag());
        if (ifNoneMatch != null)
        {
            if (!changedETAG(ifNoneMatch))
            {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return true;
            }
        }
        return false;
    }

    public void clearCache(HttpServletRequest request)
    {
        String cacheKey = getCacheKey(request);
        if (cacheKey != null)
        {
            putToCache(cacheKey, null);
        }
    }
    public String getCacheKey(HttpServletRequest request)
    {
        String cacheKey = MaidenheadLocator2.getCacheKey(request);
        if (cacheKey != null)
        {
            return cacheKey;
        }
        StringBuilder sb = new StringBuilder();
        String ServletPath = request.getServletPath();
        if (ServletPath != null)
        {
            sb.append(ServletPath);
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo != null)
        {
            sb.append(pathInfo);
        }
        String queryString = request.getQueryString();
        if (queryString != null)
        {
            sb.append('?' + queryString);
        }
        return sb.toString();
    }

    public Entity getPageEntity(String path)
    {
        try
        {
            return get(KeyFactory.createKey(getRootKey(), PageKind, path));
        }
        catch (EntityNotFoundException ex)
        {
            return null;
        }
    }

    public void setPage(Entity page)
    {
        assert PageKind.equals(page.getKind());
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

    public static URL getBase(HttpServletRequest request) throws IOException
    {
        try
        {
            URI uri = new URI(request.getRequestURL().toString());
            return uri.resolve("/").toURL();
        }
        catch (MalformedURLException | URISyntaxException ex)
        {
            throw new IOException(ex);
        }
    }

    public Key getBlogKey(String messageId)
    {
        return KeyFactory.createKey(getRootKey(), BlogKind, messageId);
    }

    public Key getTrackKey(String guid)
    {
        return KeyFactory.createKey(getRootKey(), TrackKind, guid);
    }

    public Entity getBlogFromMessageId(String messageId)
    {
        Key key = getBlogKey(messageId);
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
        return KeyFactory.createKey(getRootKey(), MetadataKind, sha1);
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

    public void writeRSS(URL base, CacheWriter cw) throws IOException, HttpException
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
            Date last = new Date(0);
            Query query = new Query(BlogKind);
            query.addSort(DateProperty, Query.SortDirection.DESCENDING);
            PreparedQuery prepared = prepare(query);
            for (Entity blog : prepared.asIterable())
            {
                String subject = (String) blog.getProperty(SubjectProperty);
                Date date = (Date) blog.getProperty(DateProperty);
                if (date.after(last))
                {
                    last = date;
                }
                Email sender = (Email) blog.getProperty(SenderProperty);
                String nickname = getNickname(sender);
                Settings bloggerSettings = getSettingsFor(sender);
                nickname = bloggerSettings.getNickname();
                Item item = new Item();
                item.setAuthor(nickname);
                item.setTitle(subject);
                item.setPubDate(date);
                URI uri = baseUri.resolve("/index.html?blog=" + KeyFactory.keyToString(blog.getKey()));
                item.setLink(uri);
                channel.getItem().add(item);
            }
            channel.setPubDate(last);
            channel.setLastBuildDate(last);
            rss.marshall(cw);
            cw.cache();
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void getBlogList(String nextBlog, URL base, boolean all, CacheWriter sb) throws HttpException, IOException
    {
        Date end = null;
        if (nextBlog != null)
        {
            Key key = KeyFactory.stringToKey(nextBlog);
            Entity entity;
            try
            {
                entity = get(key);
                end = (Date) entity.getProperty(DateProperty);
            }
            catch (EntityNotFoundException ex)
            {
            }
        }
        FetchOptions options = FetchOptions.Builder.withDefaults();
        if (!all)
        {
            options.limit(2);
        }
        Query query = new Query(BlogKind);
        query.addSort(DateProperty, Query.SortDirection.DESCENDING);
        List<Filter> filters = new ArrayList<>();
        filters.add(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
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
        Iterator<Entity> iterator = prepared.asIterator(options);
        if (iterator.hasNext())
        {
            sb.append(getBlog(iterator.next(), base));
        }
        if (iterator.hasNext())
        {
            Entity next = iterator.next();
            sb.append("<a class='lasthref' href=/blog?nextblog=" + KeyFactory.keyToString(next.getKey()) + "></a>");
        }
        else
        {
            sb.append("<p class='lasthref'>");
        }
        sb.cache();
    }

    public void getComments(Key blogKey, User loggedUser, CacheWriter cw) throws HttpException, IOException
    {
        Entity blog;
        try
        {
            blog = get(blogKey);
        }
        catch (EntityNotFoundException ex)
        {
            throw new HttpException(HttpServletResponse.SC_NOT_FOUND, blogKey + " not found");
        }
        Email sender = (Email) blog.getProperty(SenderProperty);
        Settings senderSettings;
        senderSettings = getSettingsFor(sender);
        String commentTemplate = senderSettings.getCommentTemplate();
        Locale locale = senderSettings.getLocale();
        DateFormat dateFormat = senderSettings.getDateFormat();
        Query query = new Query(CommentsKind);
        query.setAncestor(blogKey);
        query.addSort(TimestampProperty, Query.SortDirection.ASCENDING);
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
                cw.append(String.format(locale, commentTemplate, nickname, dateString, text.getValue(), hidden, KeyFactory.keyToString(comment.getKey())));
            }
        }
        cw.cache();
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
        sendMailToAdmins(user.getEmail() + " added comment", text);
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
        sendMailToAdmins(currentUser.getEmail() + " removed comment", "");
    }

    private void sendMailToAdmins(String subject, String message)
    {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try
        {
            Message msg = new MimeMessage(session);
            String appId = ApiProxy.getCurrentEnvironment().getAppId();
            msg.setFrom(new InternetAddress("admin@" + appId + ".appspotmail.com"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("admins"));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);

        }
        catch (AddressException e)
        {
            e.printStackTrace();
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
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
            throw new IllegalArgumentException(email + " not email");
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

    public void writeOpenGraph(URL base, CacheWriter cw) throws HttpException, IOException
    {
        Settings settings = getSettings();
        String title = settings.getTitle();
        if (title != null)
        {
            metaProperty(cw, "og:title", title);
        }
        String description = settings.getDescription();
        if (description != null)
        {
            metaProperty(cw, "og:description", description);
        }
        metaProperty(cw, "og:url", base.toExternalForm());
        Locale locale = settings.getLocale();
        if (locale != null)
        {
            metaProperty(cw, "og:locale", locale.toString());
        }
        String blogImage = settings.getBlogImage();
        if (blogImage != null)
        {
            metaProperty(cw, "og:image", blogImage);
        }
    }

    public void writeOpenGraph(Key blogKey, URL base, CacheWriter cw) throws HttpException, IOException
    {
        Entity blog;
        try
        {
            blog = get(blogKey);
            Settings settings = getSettings();
            String subject = (String) blog.getProperty(SubjectProperty);
            Text text = (Text) blog.getProperty(HtmlProperty);
            String sha1 = findSha1(text.getValue());
            metaProperty(cw, "og:title", subject);
            metaProperty(cw, "og:url", getBlogUrl(blogKey, base));
            String title = settings.getTitle();
            if (title != null)
            {
                metaProperty(cw, "og:site_name", title);
            }
            Locale locale = settings.getLocale();
            if (locale != null)
            {
                metaProperty(cw, "og:locale", locale.toString());
            }
            if (sha1 != null)
            {
                Entity metadata = getMetadata(sha1);
                String contentType = (String) metadata.getProperty(ContentTypeProperty);
                Object width = metadata.getProperty("PixelXDimension");
                Object height = metadata.getProperty("PixelYDimension");
                if (contentType != null)
                {
                    if (contentType.startsWith("image/"))
                    {
                        metaProperty(cw, "og:image", getJPGUrl(metadata.getKey(), base));
                        if (contentType.startsWith("image/jpeg"))
                        {
                            metaProperty(cw, "og:image:type", "image/jpeg");
                        }
                        if (contentType.startsWith("image/gif"))
                        {
                            metaProperty(cw, "og:image:type", "image/gif");
                        }
                        if (contentType.startsWith("image/png"))
                        {
                            metaProperty(cw, "og:image:type", "image/png");
                        }
                        if (width != null && height != null)
                        {
                            metaProperty(cw, "og:image:width", width.toString());
                            metaProperty(cw, "og:image:height", height.toString());
                        }
                    }
                }
            }
        }
        catch (EntityNotFoundException ex)
        {
            throw new HttpException(HttpServletResponse.SC_NOT_FOUND, blogKey + " not found");
        }
    }

    private String getAttachmentUrl(Key key, URL base)
    {
        assert AttachmentsKind.equals(key.getKind());
        try
        {
            URI baseUri = base.toURI();
            URI uri = baseUri.resolve("/blob?" + Sha1Parameter + "=" + key.getName() + "&" + OriginalParameter + "=true");
            return uri.toASCIIString();
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private String getJPGUrl(Key key, URL base)
    {
        assert AttachmentsKind.equals(key.getKind());
        try
        {
            URI baseUri = base.toURI();
            URI uri = baseUri.resolve("/blob/" + key.getName() + ".jpg");
            return uri.toASCIIString();
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private String getBlogUrl(Key key, URL base)
    {
        assert BlogKind.equals(key.getKind());
        try
        {
            URI baseUri = base.toURI();
            URI uri = baseUri.resolve("/index.html?blog=" + KeyFactory.keyToString(key));
            return uri.toASCIIString();
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private void metaProperty(CacheWriter cw, String property, String content) throws IOException
    {
        cw.append("<meta property=\"" + property + "\" content=\"" + content + "\">\n");
    }

    public void getBlog(Key key, URL base, CacheWriter cw) throws HttpException, IOException
    {
        Entity entity;
        try
        {
            entity = get(key);
        }
        catch (EntityNotFoundException ex)
        {
            throw new HttpException(HttpServletResponse.SC_NOT_FOUND, key + " not found");
        }
        cw.append(getBlog(entity, base));
        cw.cache();
    }

    public String getBlog(Entity entity, URL base) throws HttpException, IOException
    {
        Settings settings = getSettings();
        String subject = (String) entity.getProperty(SubjectProperty);
        Date date = (Date) entity.getProperty(DateProperty);
        Email sender = (Email) entity.getProperty(SenderProperty);
        Text body = (Text) entity.getProperty(HtmlProperty);
        GeoPt location = (GeoPt) entity.getProperty(LocationProperty);
        return getBlog(settings.getBlogTemplate(), sender, subject, date, body.getValue(), KeyFactory.keyToString(entity.getKey()), base, location);
    }

    public String getBlog(String sendFrom, String subject, Date date, String body, String key, URL base) throws HttpException
    {
        Email sender = new Email(sendFrom);
        Settings settings = getSettingsFor(sender);
        return getBlog(settings.getBlogTemplate(), sender, subject, date, body, key, base, null);
    }

    public String getSearchResults(String sendFrom, String subject, Date date, String body, String key, URL base) throws HttpException
    {
        Email sender = new Email(sendFrom);
        Settings settings = getSettingsFor(sender);
        return getBlog(settings.getSearchResultTemplate(), sender, subject, date, body, key, base, null);
    }

    private String getBlog(String tmpl, Email sender, String subject, Date date, String body, String key, URL base, GeoPt location) throws HttpException
    {
        Settings settings = getSettingsFor(sender);
        Locale locale = settings.getLocale();
        DateFormat dateFormat = settings.getDateFormat();
        String dateString = dateFormat.format(date);
        String locationString = getLocationString(location, key);
        return String.format(
                locale,
                tmpl,
                subject,
                dateString,
                settings.getNickname(),
                body,
                base.toString(),
                key,
                locationString
        ).replace("\u200B", "");    // Arial font doesn't have non-width-space
    }

    private String getLocationString(GeoPt location, String key)
    {
        if (location != null)
        {
            return "<a class=\"LookAt\"href=\"/kml?" + LookAtParameter + "=" + key + "\">" + GeoPtType.getString(location) + "</a>";
        }
        else
        {
            return "";
        }
    }

    public void getCalendar(CacheWriter cw) throws IOException
    {
        Settings settings = getSettings();
        Locale locale = settings.getLocale();
        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        String[] months = dfs.getMonths();
        Calendar calendar = Calendar.getInstance(locale);
        Map<Integer, Map<Integer, List<Entity>>> yearMap = new TreeMap<Integer, Map<Integer, List<Entity>>>(Collections.reverseOrder());
        Query query = new Query(BlogKind);
        query.setFilter(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
        query.addSort(DateProperty, Query.SortDirection.DESCENDING);
        PreparedQuery prepared = prepare(query);
        for (Entity entity : prepared.asIterable(FetchOptions.Builder.withDefaults()))
        {
            String subject = (String) entity.getProperty(SubjectProperty);
            Date date = (Date) entity.getProperty(DateProperty);
            Email sender = (Email) entity.getProperty(SenderProperty);
            Text body = (Text) entity.getProperty(HtmlProperty);
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            Map<Integer, List<Entity>> monthMap = yearMap.get(year);
            if (monthMap == null)
            {
                monthMap = new TreeMap<Integer, List<Entity>>(Collections.reverseOrder());
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
        for (int year : yearMap.keySet())
        {
            Map<Integer, List<Entity>> monthMap = yearMap.get(year);
            int yearCount = 0;
            for (List<Entity> list : monthMap.values())
            {
                yearCount += list.size();
            }
            cw.append("<div id=\"year" + year + "\" class=\"calendar-menu\">" + year + " (" + yearCount + ")</div>\n");
            cw.append("<div class=\"hidden year" + year + " calendar-indent\">\n");
            for (int month : monthMap.keySet())
            {
                List<Entity> blogList = monthMap.get(month);
                Date end = (Date) blogList.get(0).getProperty(DateProperty);
                Date begin = (Date) blogList.get(blogList.size() - 1).getProperty(DateProperty);
                BlogCursor bc = new BlogCursor()
                        .setBegin(begin)
                        .setEnd(end);
                String monthId = bc.getWebSafe();
                cw.append("<div id=\"" + monthId + "\" class=\"calendar-menu\">" + prettify(months[month]) + " (" + blogList.size() + ")</div>\n");
                cw.append("<div class=\"hidden " + monthId + " calendar-indent\">\n");
                for (Entity entity : blogList)
                {
                    String subject = (String) entity.getProperty(SubjectProperty);
                    cw.append("<div class=\"blog-entry\" id=\"" + KeyFactory.keyToString(entity.getKey()) + "\">" + subject + "</div>");
                }
                cw.append("</div>\n");
            }
            cw.append("</div>\n");
            yearCount = 0;
        }
        cw.cache();
    }

    private String prettify(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public Key createResourceKey()
    {
        return KeyFactory.createKey(getRootKey(), ResourceKind, BaseKey);
    }

    public Resources getResources()
    {
        Resources resources = (Resources) getFromCache(Resources);
        if (resources == null)
        {
            Key key = createResourceKey();
            Entity entity;
            try
            {
                entity = get(key);
            }
            catch (EntityNotFoundException ex)
            {
                entity = new Entity(key);
            }
            try
            {
                resources = new Resources(this, entity);
            }
            catch (EntityNotFoundException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            putToCache(Resources, resources);
        }
        return resources;
    }

    public Key createSettingsKey()
    {
        return KeyFactory.createKey(getRootKey(), SettingsKind, BaseKey);
    }

    public Settings getSettings()
    {
        Settings settings = (Settings) getFromCache("Settings");
        if (settings == null)
        {
            Key key = createSettingsKey();
            Entity entity;
            try
            {
                entity = get(key);
            }
            catch (EntityNotFoundException ex)
            {
                entity = new Entity(key);
            }
            try
            {
                settings = new Settings(this, entity);
            }
            catch (EntityNotFoundException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            putToCache("Settings", settings);
        }
        return settings;
    }

    public Settings getSettingsFor(Email email) throws HttpException
    {
        try
        {
            Key parent = KeyFactory.createKey(getRootKey(), SettingsKind, BaseKey);
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
        catch (EntityNotFoundException ex)
        {
            throw new HttpException(HttpServletResponse.SC_CONFLICT, "settings for " + email.getEmail() + " not found");
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
            }
            delete(key);
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

    public void getKeywordSelect(CacheWriter cw) throws IOException
    {   // TODO performance
        Query query = new Query(BlogKind);

        PreparedQuery prepared = prepare(query);
        Map<String, Integer> map = new TreeMap<String, Integer>();
        for (Entity blog : prepared.asIterable())
        {
            Collection<String> kwSet = (Collection<String>) blog.getProperty(KeywordsProperty);
            if (kwSet != null)
            {
                for (String kw : kwSet)
                {
                    if (kw != null && !kw.isEmpty())
                    {
                        Integer count = map.get(kw);
                        if (count == null)
                        {
                            map.put(kw, 1);
                        }
                        else
                        {
                            map.put(kw, count + 1);
                        }
                    }
                }
            }
        }
        cw.append("<select id=\"keywordSelect\">");
        cw.append("<option>------</option>");
        for (String kw : map.keySet())
        {
            cw.append("<option value=\"Keyword:" + kw + "\">" + kw + "(" + map.get(kw) + ")</option>");
        }
        cw.append("</select>");
        cw.cache();
    }

    public void handleBlogAction(Key blogKey, String action, String auth, CacheWriter cw) throws HttpException, IOException
    {
        try
        {
            Entity blog = get(blogKey);
            String subject = (String) blog.getProperty(SubjectProperty);
            String digest = getBlogDigest(blog);
            if (!digest.equals(auth))
            {
                System.err.println("digest not match " + digest + " <> " + auth + " request is illegal or blog has been changed");
                throw new HttpException(HttpServletResponse.SC_FORBIDDEN);
            }
            if (PublishParameter.equals(action))
            {
                blog.setProperty(PublishProperty, true);
                put(blog);
                cw.append("Blog " + subject + " has been published!");
            }
            else
            {
                if (RemoveParameter.equals(action))
                {
                    deleteWithChilds(blogKey);
                    cw.append("Blog " + subject + " has been deleted!");
                }
                else
                {
                    throw new HttpException(HttpServletResponse.SC_CONFLICT, action + " unknown");
                }
            }
        }
        catch (EntityNotFoundException ex)
        {
            throw new HttpException(HttpServletResponse.SC_NOT_FOUND, blogKey + " not found");
        }
    }

    public void writeLastPosition(CacheWriter cacheWriter, boolean isJSON) throws IOException
    {
        System.err.println(isJSON);
        Settings settings = getSettings();
        Entity lastPlacemark = fetchLastPlacemark(settings);
        if (lastPlacemark != null)
        {
            JSONObject json = null;
            if (isJSON)
            {
                cacheWriter.setContentType("application/json");
                json = new JSONObject();
            }
            describeLocation(lastPlacemark, cacheWriter, json);
            if (isJSON)
            {
                json.write(cacheWriter);
            }
        }
        cacheWriter.cache();
    }

    public void describeLocation(Entity placemark, Appendable out, JSONObject json) throws IOException
    {
        System.err.println(json);
        Settings settings = getSettings();
        Date timestamp;
        if (TrackPointKind.equals(placemark.getKind()))
        {
            timestamp = new Date(placemark.getKey().getId());
        }
        else
        {
            timestamp = (Date) placemark.getProperty(TimestampProperty);
        }
        GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
        MaidenheadLocator2 ml = new MaidenheadLocator2(location);
        String locator = ml.getSubsquare();
        if (timestamp != null && location != null)
        {
            if (json != null)
            {
                json.put(LatitudeParameter, location.getLatitude());
                json.put(LongitudeParameter, location.getLongitude());
                SimpleDateFormat sdf = new SimpleDateFormat(ISO8601Format);
                json.put(TimestampParameter, sdf.format(timestamp));
                json.put(MaidenheadLocatorParameter, locator);
            }
            else
            {
                out.append("<span class=\"timestamp\">");
                DateFormat dateFormat = settings.getDateFormat();
                out.append(dateFormat.format(timestamp));
                out.append(" </span>");
                out.append("<span class=\"position\">");
                String locationString = GeoPtType.getString(location);
                out.append(locationString);
                out.append(" </span>");
                out.append("<span class=\"maidenhead\" title=\"Maidenhead Locator\">");
                out.append(locator);
                out.append(" </span>");
            }
        }
    }

    public Iterable<Entity> fetchLastPlacemarks(Settings settings)
    {
        RunInNamespace<Iterable<Entity>> rin = new RunInNamespace()
        {
            @Override
            protected Iterable<Entity> run()
            {
                Query placemarkQuery = new Query(PlacemarkKind);
                placemarkQuery.addSort(TimestampProperty, Query.SortDirection.DESCENDING);
                PreparedQuery placemarkPrepared = prepare(placemarkQuery);
                return placemarkPrepared.asIterable();
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    public Entity fetchLastPlacemark(Settings settings)
    {
        RunInNamespace<Entity> rin = new RunInNamespace()
        {
            @Override
            protected Entity run()
            {
                Query placemarkQuery = new Query(PlacemarkKind);
                placemarkQuery.addSort(TimestampProperty, Query.SortDirection.DESCENDING);
                PreparedQuery placemarkPrepared = prepare(placemarkQuery);
                for (Entity placemark : placemarkPrepared.asIterable(FetchOptions.Builder.withLimit(1)))
                {
                    return placemark;
                }
                return null;
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    public Iterable<Entity> fetchPlacemarks()
    {
        Settings settings = getSettings();
        RunInNamespace<Iterable<Entity>> rin = new RunInNamespace()
        {
            @Override
            protected Iterable<Entity> run()
            {
                Query placemarkQuery = new Query(PlacemarkKind);
                placemarkQuery.addSort(TimestampProperty);
                PreparedQuery placemarkPrepared = prepare(placemarkQuery);
                return placemarkPrepared.asIterable();
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    public <T> Iterable<Entity> fetchPlacemarks(final T after)
    {
        final String filterProperty;
        if (after instanceof Date)
        {
            filterProperty = TimestampProperty;
        }
        else
        {
            if (after instanceof Key)
            {
                filterProperty = Entity.KEY_RESERVED_PROPERTY;
            }
            else
            {
                throw new IllegalArgumentException("unexpected type " + after);
            }
        }
        Settings settings = getSettings();
        RunInNamespace<Iterable<Entity>> rin = new RunInNamespace()
        {
            @Override
            protected Iterable<Entity> run()
            {
                Query placemarkQuery = new Query(PlacemarkKind);
                placemarkQuery.setFilter(new FilterPredicate(filterProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, after));
                placemarkQuery.addSort(filterProperty);
                System.err.println(placemarkQuery);
                PreparedQuery placemarkPrepared = prepare(placemarkQuery);
                return placemarkPrepared.asIterable();
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    public Iterable<Entity> fetchTracks()
    {
        Settings settings = getSettings();
        RunInNamespace<Iterable<Entity>> rin = new RunInNamespace()
        {
            @Override
            protected Iterable<Entity> run()
            {
                Query trackSeqQuery = new Query(TrackKind);
                PreparedQuery trackSeqPrepared = prepare(trackSeqQuery);
                return trackSeqPrepared.asIterable();
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    public Iterable<Entity> fetchTrackSeqs()
    {
        return fetchTrackSeqs(null);
    }

    public Iterable<Entity> fetchTrackSeqs(final Key ancestor)
    {
        Settings settings = getSettings();
        RunInNamespace<Iterable<Entity>> rin = new RunInNamespace()
        {
            @Override
            protected Iterable<Entity> run()
            {
                Query trackSeqQuery = new Query(TrackSeqKind);
                if (ancestor != null)
                {
                    trackSeqQuery.setAncestor(ancestor);
                }
                trackSeqQuery.addSort(BeginProperty);
                PreparedQuery trackSeqPrepared = prepare(trackSeqQuery);
                return trackSeqPrepared.asIterable();
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    /**
     * Returns begin of the first TrackSeq or current date if no TrackSeqs
     * found.
     *
     * @return
     */
    public Date getTrackSeqsBegin()
    {
        Date begin = (Date) getFromCache("TrackSeqsBegin");
        if (begin != null)
        {
            return begin;
        }
        Settings settings = getSettings();
        RunInNamespace<Date> rin = new RunInNamespace()
        {
            @Override
            protected Date run()
            {
                Date begin = new Date();
                Query trackSeqQuery = new Query(TrackSeqKind);
                PreparedQuery trackSeqPrepared = prepare(trackSeqQuery);
                for (Entity ts : trackSeqPrepared.asIterable())
                {
                    Date b = (Date) ts.getProperty(BeginProperty);
                    if (begin == null || begin.after(b))
                    {
                        begin = b;
                    }
                }
                return begin;
            }
        };
        begin = rin.doIt(null, settings.isCommonPlacemarks());
        putToCache("TrackSeqsBegin", begin);
        return begin;
    }

    public Iterable<Entity> fetchTrackPoints(final Key trackSeqKey)
    {
        Settings settings = getSettings();
        RunInNamespace<Iterable<Entity>> rin = new RunInNamespace()
        {
            @Override
            protected Iterable<Entity> run()
            {
                Query trackPointQuery = new Query(TrackPointKind);
                trackPointQuery.setAncestor(trackSeqKey);
                trackPointQuery.addSort(Entity.KEY_RESERVED_PROPERTY);
                PreparedQuery trackPointPrepared = prepare(trackPointQuery);
                return trackPointPrepared.asIterable();
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    public Iterable<Entity> fetchImageMetadata(Date begin, Date end)
    {
        return fetchImageMetadata(begin, end, false);
    }

    public Iterable<Entity> fetchImageMetadata(Date begin, Date end, boolean withoutLocation)
    {
        PreparedQuery metadataPrepared = createImageMetadataQuery(begin, end, withoutLocation);
        return metadataPrepared.asIterable();
    }

    public boolean hasImageMetadata(Date begin, Date end, boolean withoutLocation)
    {
        PreparedQuery metadataPrepared = createImageMetadataQuery(begin, end, withoutLocation);
        int count = metadataPrepared.countEntities(FetchOptions.Builder.withLimit(1));
        return count > 0;
    }

    private PreparedQuery createImageMetadataQuery(Date begin, Date end, boolean withoutLocation)
    {
        Query metadataQuery = new Query(MetadataKind);
        List<Filter> filters = new ArrayList<>();
        filters.add(new FilterPredicate("DateTimeOriginal", Query.FilterOperator.GREATER_THAN_OR_EQUAL, begin));
        filters.add(new FilterPredicate("DateTimeOriginal", Query.FilterOperator.LESS_THAN_OR_EQUAL, end));
        if (withoutLocation)
        {
            filters.add(new FilterPredicate(LocationProperty, Query.FilterOperator.EQUAL, null));
        }
        CompositeFilter compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filters);
        metadataQuery.setFilter(compositeFilter);
        metadataQuery.addSort("DateTimeOriginal");
        return prepare(metadataQuery);
    }

    public Iterable<Entity> fetchImageLocations()
    {
        Query imageLocationQuery = new Query(MetadataKind);
        imageLocationQuery.addProjection(new PropertyProjection(LocationProperty, GeoPt.class));
        PreparedQuery imageLocationPrepared = prepare(imageLocationQuery);
        return imageLocationPrepared.asIterable();
    }

    public Iterable<Entity> fetchBlogLocations()
    {
        Query blogLocationQuery = new Query(BlogKind);
        blogLocationQuery.addProjection(new PropertyProjection(LocationProperty, GeoPt.class));
        PreparedQuery blogLocationPrepared = prepare(blogLocationQuery);
        return blogLocationPrepared.asIterable();
    }

    public static GeoPt center(Collection<GeoPt> list)
    {
        float lat = 0;
        float lon = 0;
        for (GeoPt pt : list)
        {
            lat += pt.getLatitude();
            lon += pt.getLongitude();
        }
        return new GeoPt(lat / list.size(), lon / list.size());
    }

    private String getCoordinatesString(Collection<GeoPt> coordinates)
    {
        StringBuilder sb = new StringBuilder();
        for (GeoPt location : coordinates)
        {
            if (sb.length() > 0)
            {
                sb.append(' ');
            }
            sb.append(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
        }
        return sb.toString();
    }

    public void addPlacemark(final Date time, final GeoPt geoPt, final String title, final String description)
    {
        Settings settings = getSettings();
        RunInNamespace<Entity> rin = new RunInNamespace()
        {
            @Override
            protected Entity run()
            {
                Entity placemark = new Entity(PlacemarkKind, time.getTime(), getRootKey());
                MaidenheadLocator2.setLocation(placemark, geoPt, MaidenheadLocator2.LocatorLevel.Field);
                placemark.setProperty(LocationProperty, geoPt);
                placemark.setProperty(TimestampProperty, time);
                placemark.setUnindexedProperty(TitleProperty, title);
                placemark.setUnindexedProperty(DescriptionProperty, description);
                put(placemark);
                return placemark;
            }
        };
        rin.doIt(null, settings.isCommonPlacemarks());
        clearPlacemarkCache();
    }

    public void addPlacemark(final Entity placemark)
    {
        Settings settings = getSettings();
        RunInNamespace<Entity> rin = new RunInNamespace()
        {
            @Override
            protected Entity run()
            {
                put(placemark);
                return placemark;
            }
        };
        rin.doIt(null, settings.isCommonPlacemarks());
        clearPlacemarkCache();
    }

    public Entity createPlacemark()
    {
        Settings settings = getSettings();
        RunInNamespace<Entity> rin = new RunInNamespace()
        {
            @Override
            protected Entity run()
            {
                return new Entity(PlacemarkKind, getRootKey());
            }
        };
        return rin.doIt(null, settings.isCommonPlacemarks());
    }

    private void clearPlacemarkCache()
    {
        RunForAllNamespaces rfan = new RunForAllNamespaces()
        {
            @Override
            protected void run(String namespace)
            {
                DS ds = DS.get();
                ds.cache.delete("/lastPosition");
            }
        };
        rfan.start(this);
    }
    private static final long DayInMillis = 24 * 60 * 60 * 1000;

    public Entity getBlogEntity(String messageId, String subject)
    {
        Date yesterday = new Date(System.currentTimeMillis() - DayInMillis);
        Query query = new Query(BlogKind);
        query.setFilter(new FilterPredicate(TimestampProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, yesterday));
        PreparedQuery prepared = prepare(query);
        for (Entity blog : prepared.asIterable())
        {
            String sub = (String) blog.getProperty(SubjectProperty);
            if (subject.equals(sub))
            {
                return blog;
            }
        }
        return getBlogFromMessageId(messageId);
    }

    public Entity getEntityForKey(String keyString)
    {
        Key key = KeyFactory.stringToKey(keyString);
        try
        {
            return datastore.get(key);
        }
        catch (EntityNotFoundException ex)
        {
            return null;
        }
    }

    @Override
    protected void afterDeleted(Key key)
    {
        switch (key.getKind())
        {
            case BlogKind:
                Searches.deleteBlog(key);
                break;
        }
    }

    @Override
    protected void afterPut(Entity entity)
    {
        switch (entity.getKind())
        {
            case BlogKind:
                Boolean publish = (Boolean) entity.getProperty(PublishProperty);
                if (publish != null && publish)
                {
                    Searches.saveBlog(entity);
                }
                else
                {
                    Searches.deleteBlog(entity.getKey());
                }
                break;
        }
    }

    public void populateTrack(PrintWriter pw)
    {
        Query q1 = new Query(TrackKind);
        PreparedQuery p1 = prepare(q1);
        for (Entity track : p1.asIterable())
        {
            if (!BoundingBox.isPopulated(track) || !TimeSpan.isPopulated(track))
            {
                BoundingBox bb1 = new BoundingBox();
                TimeSpan ts1 = new TimeSpan();
                Query q2 = new Query(TrackSeqKind);
                q2.setAncestor(track.getKey());
                PreparedQuery p2 = prepare(q2);
                for (Entity trackSeq : p2.asIterable())
                {
                    BoundingBox bb2 = BoundingBox.getInstance(trackSeq);
                    bb1.add(bb2);
                    TimeSpan ts2 = new TimeSpan(trackSeq);
                    ts1.add(ts2);
                }
                bb1.populate(track);
                bb1.clear();
                ts1.populate(track);
                ts1.clear();
                put(track);
                pw.println("fixed " + track);
            }
        }
        pw.println("ready");
    }

    public void connectPictures(PrintWriter pw)
    {
        pw.println("started locating pictures");
        Iterable<Entity> trackIterable = fetchTracks();
        for (Entity track : trackIterable)
        {
            BoundingBox bb1 = BoundingBox.getInstance(track);
            TimeSpan ts1 = new TimeSpan(track);
            if (hasImageMetadata(ts1.getBegin(), ts1.getEnd(), true))
            {
                Iterable<Entity> trackSeqIterable = fetchTrackSeqs(track.getKey());
                for (Entity trackSeq : trackSeqIterable)
                {
                    BoundingBox bb2 = BoundingBox.getInstance(trackSeq);
                    TimeSpan ts2 = new TimeSpan(trackSeq);
                    if (hasImageMetadata(ts2.getBegin(), ts2.getEnd(), true))
                    {
                        Iterable<Entity> imageIterable = fetchImageMetadata(ts2.getBegin(), ts2.getEnd(), true);
                        Iterator<Entity> ImageIterator = imageIterable.iterator();
                        Iterable<Entity> trackPointIterable = fetchTrackPoints(trackSeq.getKey());
                        Iterator<Entity> trackPointIterator = trackPointIterable.iterator();
                        if (!trackPointIterator.hasNext())
                        {
                            continue;   // shouldn't happen
                        }
                        if (!ImageIterator.hasNext())
                        {
                            continue;   // shouldn't happen
                        }
                        Entity image = ImageIterator.next();
                        Date imageDate = (Date) image.getProperty("DateTimeOriginal");
                        long imageTime = imageDate.getTime();
                        Entity prevTrackPoint = trackPointIterator.next();
                        long prevTime = prevTrackPoint.getKey().getId();
                        while (trackPointIterator != null && trackPointIterator.hasNext())
                        {
                            Entity nextTrackPoint = trackPointIterator.next();
                            long nextTime = nextTrackPoint.getKey().getId();
                            while (imageTime <= nextTime)
                            {
                                GeoPt prevLocation = (GeoPt) prevTrackPoint.getProperty(LocationProperty);
                                double prevLatitude = prevLocation.getLatitude();
                                double prevLongitude = prevLocation.getLongitude();
                                GeoPt nextLocation = (GeoPt) nextTrackPoint.getProperty(LocationProperty);
                                double nextLatitude = nextLocation.getLatitude();
                                double nextLongitude = nextLocation.getLongitude();
                                double dLatitude = nextLatitude - prevLatitude;
                                double dLongitude = nextLongitude - prevLongitude;
                                double dTrackPointTime = nextTime - prevTime;
                                double dImageTime = imageTime - prevTime;
                                double coeff = dImageTime / dTrackPointTime;
                                float imageLatitude = (float) (prevLatitude + coeff * dLatitude);
                                float imageLongitude = (float) (prevLongitude + coeff * dLongitude);
                                GeoPt imageLocation = new GeoPt(imageLatitude, imageLongitude);
                                image.setProperty(LocationProperty, imageLocation);
                                put(image);
                                pw.println("Located: " + image);
                                if (ImageIterator.hasNext())
                                {
                                    image = ImageIterator.next();
                                    imageDate = (Date) image.getProperty("DateTimeOriginal");
                                    imageTime = imageDate.getTime();
                                }
                                else
                                {
                                    trackPointIterator = null;
                                    break;
                                }
                            }
                            prevTrackPoint = nextTrackPoint;
                            prevTime = nextTime;
                        }
                    }
                }
            }
        }
        pw.println("ended locating pictures");
    }

    public void writeMapInit(CacheWriter cw, int height, int width)
    {
        Settings settings = getSettings();
        JSONObject json = new JSONObject();
        Iterable<Entity> lastPlacemarksIterable = fetchLastPlacemarks(settings);
        Iterator<Entity> iterator = lastPlacemarksIterable.iterator();
        BoundingBox bb = new BoundingBox();
        if (iterator.hasNext())
        {
            Entity pm = iterator.next();
            GeoPt location = (GeoPt) pm.getProperty(LocationProperty);
            bb.add(location);
            Date timestamp = (Date) pm.getProperty(TimestampProperty);
            json.put(LatitudeParameter, location.getLatitude());
            json.put(LongitudeParameter, location.getLongitude());
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601Format);
            json.put(TimestampParameter, sdf.format(timestamp));
            String description = (String) pm.getProperty(DescriptionProperty);
            SpotType st = SpotType.getSpotType(description);
            GeoPt location2 = null;
            while (iterator.hasNext() && SpotType.Ok != st)
            {
                pm = iterator.next();
                location2 = (GeoPt) pm.getProperty(LocationProperty);
                bb.add(location2);
                description = (String) pm.getProperty(DescriptionProperty);
                st = SpotType.getSpotType(description);
            }
            int iz = settings.getZoom();
            json.put(ZoomParameter, iz);
            System.err.println("zoom="+iz);
            if (location2 != null)
            {
                double bbWidth = bb.getWidth();
                double bbHeight = bb.getHeight();
                for (int zoom = iz; zoom >= 0; zoom--)
                {
                    double c = Math.pow(2, zoom);
                    int neededWidth = (int) (c * bbWidth);
                    int neededHeight = (int) (c * bbHeight);
                    if (neededWidth < width && neededHeight < height)
                    {
                        json.put(ZoomParameter, zoom);
                        bb.populate(json);
                        break;
                    }
                }
            }
        }
        json.write(cw);
        cw.cache();
    }

    private GeoData getGeoData()
    {
        GeoData geoData = (GeoData) getFromCache("GeoData");
        if (geoData == null)
        {
            geoData = new GeoData(this);
            putToCache("GeoData", geoData);
        }
        return geoData;
    }

    public void writeRegionKeys(CacheWriter cw, BoundingBox bb)
    {
        GeoData geoData = getGeoData();
        geoData.writeRegionKeys(cw, bb);
    }

    public void writeFeature(CacheWriter cw, Key key)
    {
        Entity entity;
        try
        {
            entity = get(key);
        }
        catch (EntityNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        Feature feature;
        Settings settings = getSettings();
        switch (key.getKind())
        {
            case TrackSeqKind:
            {
                Date begin = (Date) entity.getProperty(BeginProperty);
                LineString lineString = new LineString();
                for (Entity trackPoint : fetchTrackPoints(key))
                {
                    GeoPt location = (GeoPt) trackPoint.getProperty(LocationProperty);
                    lineString.add(location);
                }
                feature = new Feature(lineString);
                feature.setId(KeyFactory.keyToString(key));
                feature.setProperty("color", settings.getTrackCss3Color());
                feature.setProperty("opacity", getAlpha(begin));
                feature.write(cw);
            }
            break;
            case BlogKind:
            case MetadataKind:
            {
                GeoPt location = (GeoPt) entity.getProperty(LocationProperty);
                Point point = new Point(location);
                feature = new Feature(point);
                feature.setId(KeyFactory.keyToString(key));
                feature.setProperty("icon", settings.getIcon(entity));
                feature.setProperty("pmm", settings.getPpm(entity));
                feature.write(cw);
            }
            break;
            case PlacemarkKind:
            {
                GeoData geoData = getGeoData();
                Date timestamp = (Date) entity.getProperty(TimestampProperty);
                LineString lineString = new LineString();
                Collection<GeoPt> placemarkPoints = geoData.getPlacemarkPoints(key);
                if (placemarkPoints != null)
                {
                    lineString.add(placemarkPoints);
                }
                feature = new Feature(lineString);
                feature.setId(KeyFactory.keyToString(key));
                feature.setProperty("color", settings.getTrackCss3Color());
                feature.setProperty("opacity", getAlpha(timestamp));
                feature.write(cw);
            }
            break;
            default:
                System.err.println("Unknown entity " + entity);
        }
        cw.cache();
    }

    public int getAlpha(Date begin)
    {
        if (begin == null)
        {
            return 255;
        }
        Settings settings = getSettings();
        long x0 = getTrackSeqsBegin().getTime();
        long xn = System.currentTimeMillis();
        int minOpaque = settings.getMinOpaque();
        double span = 255 - minOpaque;
        double c = span / Math.sqrt(xn - x0);
        int age = (int) Math.round(c * Math.sqrt(xn - begin.getTime()));
        return 255 - age;
    }

    /**
     * Return distance in degrees between l1 and 2l
     *
     * @param l1
     * @param l2
     * @return
     */
    public static double getDegreesDistance(GeoPt l1, GeoPt l2)
    {
        double departure = Math.cos(Math.toRadians((l1.getLatitude() + l2.getLatitude()) / 2));
        return Math.hypot(l1.getLatitude() - l2.getLatitude(), departure * (l1.getLongitude() - l2.getLongitude()));
    }

    public void addResource(final String id, final String type, final String text) throws IOException
    {
        RunInNamespace rin = new RunInNamespace()
        {
            @Override
            protected Object run()
            {
                try
                {
                    Updater updater = new Updater()
                    {
                        @Override
                        protected Object update() throws IOException
                        {
                            Key key = createResourceKey();
                            Entity entity;
                            try
                            {
                                entity = get(key);
                            }
                            catch (EntityNotFoundException ex)
                            {
                                entity = new Entity(key);
                            }
                            if (entity.hasProperty(id))
                            {
                                System.err.println(id+" exists");
                            }
                            else
                            {
                                switch (type)
                                {
                                    case "string":
                                        entity.setUnindexedProperty(id, text);
                                        break;
                                    case "text":
                                        entity.setUnindexedProperty(id, new Text(text));
                                        break;
                                    default:
                                        System.err.println("unknown type "+type);
                                }
                                put(entity);
                            }
                            return null;
                        }
                    };
                    updater.start();
                }
                catch (IOException ex)
                {
                    System.err.println(ex.getMessage());
                }
                return null;
            }
        };
        rin.doIt(null);
        putToCache(Resources, null);
    }

    private String findSha1(String text)
    {
        String ss = Sha1Parameter+"=";
        int i1 = text.indexOf(ss);
        if (i1 != -1)
        {
            int i2 = text.indexOf('"', i1);
            if (i2 != -1)
            {
                return text.substring(i1+ss.length(), i2);
            }
        }
        return null;
    }

    public interface Caching
    {

        Caching setMaxAge(int maxAge);

        Caching setContentType(String contentType);

        Caching setCharset(String charset);

        Caching setETag(String eTag);

        Caching setPrivate(boolean isPrivate);

        void cache();
    }

    public abstract class CachingImpl implements Caching
    {

        private final HttpServletResponse response;
        private String eTag;
        private final String cacheKey;
        private boolean isPrivate;
        private int maxAge;

        CachingImpl(HttpServletRequest request, HttpServletResponse response)
        {
            this.response = response;
            this.cacheKey = getCacheKey(request);
            setContentType("text/html");
            setCharset("utf-8");
        }

        @Override
        public Caching setMaxAge(int maxAge)
        {
            this.maxAge = maxAge;
            return this;
        }

        @Override
        public Caching setContentType(String contentType)
        {
            response.setContentType(contentType);
            return this;
        }

        @Override
        public Caching setCharset(String charset)
        {
            response.setCharacterEncoding(charset);
            return this;
        }

        @Override
        public Caching setETag(String eTag)
        {
            this.eTag = eTag;
            response.setHeader("ETag", eTag);
            return this;
        }

        @Override
        public Caching setPrivate(boolean isPrivate)
        {
            this.isPrivate = isPrivate;
            cacheControl();
            return this;
        }

        private void cacheControl()
        {
            if (isPrivate)
            {
                response.setHeader("Cache-Control", "private, max-age=" + maxAge + ", no-cache");
            }
            else
            {
                if (maxAge > 0)
                {
                    response.setHeader("Cache-Control", "public, max-age=" + maxAge);
                }
                else
                {
                    response.setHeader("Cache-Control", "public");
                }
            }
        }

    }

    public class CacheWriter extends FilterWriter implements Caching
    {

        private CachingImpl caching;
        private final StringWriter stringWriter = new StringWriter();
        private boolean cached;

        private CacheWriter(HttpServletRequest request, HttpServletResponse response) throws IOException
        {
            super(response.getWriter());
            this.caching = new CachingImpl(request, response)
            {
                @Override
                public void cache()
                {
                    if (!caching.isPrivate)
                    {
                        String content = stringWriter.toString();
                        CachedContent cachedContent;
                        try
                        {
                            cachedContent = new CachedContent(content, caching.response.getContentType(), caching.response.getCharacterEncoding(), caching.eTag, caching.isPrivate);
                        }
                        catch (UnsupportedEncodingException ex)
                        {
                            throw new IllegalArgumentException(ex);
                        }
                        cache.put(caching.cacheKey, cachedContent);
                        System.err.println("caching as " + caching.cacheKey);
                    }
                    cached = true;
                }
            };
        }

        @Override
        public Caching setMaxAge(int maxAge)
        {
            return caching.setMaxAge(maxAge);
        }

        @Override
        public Caching setContentType(String contentType)
        {
            return caching.setContentType(contentType);
        }

        @Override
        public Caching setCharset(String charset)
        {
            return caching.setCharset(charset);
        }

        @Override
        public Caching setETag(String eTag)
        {
            return caching.setETag(eTag);
        }

        @Override
        public Caching setPrivate(boolean isPrivate)
        {
            return caching.setPrivate(isPrivate);
        }

        @Override
        public void cache()
        {
            caching.cache();
        }

        @Override
        public void write(String str, int off, int len) throws IOException
        {
            super.write(str, off, len);
            stringWriter.write(str, off, len);
        }

        @Override
        public void write(int c) throws IOException
        {
            super.write(c);
            stringWriter.write(c);
        }

        @Override
        public void write(char[] buf, int off, int len) throws IOException
        {
            out.write(buf, off, len);
            stringWriter.write(buf, off, len);
        }

        @Override
        public void close() throws IOException
        {
            if (!cached)
            {
                System.err.println("closing without caching");
            }
        }
    }

    public class CacheOutputStream extends FilterOutputStream implements Caching
    {

        private final CachingImpl caching;
        private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        private boolean cached;

        private CacheOutputStream(HttpServletRequest request, HttpServletResponse response) throws IOException
        {
            super(response.getOutputStream());
            caching = new CachingImpl(request, response)
            {
                @Override
                public void cache()
                {
                    if (!caching.isPrivate)
                    {
                        byte[] content = byteStream.toByteArray();
                        CachedContent cachedContent;
                        cachedContent = new CachedContent(content, caching.response.getContentType(), caching.response.getCharacterEncoding(), caching.eTag, caching.isPrivate);
                        cache.put(caching.cacheKey, cachedContent);
                        System.err.println("caching as " + caching.cacheKey);
                    }
                    cached = true;
                }
            };
        }

        @Override
        public Caching setMaxAge(int maxAge)
        {
            return caching.setMaxAge(maxAge);
        }

        @Override
        public Caching setContentType(String contentType)
        {
            return caching.setContentType(contentType);
        }

        @Override
        public Caching setCharset(String charset)
        {
            return caching.setCharset(charset);
        }

        @Override
        public Caching setETag(String eTag)
        {
            return caching.setETag(eTag);
        }

        @Override
        public Caching setPrivate(boolean isPrivate)
        {
            return caching.setPrivate(isPrivate);
        }

        @Override
        public void cache()
        {
            caching.cache();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            super.write(b, off, len);
            byteStream.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException
        {
            super.write(b);
            byteStream.write(b);
        }

        @Override
        public void write(int b) throws IOException
        {
            super.write(b);
            byteStream.write(b);
        }

        @Override
        public void close() throws IOException
        {
            if (!cached)
            {
                System.err.println("closing without caching");
            }
            super.close();
        }
    }

    private static class NullOutputStream extends OutputStream
    {

        @Override
        public void write(int b) throws IOException
        {
        }
    }
}
