/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MailHandlerServletTest extends DSHelper
{
    
    public MailHandlerServletTest()
    {
    }

    @Test
    public void testTextPlain() throws MessagingException, IOException
    {
        try (InputStream is = MailHandlerServletTest.class.getResourceAsStream("/NWLVXNVCVEFT.mime"))
        {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(session, is);
            String contentType = message.getContentType();
            String content = (String) message.getContent();
            String utf8 = UTF_8_Fixer.fix(content);
        }
    }
    @Test
    public void testWinlink() throws MessagingException, IOException
    {
        String html = read("/WL2K.mime");
        assertTrue(html.contains("<img src=\"cid:DSCN0979pienempi.jpg\" alt=\"DSCN0979pienempi.jpg\">"));
    }
    @Test
    public void testTablet() throws MessagingException, IOException
    {
        String html = read("/Tabletista.mime");
        assertTrue(html.contains("<img src=\"cid:16ba5fbf1458cfc9b62\" alt=\"DSCN0933.JPG\">"));
    }
    private String read(String mime) throws MessagingException, IOException
    {
        try (InputStream is = MailHandlerServletTest.class.getResourceAsStream(mime))
        {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(session, is);
            Object content = message.getContent();
            if (content instanceof Multipart)
            {
                Multipart multipart = (Multipart) message.getContent();
                List<BodyPart> bodyPartList = MailHandlerServlet.findParts(multipart);
                ImagePropertyHandler bodyPropertyHandler = new ImagePropertyHandler(bodyPartList);
                String htmlBody = MailHandlerServlet.getHtmlBody(bodyPartList);
                for (BodyPart bodyPart : bodyPartList)
                {
                    String cid=null;
                    String[] cids = bodyPart.getHeader("Content-ID");
                    if (cids != null)
                    {
                        cid = cids[0];
                    }
                    System.err.println(bodyPart.getFileName()+" "+cid);
                }
                htmlBody = bodyPropertyHandler.replace(htmlBody);
                return htmlBody;
            }
        }
        return "";
    }
    
}
