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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.apphosting.api.ApiProxy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.mailblog.exif.ExifParser;

/**
 *
 * @author Timo Vesalainen
 */
public class MailHandlerServlet extends HttpServlet implements BlogConstants
{
    private static final String CRLF = "\r\n";
    private MessageDigest sha1;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            sha1 = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new ServletException(ex);
        }
    }

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log("namespace="+NamespaceManager.get());
        String removeKey = request.getParameter(RemoveParameter);
        if (removeKey != null)
        {
            remove(removeKey, response);
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String removeKey = request.getParameter(RemoveParameter);
        if (removeKey != null)
        {
            remove(removeKey, response);
        }
        else
        {
            try
            {
                BlogAuthor blogAuthor = setNamespace(request, response);
                if (blogAuthor == null)
                {
                    return;
                }
                handleMail(request, response, blogAuthor);
            }
            catch (EntityNotFoundException ex)
            {
                log(ex.getMessage(), ex);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }
    private BlogAuthor setNamespace(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        log("pathInfo="+pathInfo);
        if (pathInfo == null)
        {
            log("pathInfo=null");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        int idx = pathInfo.indexOf('@');
        if (idx == -1)
        {
            log("pathInfo doesn't contain @");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String namespace = pathInfo.substring(1, idx);
        NamespaceManager.set(namespace);
        log("namespace set to "+namespace);
        String address = pathInfo.substring(idx+1);
        return new BlogAuthor(namespace, address);
    }
    private void handleMail(HttpServletRequest request, HttpServletResponse response, BlogAuthor blogAuthor) throws IOException, ServletException, EntityNotFoundException
    {
        DB db = DB.DB;
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        Transaction tr = db.beginTransaction();
        try
        {
            log("namespace is "+NamespaceManager.get());
            MimeMessage message = new MimeMessage(session, request.getInputStream());
            String messageID = message.getMessageID();
            if (messageID == null)
            {
                log("messageID missing");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            InternetAddress sender = (InternetAddress) message.getSender();
            log("sender="+sender);
            if (sender == null)
            {
                log("Sender missing");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Email senderEmail = new Email(sender.getAddress());
            Settings settings = db.getSettingsFor(senderEmail);
            if (settings == null)
            {
                log(senderEmail.getEmail()+" not allowed to send blogs");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String[] ripperDate = message.getHeader(BlogRipper+"Date");
            boolean ripping = ripperDate != null && ripperDate.length > 0;
            Multipart multipart = (Multipart) message.getContent();
            List<BodyPart> bodyPartList = findParts(multipart);
            Entity blog = getBlog(message);
            if (!blog.hasProperty(HtmlProperty))
            {
                String htmlBody = null;
                for (BodyPart bodyPart : bodyPartList)
                {
                    String contentType = bodyPart.getContentType();
                    Object content = bodyPart.getContent();
                    if (contentType.startsWith("text/html"))
                    {
                        htmlBody = (String) content;
                    }
                    else
                    {
                        if (contentType.startsWith("text/plain"))
                        {
                            if (htmlBody == null)
                            {
                                htmlBody = (String) content;
                            }
                        }
                    }
                }
                blog.setUnindexedProperty(HtmlProperty, new Text(htmlBody));
                if (!ripping)
                {
                    sendMail(request, blogAuthor, blog);
                }
            }
            List<Future<HTTPResponse>> futureList = new ArrayList<Future<HTTPResponse>>();
            for (BodyPart bodyPart : bodyPartList)
            {
                Collection<Future<HTTPResponse>> futures = handleBodyPart(request, blog, bodyPart, settings);
                if (futures != null)
                {
                    futureList.addAll(futures);
                }
            }
            db.putAndCache(blog);
            tr.commit();
            long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
            log("remainingMillis="+remainingMillis);
            for (Future<HTTPResponse> res : futureList)
            {
                try
                {
                    HTTPResponse hr = res.get();
                    log("code="+hr.getResponseCode());
                    if (hr.getResponseCode() != HttpServletResponse.SC_OK)
                    {
                        throw new ServletException("blob upload failed code="+hr.getResponseCode());
                    }
                }
                catch (InterruptedException ex)
                {
                    throw new IOException(ex);
                }
                catch (ExecutionException ex)
                {
                    throw new IOException(ex);
                }
            }
        }
        catch (MessagingException ex)
        {
            throw new IOException(ex);
        }
        finally
        {
            if (tr.isActive())
            {
                log("transaction rolled back");
                tr.rollback();
                response.sendError(HttpServletResponse.SC_CONFLICT);
            }
        }
    }

    private Entity getBlog(MimeMessage message) throws MessagingException, IOException
    {
        DB db = DB.DB;
        Enumeration allHeaderLines = message.getAllHeaderLines();
        while (allHeaderLines.hasMoreElements())
        {
            log(allHeaderLines.nextElement().toString());
        }
        String messageID = (String) getHeader(message, "Message-ID");
        log("messageID="+messageID);
        Entity blog = db.getBlogFromMessageId(messageID);
        setProperty(message, SubjectProperty, blog, false);
        setProperty(message, SenderProperty, blog, false);
        setProperty(message, SentDateProperty, blog, true);
        blog.setProperty(TimestampProperty, new Date());
        return blog;
    }
    private void setProperty(MimeMessage message, String name, Entity blog, boolean indexed) throws MessagingException, IOException
    {
        Object header = getHeader(message, name);
        if (header != null)
        {
            if (indexed)
            {
                log("indexed="+name);
                blog.setProperty(name, header);
            }
            else
            {
                log("unindexed="+name);
                blog.setUnindexedProperty(name, header);
            }
        }
    }
    private Object getHeader(MimeMessage message, String name) throws MessagingException, IOException
    {
        String[] header = message.getHeader(BlogRipper+name);
        if (header == null || header.length == 0)
        {
            header = message.getHeader(name);
        }
        if (header == null || header.length == 0)
        {
            return null;
        }
        if ("Date".equals(name))
        {
            SimpleDateFormat df = new SimpleDateFormat(RFC1123Format, Locale.US);
            try
            {
                return df.parse(header[0]);
            }
            catch (ParseException ex)
            {
                throw new IOException(ex);
            }
        }
        if ("Sender".equals(name))
        {
            String email = header[0];
            int i1 = email.indexOf('<');
            int i2 = email.indexOf('>');
            if (i1 != -1 && i2 != -1)
            {
                email = email.substring(i1+1, i2);
            }
            return new Email(email);
        }
        return MimeUtility.decodeText(header[0]);
    }
    private Collection<Future<HTTPResponse>> handleBodyPart(HttpServletRequest request, Entity blog, BodyPart bodyPart, Settings settings) throws MessagingException, IOException
    {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        DB db = DB.DB;
        Collection<Future<HTTPResponse>> futures = new ArrayList<Future<HTTPResponse>>();
        String contentType = bodyPart.getContentType();
        log(contentType);
        Object content = bodyPart.getContent();
        if (content instanceof InputStream)
        {
            String filename = bodyPart.getFileName();
            String[] cids = bodyPart.getHeader("Content-ID");
            if (cids == null || cids.length == 0)
            {
                log("attachment filename="+filename+" doesn't have a Content-ID");
                return null;
            }
            String cid = cids[0];
            byte[] bytes = getBytes(bodyPart);
            byte[] digest = sha1.digest(bytes);
            String digestString = Hex.convert(digest);
            log(digestString);
            Entity metadata = db.getMetadata(digestString);
            log(metadata.toString());
            if (metadata.getProperties().isEmpty())
            {
                metadata.setUnindexedProperty(FilenameProperty, filename);
                metadata.setUnindexedProperty(ContentTypeProperty, contentType);
                metadata.setUnindexedProperty(TimestampProperty, new Date());
                try
                {
                    if (contentType.startsWith("image/jpeg"))
                    {
                        ExifParser exif = new ExifParser(bytes);
                        Date timestamp = exif.getTimestamp();
                        if (timestamp != null)
                        {
                            exif.populate(metadata);
                        }
                    }
                }
                catch (Exception ex)
                {
                }
                db.putAndCache(metadata);
            }
            replaceBlogRef(blog, cid, digestString);
            db.blogsChanged();
            if (contentType.startsWith("image/"))
            {
                Image image = ImagesServiceFactory.makeImage(bytes);
                if (settings.isFixPic())
                {
                    Transform makeImFeelingLucky = ImagesServiceFactory.makeImFeelingLucky();
                    image = imagesService.applyTransform(makeImFeelingLucky, image);
                }
                if (
                        image.getHeight() > settings.getPicMaxHeight() ||
                        image.getWidth() > settings.getPicMaxWidth()
                        )
                {
                    log("shrinking");
                    Transform makeResize = ImagesServiceFactory.makeResize(settings.getPicMaxHeight(), settings.getPicMaxWidth());
                    Image shrinken = imagesService.applyTransform(makeResize, image);
                    log("upload "+cid);
                    Future<HTTPResponse> res = postBlobs(filename, contentType, digestString, shrinken.getImageData(), WebSizeProperty, request);
                    futures.add(res);
                }
            }
            log("upload "+cid);
            Future<HTTPResponse> res = postBlobs(filename, contentType, digestString, bytes, OriginalSizeProperty, request);
            futures.add(res);
        }
        if (contentType.startsWith("application/X-jsr179-location-nmea"))
        {
            log("NMEA not yet supported");
        }        
        return futures;
    }

    private Future<HTTPResponse> postBlobs(String filename, String contentType, String sha1, byte[] data, String metadataSize, HttpServletRequest request) throws MessagingException, IOException
    {
        try
        {
            URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
            BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
            URI reqUri = new URI(request.getScheme(), request.getServerName(), "", "");
            URI uri = reqUri.resolve("/blob?"+NamespaceParameter+"="+NamespaceManager.get()+"&"+SizeParameter+"="+metadataSize);
            URL uploadUrl = new URL(blobstore.createUploadUrl(uri.toASCIIString()));
            log("post blob to "+uploadUrl);
            HTTPRequest httpRequest = new HTTPRequest(uploadUrl, HTTPMethod.POST, FetchOptions.Builder.withDeadline(60));
            String uid = UUID.randomUUID().toString();
            httpRequest.addHeader(new HTTPHeader("Content-Type", "multipart/form-data; boundary="+uid));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ps.append("--"+uid);
            ps.append(CRLF);
            ps.append("Content-Disposition: form-data; name=\""+sha1+"\"; filename=\""+filename+"\"");
            ps.append(CRLF);
            ps.append("Content-Type: "+contentType);
            ps.append(CRLF);
            ps.append("Content-Transfer-Encoding: binary");
            ps.append(CRLF);
            ps.append(CRLF);
            ps.write(data);
            ps.append(CRLF);
            ps.append("--"+uid+"--");
            ps.append(CRLF);
            ps.close();
            log("sending blog size="+baos.size());
            httpRequest.setPayload(baos.toByteArray());
            return fetchService.fetchAsync(httpRequest);
        }
        catch (URISyntaxException ex)
        {
            throw new IOException(ex);
        }
    }

    private List<BodyPart> findParts(Multipart multipart) throws MessagingException, IOException
    {
        List<BodyPart> list = new ArrayList<BodyPart>();
        findParts(list, multipart);
        return list;
    }
    private void findParts(List<BodyPart> list, Multipart multipart) throws MessagingException, IOException
    {
        for (int ii=0;ii<multipart.getCount();ii++)
        {
            BodyPart bodyPart = multipart.getBodyPart(ii);
            list.add(bodyPart);
            Object content = bodyPart.getContent();
            if (content instanceof Multipart)
            {
                Multipart mp = (Multipart) content;
                findParts(list, mp);
            }
        }
    }
    private void remove(String encoded, HttpServletResponse response) throws IOException
    {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        DB db = DB.DB;
        Key key = KeyFactory.stringToKey(encoded);
        Transaction tr = db.beginTransaction();
        try
        {
            Entity blog = db.get(key);
            db.deleteBlog(key);
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.println("Deleted blog: "+blog.getProperty("Subject"));
            writer.close();
            tr.commit();
        }
        catch (EntityNotFoundException ex)
        {
            throw new IOException(ex);
        }
        finally
        {
            if (tr.isActive())
            {
                tr.rollback();
            }
        }
    }

        
    private byte[] getBytes(BodyPart bodyPart) throws IOException, MessagingException
    {
        Object content = bodyPart.getContent();
        InputStream is = (InputStream) content;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int rc = is.read(buffer);
        while (rc != -1)
        {
            baos.write(buffer, 0, rc);
            rc = is.read(buffer);
        }
        return baos.toByteArray();
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }

    private void sendMail(HttpServletRequest request, BlogAuthor blogAuthor, Entity blog) throws IOException
    {
        try
        {
            log(blog.toString());
            MailService mailService = MailServiceFactory.getMailService();
            Message reply = new Message();
            reply.setSender(blogAuthor.toString());
            Email sender = (Email) blog.getProperty(SenderProperty);
            reply.setTo(sender.getEmail());
            String subject = (String) blog.getProperty(SubjectProperty);
            reply.setSubject("Blog: "+subject+" received");
            StringBuilder blogDeleteHref = new StringBuilder();
            URI reqUri = new URI(request.getScheme(), NamespaceManager.get(), "", "");
            URI uri = reqUri.resolve("/blog?remove="+KeyFactory.keyToString(blog.getKey()));
            blogDeleteHref.append(uri.toASCIIString());
            reply.setHtmlBody("<a href=\""+blogDeleteHref+"\">Delete Blog</a>");
            mailService.send(reply);
        }
        catch (URISyntaxException ex)
        {
            throw new IOException(ex);
        }
    }

    private void replaceBlogRef(Entity blog, String cid, String sha1)
    {
        DB db = DB.DB;
        Collection<Key> attachments = (Collection<Key>) blog.getProperty(AttachmentsProperty);
        if (attachments == null)
        {
            attachments = new HashSet<Key>();
            blog.setUnindexedProperty(AttachmentsProperty, attachments);
        }
        attachments.add(db.getMetadataKey(sha1));
        if (cid.startsWith("<") && cid.endsWith(">"))
        {
            cid = cid.substring(1, cid.length()-1);
        }
        Text text = (Text) blog.getProperty(HtmlProperty);
        String body = text.getValue();
        body = body.replace("cid:"+cid, "/blob?"+Sha1Parameter+"="+sha1);
        blog.setUnindexedProperty(HtmlProperty, new Text(body));
    }

    private class BlogAuthor
    {
        private String blogNamespace;
        private String blogAddress;

        public BlogAuthor(String blogNamespace, String blogAddress)
        {
            this.blogNamespace = blogNamespace;
            this.blogAddress = blogAddress;
        }

        public String getBlogNamespace()
        {
            return blogNamespace;
        }

        public String getBlogAddress()
        {
            return blogAddress;
        }

        @Override
        public String toString()
        {
            return blogNamespace + "@" + blogAddress;
        }

        
    }
}
