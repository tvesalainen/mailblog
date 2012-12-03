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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author Timo Vesalainen
 */
public class MailHandlerServlet extends HttpServlet
{
    private static final String HTML = "Html";
    private static final String BLOBS = "Blobs";
    private static final String CRLF = "\r\n";
    private DatastoreService datastore;
    private BlobstoreService blobstore;
    private Session session;
    private MailService mailService;
    private URLFetchService fetchService;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        Properties props = new Properties();
        session = Session.getDefaultInstance(props, null);
        datastore = DatastoreServiceFactory.getDatastoreService();
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
        mailService = MailServiceFactory.getMailService();
        fetchService = URLFetchServiceFactory.getURLFetchService();
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
        String removeKey = request.getParameter("remove");
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
        String key = request.getParameter("addBlobs");
        if (key != null)
        {
            addBlobs(request, key);
        }
        else
        {
            String removeKey = request.getParameter("remove");
            if (removeKey != null)
            {
                remove(removeKey, response);
            }
            else
            {
                handleMail(request);
            }
        }
    }
    private void addBlobs(HttpServletRequest request, String encoded) throws ServletException
    {
        try
        {
            Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
            Key key = KeyFactory.stringToKey(encoded);
            Entity blog = datastore.get(key);
            Text text = (Text) blog.getProperty(HTML);
            if (text == null)
            {
                throw new ServletException(HTML+" property not found in "+blog);
            }
            String html = text.getValue();
            List<BlobKey> blobKeys = new ArrayList<BlobKey>();
            for (Entry<String, List<BlobKey>> entry : blobs.entrySet())
            {
                String cid = entry.getKey();
                log(cid);
                if (cid.startsWith("<") && cid.endsWith(">"))
                {
                    cid = cid.substring(1, cid.length()-1);
                }
                String blobKey = entry.getValue().get(0).getKeyString();
                html = html.replace("cid:"+cid, "/blob?blob-key="+blobKey);
                blobKeys.addAll(entry.getValue());
            }
            blog.setUnindexedProperty(HTML, new Text(html));
            blog.setUnindexedProperty(BLOBS, blobKeys);
            log(blog.toString());
            datastore.put(blog);
        }
        catch (EntityNotFoundException ex)
        {
            throw new ServletException(ex);
        }
    }

    private void handleMail(HttpServletRequest request) throws IOException
    {
        try
        {
            MimeMessage message = new MimeMessage(session, request.getInputStream());
            Entity blog = new Entity("Blog");
            InternetAddress sender = (InternetAddress) message.getSender();
            blog.setProperty("Sender", new Email(sender.getAddress()));
            String subject = message.getSubject();
            blog.setProperty("Subject", subject);
            Date sentDate = message.getSentDate();
            blog.setProperty("SentDate", sentDate);
            Multipart multipart = (Multipart) message.getContent();
            List<BodyPart> list = new ArrayList<BodyPart>();
            findParts(list, multipart);
            boolean hasBlobs = false;
            for (BodyPart bodyPart : list)
            {
                String contentType = bodyPart.getContentType();
                Object content = bodyPart.getContent();
                if (contentType.startsWith("text/html"))
                {
                    String html = (String) content;
                    blog.setUnindexedProperty(HTML, new Text(html));
                }
                else
                {
                    if (contentType.startsWith("text/plain"))
                    {
                        if (!blog.hasProperty(HTML))
                        {
                            String plain = (String) content;
                            blog.setUnindexedProperty(HTML, new Text(plain));
                        }
                    }
                    else
                    {
                        log("Blob: "+contentType);
                        hasBlobs = true;
                    }
                }
            }
            datastore.put(blog);
            if (hasBlobs)
            {
                postBlobs(list, KeyFactory.keyToString(blog.getKey()), request);
            }
            Message reply = new Message();
            String uri = request.getRequestURI();
            int idx = uri.lastIndexOf('/');
            uri = uri.substring(idx+1);
            reply.setSender(uri);
            reply.setTo(sender.getAddress());
            reply.setSubject("Blog: "+subject+" received");
            String keyString = KeyFactory.keyToString(blog.getKey());
            StringBuffer requestURL = request.getRequestURL();
            requestURL.append("?remove="+keyString);
            reply.setHtmlBody("<a href=\""+requestURL+"\">Delete Blog</a>");
            mailService.send(reply);
        }
        catch (MessagingException ex)
        {
            throw new IOException(ex);
        }
    }
    
    private void postBlobs(List<BodyPart> list, String key, HttpServletRequest request) throws MessagingException, IOException
    {
        // This will result in timeout in most cases
        String redir = request.getServletPath();
        URL uploadUrl = new URL(blobstore.createUploadUrl(redir+"?addBlobs="+key));
        HTTPRequest httpRequest = new HTTPRequest(uploadUrl, HTTPMethod.POST, FetchOptions.Builder.withDeadline(60));
        String uid = UUID.randomUUID().toString();
        httpRequest.addHeader(new HTTPHeader("Content-Type", "multipart/form-data; boundary="+uid));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        byte[] buffer = new byte[8192];
        for (BodyPart bodyPart : list)
        {
            Object content = bodyPart.getContent();
            if (content instanceof InputStream)
            {
                InputStream is = (InputStream) content;
                String filename = bodyPart.getFileName();
                String[] cids = bodyPart.getHeader("Content-ID");
                if (cids == null)
                {
                    throw new IOException("attachment filename="+filename+" doesn't have a Content-ID");
                }
                String cid = cids[0];
                ps.append("--"+uid);
                ps.append(CRLF);
                ps.append("Content-Disposition: form-data; name=\""+cid+"\"; filename=\""+filename+"\"");
                ps.append(CRLF);
                ps.append("Content-Type: "+bodyPart.getContentType());
                ps.append(CRLF);
                ps.append("Content-Transfer-Encoding: binary");
                ps.append(CRLF);
                ps.append(CRLF);
                int rc = is.read(buffer);
                while (rc != -1)
                {
                    ps.write(buffer, 0, rc);
                    rc = is.read(buffer);
                }
                is.close();
                ps.append(CRLF);
            }
        }
        ps.append("--"+uid+"--");
        ps.append(CRLF);
        ps.close();
        log("sending blog size="+baos.size());
        httpRequest.setPayload(baos.toByteArray());
        Future<HTTPResponse> future = fetchService.fetchAsync(httpRequest);
        HTTPResponse response;
        try
        {
            log("remaining="+ApiProxy.getCurrentEnvironment().getRemainingMillis());
            response = future.get(50, TimeUnit.SECONDS);
            log("code="+response.getResponseCode());
        }
        catch (TimeoutException ex)
        {
            throw new IOException(ex);
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
        Transaction tr = datastore.beginTransaction();
        try
        {
            Entity blog = datastore.get(key);
            Collection<BlobKey> blobs = (Collection<BlobKey>) blog.getProperty(BLOBS);
            if (blobs != null)
            {
                blobstore.delete(blobs.toArray(new BlobKey[blobs.size()]));
            }
            datastore.delete(key);
            tr.commit();
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.println("Deleted blog: "+blog.getProperty("Subject"));
            writer.close();
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
