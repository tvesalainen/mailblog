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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImagePropertyHandler extends BodyPropertyHandler
{
    private Map<String,BodyPart> bodyParts = new HashMap<>();
    private Map<String,String> cidMap = new HashMap<>();
    
    public ImagePropertyHandler(List<BodyPart> bodyPartList) throws MessagingException
    {
        MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
        for (BodyPart bodyPart : bodyPartList)
        {
            String fileName = null;
            String cid = null;
            fileName = bodyPart.getFileName();
            String contentType = bodyPart.getContentType();
            String[] cids = bodyPart.getHeader("Content-ID");
            if (cids != null)
            {
                cid = cids[0];
                if (cid.startsWith("<") && cid.endsWith(">"))
                {
                    cid = cid.substring(1, cid.length()-1);
                }
            }
            else
            {
                if (fileName != null)
                {
                    cid = fileName;
                    bodyPart.addHeader("Content-ID", "<"+cid+">");
                    String mimeType = mimeMap.getContentType(fileName);
                    if (!contentType.startsWith(mimeType))    // winlink put's attachment as content type
                    {
                        bodyPart.setHeader("Content-Type", mimeType+"; name=\""+fileName+"\"");
                    }
                }
            }
            if (cid != null && fileName != null)
            {
                bodyParts.put(cid, bodyPart);
                cidMap.put(fileName, cid);
            }
        }
    }

    @Override
    protected String handle(String key, String filename)
    {
        if ("img".equalsIgnoreCase(key))
        {
            String cid = cidMap.get(filename);
            if (cid != null)
            {
                BodyPart bodyPart = bodyParts.get(cid);
                if (bodyPart != null)
                {
                    return "<img src=\"cid:"+cid+"\" alt=\""+filename+"\">";
                }
                else
                {
                    info("body missing filename=%s cid=%s", filename, cid);
                }
            }
            else
            {
                info("cid missing filename=%s", filename);
            }
            return "";
        }
        else
        {
            return super.handle(key, filename);
        }
    }
    
}
