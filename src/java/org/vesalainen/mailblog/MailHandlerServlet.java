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
import com.google.apphosting.api.ApiProxy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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
public class MailHandlerServlet extends HttpServlet implements BlogConstants
{
    private DB db;
    private Session session;
    private MailService mailService;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        db = new DB();
        Properties props = new Properties();
        session = Session.getDefaultInstance(props, null);
        mailService = MailServiceFactory.getMailService();
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
            handleMail(request);
        }
    }
    private void handleMail(HttpServletRequest request) throws IOException
    {
        Transaction tr = db.beginTransaction();
        try
        {
            MimeMessage message = new MimeMessage(session, request.getInputStream());
            String messageID = message.getMessageID();
            Key key = db.createBlogKey(messageID);
            Entity blog = new Entity(key);
            
            blog.setProperty(TimestampProperty, new Date());
            InternetAddress sender = (InternetAddress) message.getSender();
            blog.setProperty(SenderProperty, new Email(sender.getAddress()));
            String subject = message.getSubject();
            blog.setProperty(SubjectProperty, subject);
            Date sentDate = message.getSentDate();
            blog.setProperty(SentDateProperty, sentDate);
            Multipart multipart = (Multipart) message.getContent();
            List<BodyPart> list = new ArrayList<BodyPart>();
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
                    else
                    {
                        log("Blob: "+contentType);
                        if (content instanceof InputStream)
                        {   // TODO if bigger than 1000000
                            byte[] buffer = new byte[8192];
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            InputStream is = (InputStream) content;
                            String filename = bodyPart.getFileName();
                            String[] cids = bodyPart.getHeader("Content-ID");
                            if (cids == null)
                            {
                                throw new IOException("attachment filename="+filename+" doesn't have a Content-ID");
                            }
                            String cid = cids[0];
                            int rc = is.read(buffer);
                            while (rc != -1)
                            {
                                baos.write(buffer, 0, rc);
                                rc = is.read(buffer);
                            }
                            is.close();
                            if (cid.startsWith("<") && cid.endsWith(">"))
                            {
                                cid = cid.substring(1, cid.length()-1);
                            }
                            String blobKey = db.addBlob(blog.getKey(), fileName, contentType, baos.toByteArray());
                            htmlBody = htmlBody.replace("cid:"+cid, "/blog?blob="+blobKey);
                        }
                    }
                }
            }
            blog.setUnindexedProperty(HtmlProperty, new Text(htmlBody));
            db.put(blog);
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
            long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
            log("remainingMillis="+remainingMillis);
            tr.commit();
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
        try
        {
            Entity blog = db.get(key);
            db.delete(key);
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.println("Deleted blog: "+blog.getProperty("Subject"));
            writer.close();
        }
        catch (EntityNotFoundException ex)
        {
            throw new IOException(ex);
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
