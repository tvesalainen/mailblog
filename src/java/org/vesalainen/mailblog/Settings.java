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
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import static org.vesalainen.mailblog.BlogConstants.NicknameProperty;
import org.vesalainen.mailblog.types.LocaleHelp;

/**
 * @author Timo Vesalainen
 */
public class Settings implements BlogConstants, Serializable
{
    private static final long serialVersionUID = 1L;
    private Map<String,Object> map = new HashMap<String,Object>();

    Settings(DS db, Entity entity) throws EntityNotFoundException
    {
        populate(db, entity);
    }
    
    private void populate(DS db, Entity entity) throws EntityNotFoundException
    {
        Key parent = entity.getParent();
        if (parent != null && SettingsKind.equals(parent.getKind()))
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
    
    public String getTitle()
    {
        return (String) map.get(TitleProperty);
    }
    public String getDescription()
    {
        return (String) map.get(DescriptionProperty);
    }
    public Email getEmail()
    {
        return (Email) Objects.nonNull(map.get(EmailProperty));
    }
    public String getNickname()
    {
        String nickname = (String) map.get(NicknameProperty);
        if (nickname != null)
        {
            return (String) Objects.nonNull(map.get(NicknameProperty));
        }
        else
        {
            return "";
        }
    }
    public Locale getLocale()
    {
        return LocaleHelp.toLocale((String)map.get(LocaleProperty));
    }
    public TimeZone getTimeZone()
    {
        String id = (String) map.get(TimeZoneProperty);
        return id != null ? TimeZone.getTimeZone(id) : TimeZone.getDefault();
    }
    public DateFormat getDateFormat()
    {
        Locale locale = getLocale();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        TimeZone timeZone = getTimeZone();
        dateFormat.setTimeZone(timeZone);
        return dateFormat;
    }
    public String getBlogAreaTemplate()
    {
        Text text = (Text) Objects.nonNull(map.get(BlogAreaTemplateProperty));
        String tmpl = text.getValue();
        return tmpl;
    }
    public String getBlogTemplate()
    {
        Text text = (Text) Objects.nonNull(map.get(BlogTemplateProperty));
        String tmpl = text.getValue();
        tmpl = tmpl.replace("${Subject}", "%1$s");
        tmpl = tmpl.replace("${Date}", "%2$s");
        tmpl = tmpl.replace("${Sender}", "%3$s");
        tmpl = tmpl.replace("${Blog}", "%4$s");
        tmpl = tmpl.replace("${Url}", "%5$s");
        tmpl = tmpl.replace("${Id}", "%6$s");
        return tmpl;
    }
    public String getCommentTemplate()
    {
        Text text = (Text) Objects.nonNull(map.get(CommentTemplateProperty));
        String tmpl = text.getValue();
        tmpl = tmpl.replace("${Nickname}", "%1$s");
        tmpl = tmpl.replace("${Date}", "%2$s");
        tmpl = tmpl.replace("${Comment}", "%3$s");
        tmpl = tmpl.replace("${Hidden}", "%4$s");
        tmpl = tmpl.replace("${Id}", "%5$s");
        return tmpl;
    }
    public boolean isPublishImmediately()
    {
        Boolean b = (Boolean) map.get(PublishImmediatelyProperty);
        return b != null ? b: false;
    }
    public boolean isFixPic()
    {
        Boolean b = (Boolean) map.get(FixPicProperty);
        return b != null ? b: false;
    }
    public boolean isCommonPlacemarks()
    {
        Boolean b = (Boolean) map.get(CommonPlacemarksProperty);
        return b != null ? b: false;
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

    private static final String DefaultIcon = "http://maps.google.com/mapfiles/kml/shapes/info.png";
    public String getBlogIcon()
    {
        Link link = (Link) map.get(BlogIconProperty);
        if (link != null)
        {
            return link.getValue();
        }
        else
        {
            return DefaultIcon;
        }
    }

    public String getPlacemarkIcon()
    {
        Link link = (Link) map.get(PlacemarkIconProperty);
        if (link != null)
        {
            return link.getValue();
        }
        else
        {
            return DefaultIcon;
        }
    }
    
    @Override
    public String toString()
    {
        return "Settings{" + "map=" + map + '}';
    }

}
