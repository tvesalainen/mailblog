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
import com.google.appengine.api.blobstore.BlobKey;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private DB db;
    private BlobstoreService blobstore;
    private URLFetchService fetchService;
    private Session session;
    private MailService mailService;
    private ImagesService imagesService;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        db = new DB();
        Properties props = new Properties();
        session = Session.getDefaultInstance(props, null);
        mailService = MailServiceFactory.getMailService();
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
        fetchService = URLFetchServiceFactory.getURLFetchService();
        imagesService = ImagesServiceFactory.getImagesService();
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
        String blobKeyString = request.getParameter("addBlobs");
        if (blobKeyString != null)
        {
            addBlobs(request, blobKeyString, request.getParameter("metadata"));
        }
        else
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
                    if (!setNamespace(request, response))
                    {
                        return;
                    }
                    handleMail(request);
                }
                catch (EntityNotFoundException ex)
                {
                    log(ex.getMessage(), ex);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        }
    }
    private boolean setNamespace(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        log("pathInfo="+pathInfo);
        if (pathInfo == null)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        int idx = pathInfo.indexOf('@');
        if (idx == -1)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        String namespace = pathInfo.substring(1, idx);
        NamespaceManager.set(namespace);
        log("namespace set to "+namespace);
        return true;
    }
    private void handleMail(HttpServletRequest request) throws IOException, ServletException, EntityNotFoundException
    {
        Transaction tr = db.beginTransaction();
        try
        {
            MimeMessage message = new MimeMessage(session, request.getInputStream());
            String messageID = message.getMessageID();
            List<BodyPart> list = new ArrayList<BodyPart>();
            Entity blog = db.getBlogFromMessageId(messageID);
            InternetAddress sender = (InternetAddress) message.getSender();
            Email senderEmail = new Email(sender.getAddress());
            Settings settings = db.getSettingsFor(senderEmail);
            String blogKeyString = KeyFactory.keyToString(blog.getKey());
            if (!blog.hasProperty(TimestampProperty))
            {
                blog.setProperty(TimestampProperty, new Date());
                blog.setProperty(SenderProperty, senderEmail);
                String subject = message.getSubject();
                blog.setProperty(SubjectProperty, subject);
                Date sentDate = message.getSentDate();
                blog.setProperty(SentDateProperty, sentDate);
                Multipart multipart = (Multipart) message.getContent();
                findParts(list, multipart);
                String htmlBody = null;
                for (BodyPart bodyPart : list)
                {
                    String contentType = bodyPart.getContentType();
                    String fileName = bodyPart.getFileName();
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
                Key put = db.put(blog);
                tr.commit();
                Message reply = new Message();
                String uri = request.getRequestURI();
                int idx = uri.lastIndexOf('/');
                uri = uri.substring(idx+1);
                reply.setSender(uri);
                reply.setTo(sender.getAddress());
                reply.setSubject("Blog: "+subject+" received");
                StringBuffer requestURL = request.getRequestURL();
                requestURL.append("?remove="+blogKeyString);
                reply.setHtmlBody("<a href=\""+requestURL+"\">Delete Blog</a>");
                mailService.send(reply);
            }
            Collection<String> cidSet = (Collection<String>) blog.getProperty(CidsProperty);
            List<Future<HTTPResponse>> futureList = new ArrayList<Future<HTTPResponse>>();
            for (BodyPart bodyPart : list)
            {
                String contentType = bodyPart.getContentType();
                Object content = bodyPart.getContent();
                if (!contentType.startsWith("text/html"))
                {
                    if (content instanceof InputStream)
                    {
                        String filename = bodyPart.getFileName();
                        String[] cids = bodyPart.getHeader("Content-ID");
                        if (cids == null || cids.length == 0)
                        {
                            throw new IOException("attachment filename="+filename+" doesn't have a Content-ID");
                        }
                        byte[] bytes = getBytes(bodyPart);
                        String metadataKeyString = null;
                        if (cidSet == null || !cidSet.contains(cids[0]))
                        {
                            String cid = cids[0];
                            try
                            {
                                // TODO check contentType
                                ExifParser exif = new ExifParser(bytes);
                                Date timestamp = exif.getTimestamp();
                                if (timestamp != null)
                                {
                                    Entity metadata = db.getMetadataFromDate(timestamp);
                                    exif.populate(metadata);
                                    Key key = db.put(metadata);
                                    metadataKeyString = KeyFactory.keyToString(key);
                                }
                            }
                            catch (Exception ex)
                            {
                            }
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
                                Future<HTTPResponse> res = postBlobs(filename, contentType, cid, shrinken.getImageData(), blogKeyString, metadataKeyString, request);
                                futureList.add(res);
                                cid = cid+"-original";
                            }
                            log("upload "+cid);
                            Future<HTTPResponse> res = postBlobs(filename, contentType, cid, bytes, blogKeyString, metadataKeyString, request);
                            futureList.add(res);
                        }
                        else
                        {
                            log("was uploaded "+cids[0]);
                        }
                    }
                }
            }
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
                tr.rollback();
            }
        }
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
        Key key = KeyFactory.stringToKey(encoded);
        Transaction tr = db.beginTransaction();
        try
        {
            Entity blog = db.get(key);
            Collection<BlobKey> blobKeys = (Collection<BlobKey>) blog.getProperty(BlobsProperty);
            int blobCount = 0;
            if (blobKeys != null)
            {
                blobCount = blobKeys.size();
                for (BlobKey bk : blobKeys)
                {
                    blobstore.delete(bk);
                }
            }
            db.deleteBlog(key);
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.println("Deleted blog: "+blog.getProperty("Subject")+" and "+blobCount+" blobs");
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

        
    private void addBlobs(HttpServletRequest request, String blogKeyString, String metadataKeyString) throws ServletException
    {
        while (ApiProxy.getCurrentEnvironment().getRemainingMillis() > 1000)
        {
            try
            {
                tryAddBlobs(request, blogKeyString, metadataKeyString);
                return;
            }
            catch (ConcurrentModificationException ex)
            {
                log("retry add blob "+blogKeyString);
            }
        }
        log("giving up "+blogKeyString);
    }
    private void tryAddBlobs(HttpServletRequest request, String blogKeyString, String metadataKeyString) throws ServletException
    {
        Transaction tr = db.beginTransaction();
        try
        {
            Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
            Key key = KeyFactory.stringToKey(blogKeyString);
            Entity blog = db.get(key);
            Text text = (Text) blog.getProperty(HtmlProperty);
            if (text == null)
            {
                throw new ServletException(HtmlProperty+" property not found in "+blog);
            }
            String html = text.getValue();
            Collection<String> cidSet = (Collection<String>) blog.getProperty(CidsProperty);
            if (cidSet == null)
            {
                cidSet = new HashSet<String>();
            }
            Collection<BlobKey> blobKeys = (Collection<BlobKey>) blog.getProperty(BlobsProperty);
            if (blobKeys == null)
            {
                blobKeys = new ArrayList<BlobKey>();
            }
            for (Map.Entry<String, List<BlobKey>> entry : blobs.entrySet())
            {
                String cid = entry.getKey();
                log(cid);
                if (cid.startsWith("<") && cid.endsWith(">"))
                {
                    cid = cid.substring(1, cid.length()-1);
                }
                String blobKey = entry.getValue().get(0).getKeyString();
                html = html.replace("cid:"+cid, "/blog?blob-key="+blobKey);
                cidSet.add(cid);
                blobKeys.addAll(entry.getValue());
            }
            blog.setUnindexedProperty(HtmlProperty, new Text(html));
            blog.setUnindexedProperty(CidsProperty, cidSet);
            blog.setUnindexedProperty(BlobsProperty, blobKeys);
            log(blog.toString());
            db.put(blog);
            Key metadataKey = KeyFactory.stringToKey(metadataKeyString);
            Entity metadata = db.get(metadataKey);
            blobKeys = (Collection<BlobKey>) metadata.getProperty(BlobsProperty);
            if (blobKeys == null)
            {
                blobKeys = new ArrayList<BlobKey>();
            }
            for (Map.Entry<String, List<BlobKey>> entry : blobs.entrySet())
            {
                blobKeys.addAll(entry.getValue());
            }
            metadata.setUnindexedProperty(BlobsProperty, blobKeys);
            tr.commit();
        }
        catch (EntityNotFoundException ex)
        {
            throw new ServletException(ex);
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
    private Future<HTTPResponse> postBlobs(String filename, String contentType, String contentId, byte[] data, String blogKeyString, String metadataKeyString, HttpServletRequest request) throws MessagingException, IOException
    {
        String redir = request.getServletPath();
        String metadataOpt = "";
        if (metadataKeyString != null)
        {
            metadataOpt = "&metadata="+metadataKeyString;
        }
        URL uploadUrl = new URL(blobstore.createUploadUrl(redir+"?addBlobs="+blogKeyString+metadataOpt));
        HTTPRequest httpRequest = new HTTPRequest(uploadUrl, HTTPMethod.POST, FetchOptions.Builder.withDeadline(60));
        String uid = UUID.randomUUID().toString();
        httpRequest.addHeader(new HTTPHeader("Content-Type", "multipart/form-data; boundary="+uid));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.append("--"+uid);
        ps.append(CRLF);
        ps.append("Content-Disposition: form-data; name=\""+contentId+"\"; filename=\""+filename+"\"");
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

}
