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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.vesalainen.mailblog.types.LocaleHelp;

/**
 * @author Timo Vesalainen
 */
public class Settings implements BlogConstants, Serializable
{
    private static final long serialVersionUID = 1L;
    private Map<String,Object> map = new HashMap<String,Object>();

    Settings(DB db, Entity entity) throws EntityNotFoundException
    {
        populate(db, entity);
    }
    
    private void populate(DB db, Entity entity) throws EntityNotFoundException
    {
        Key parent = entity.getParent();
        if (parent != null)
        {
            Entity ent = db.get(parent);
            populate(db, ent);
        }
        map.putAll(entity.getProperties());
    }

    public Map<String, Object> getMap()
    {
        return map;
    }
    
    public Email getEmail()
    {
        return (Email) Objects.nonNull(map.get(EmailProperty));
    }
    public String getNickname()
    {
        return (String) Objects.nonNull(map.get(NicknameProperty));
    }
    public Locale getLocale()
    {
        return LocaleHelp.toLocale((String)map.get(LocaleProperty));
    }
    public Text getTemplate()
    {
        return (Text) Objects.nonNull(map.get(TemplateProperty));
    }
    public boolean isPublishImmediately()
    {
        return (Boolean) Objects.nonNull(map.get(PublishImmediatelyProperty));
    }
    public boolean isFixPic()
    {
        return (Boolean) Objects.nonNull(map.get(FixPicProperty));
    }
    public int getPicMaxHeight()
    {
        Long l = (Long) Objects.nonNull(map.get(PicMaxHeightProperty));
        return l.intValue();
    }
    public int getPicMaxWidth()
    {
        Long l = (Long) Objects.nonNull(map.get(PicMaxWidthProperty));
        return l.intValue();
    }
    public int getShowCount()
    {
        Long l = (Long) Objects.nonNull(map.get(ShowCountProperty));
        return l.intValue();
    }
}
