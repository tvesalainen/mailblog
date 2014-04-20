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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.common.base.Objects;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import static org.vesalainen.mailblog.BlogConstants.*;
import static org.vesalainen.mailblog.SpotType.Ok;
import org.vesalainen.mailblog.types.LocaleHelp;

/**
 * @author Timo Vesalainen
 */
public class Settings implements Serializable
{
    private static final long serialVersionUID = 2L;
    private Map<String,Object> map = new HashMap<>();

    Settings(final DS db, Entity entity) throws EntityNotFoundException
    {
        assert SettingsKind.equals(entity.getKind());
        RunInNamespace rin = new RunInNamespace() 
        {
            @Override
            protected Object run()
            {
                Key key = KeyFactory.createKey(DS.getRootKey(), SettingsKind, BaseKey);
                try
                {
                    Entity entity = db.get(key);
                    putAll(entity.getProperties());
                }
                catch (EntityNotFoundException ex)
                {
                }
                return null;
            }
        };
        rin.doIt(null);
        
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
        putAll(entity.getProperties());
    }
    private void putAll(Map<String,Object> m)
    {
        for (Entry<String,Object> e : m.entrySet())
        {
            if (e.getValue() != null)
            {
                map.put(e.getKey(), e.getValue());
            }
        }
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
        if (dateFormat instanceof SimpleDateFormat)
        {
            SimpleDateFormat sdf = (SimpleDateFormat) dateFormat;
            String localizedPattern = sdf.toLocalizedPattern();
            sdf.applyLocalizedPattern(localizedPattern+" z");
        }
        TimeZone timeZone = getTimeZone();
        dateFormat.setTimeZone(timeZone);
        return dateFormat;
    }
    public String getBlogTemplate()
    {
        return getBlogTemplate(BlogTemplateProperty);
    }
    public String getSearchResultTemplate()
    {
        return getBlogTemplate(SearchResultsTemplateProperty);
    }

    private String getBlogTemplate(String tmplName)
    {
        Text text = (Text) Objects.nonNull(map.get(tmplName));
        String tmpl = text.getValue();
        tmpl = tmpl.replace("${Subject}", "%1$s");
        tmpl = tmpl.replace("${Date}", "%2$s");
        tmpl = tmpl.replace("${Sender}", "%3$s");
        tmpl = tmpl.replace("${Blog}", "%4$s");
        tmpl = tmpl.replace("${Url}", "%5$s");
        tmpl = tmpl.replace("${Id}", "%6$s");
        tmpl = tmpl.replace("${Location}", "%7$s");
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
    public boolean dontSendEmail()
    {
        Boolean b = (Boolean) map.get(DontSendEmailProperty);
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

    public double getTrackBearingTolerance()
    {
        Double d = (Double) Objects.nonNull(map.get(TrackBearingToleranceProperty));
        return d.doubleValue();
    }
    public double getTrackMinimumDistance()
    {
        Double d = (Double) Objects.nonNull(map.get(TrackMinDistanceProperty));
        return d.doubleValue();
    }
    public double getEyeAltitude()
    {
        Double d = (Double) Objects.nonNull(map.get(EyeAltitudeProperty));
        return d.doubleValue();
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

    public String getImageIcon()
    {
        Link link = (Link) map.get(ImageIconProperty);
        if (link != null)
        {
            return link.getValue();
        }
        else
        {
            return DefaultIcon;
        }
    }

    public String getSpotOkIcon()
    {
        return getSpotIcon(SpotOkIconProperty);
    }

    public String getSpotCustomIcon()
    {
        return getSpotIcon(SpotCustomIconProperty);
    }

    public String getSpotHelpIcon()
    {
        return getSpotIcon(SpotHelpIconProperty);
    }

    private String getSpotIcon(String property)
    {
        Link link = (Link) map.get(property);
        if (link != null)
        {
            return link.getValue();
        }
        else
        {
            return DefaultIcon;
        }
    }

    public String getSpotIcon(SpotType type)
    {
        switch (type)
        {
            case Ok:
                return getSpotIcon(SpotOkIconProperty);
            case Custom:
                return getSpotIcon(SpotCustomIconProperty);
            case Help:
                return getSpotIcon(SpotHelpIconProperty);
            default:
                return DefaultIcon;
        }
    }
    
    public byte[] getPathColor()
    {
        Long l = (Long) Objects.nonNull(map.get(PathColorProperty));
        return rgbToArray(l.intValue());
    }

    public byte[] getTrackColor()
    {
        Long l = (Long) Objects.nonNull(map.get(TrackColorProperty));
        return rgbToArray(l.intValue());
    }
    private byte[] rgbToArray(int i)
    {
        return new byte[] {
            (byte)0xff, 
            (byte)(i >> 16), 
            (byte)((i >> 8) & 0xff), 
            (byte)(i & 0xff)
        };
    }
    @Override
    public String toString()
    {
        return "Settings{" + "map=" + map + '}';
    }

}
