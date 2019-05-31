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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import static org.vesalainen.mailblog.BlogConstants.*;
import static org.vesalainen.mailblog.SpotType.Ok;
import org.vesalainen.mailblog.types.LocaleHelp;

/**
 * @author Timo Vesalainen
 */
public class Settings extends BaseProperty
{

    Settings(DS db, Entity entity) throws EntityNotFoundException
    {
        super(SettingsKind, db, entity);
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
        return (Email) map.get(EmailProperty);
    }

    public String getNickname()
    {
        String nickname = (String) map.get(NicknameProperty);
        if (nickname != null)
        {
            return (String) map.get(NicknameProperty);
        }
        else
        {
            return "";
        }
    }

    public Locale getLocale()
    {
        return LocaleHelp.toLocale((String) map.get(LocaleProperty));
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
            sdf.applyLocalizedPattern(localizedPattern + " z");
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
        Text text = (Text) map.get(tmplName);
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
        Text text = (Text) map.get(CommentTemplateProperty);
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
        return b != null ? b : false;
    }

    public boolean isFixPic()
    {
        Boolean b = (Boolean) map.get(FixPicProperty);
        return b != null ? b : false;
    }

    public boolean isCommonPlacemarks()
    {
        Boolean b = (Boolean) map.get(CommonPlacemarksProperty);
        return b != null ? b : false;
    }

    public boolean dontSendEmail()
    {
        Boolean b = (Boolean) map.get(DontSendEmailProperty);
        return b != null ? b : false;
    }

    public int getPicMaxHeight()
    {
        return getIntProperty(PicMaxHeightProperty);
    }

    public int getPicMaxWidth()
    {
        return getIntProperty(PicMaxWidthProperty);
    }

    public double getTrackBearingTolerance()
    {
        return getDoubleProperty(TrackBearingToleranceProperty);
    }

    public double getTrackMinimumDistance()
    {
        return getDoubleProperty(TrackMinDistanceProperty);
    }

    public double getTrackMaxSpeed()
    {
        Double d = (Double) map.get(TrackMaxSpeedProperty);
        if (d == null)
        {
            return 15;
        }
        return d;
    }
    public int getZoom()
    {
        return convertAltToZoom(getEyeAltitude());
    }
    public double getEyeAltitude()
    {
        return getDoubleProperty(EyeAltitudeProperty, 300.0);
    }
    private static final String DefaultIcon = "http://maps.google.com/mapfiles/kml/shapes/info.png";

    public String getIcon(Entity entity)
    {
        switch (entity.getKind())
        {
            case BlogKind:
                return getBlogIcon();
            case MetadataKind:
                return getImageIcon();
            case PlacemarkKind:
                String description = (String) entity.getProperty(DescriptionProperty);
                return getSpotIcon(description);
            default:
                return DefaultIcon;
        }
    }

    public int getPpm(Entity entity)
    {
        switch (entity.getKind())
        {
            case BlogKind:
                return getIntProperty(BlogIconPpmProperty, 1000);
            case MetadataKind:
                return getIntProperty(ImageIconPpmProperty, 1000);
            case PlacemarkKind:
                return getIntProperty(PlacemarkIconPpmProperty, 1000);
            default:
                return 1000;
        }
    }

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
        return getSpotIcon(AnchoredIconProperty);
    }

    public String getSpotCustomIcon()
    {
        return getSpotIcon(WaypointIconProperty);
    }

    public String getSpotHelpIcon()
    {
        return getSpotIcon(DestinationIconProperty);
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
                return getSpotIcon(AnchoredIconProperty);
            case Waypoint:
                return getSpotIcon(WaypointIconProperty);
            case Help:
                return getSpotIcon(DestinationIconProperty);
            default:
                return DefaultIcon;
        }
    }

    public String getBlogImage()
    {
        Link link = (Link) map.get(ImageProperty);
        if (link != null)
        {
            return link.getValue();
        }
        else
        {
            return "";
        }
    }

    /**
     * Returns full opaque track color
     *
     * @return
     */
    public byte[] getTrackColor()
    {
        return getTrackColor(0xff);
    }

    /**
     * Return track color
     *
     * @param alpha
     * @return
     */
    public byte[] getTrackColor(int alpha)
    {
        Long l = (Long) map.get(TrackColorProperty);
        return rgbToArray(alpha, l.intValue());
    }

    private byte[] rgbToArray(int alpha, int i)
    {
        return new byte[]
        {
            (byte) alpha,
            (byte) (i >> 16),
            (byte) ((i >> 8) & 0xff),
            (byte) (i & 0xff)
        };
    }

    public String getTrackCss3Color()
    {
        Long l = (Long) map.get(TrackColorProperty);
        return css3Color(l.intValue());
    }

    private String css3Color(int c)
    {
        return String.format("#%06X", c);
    }

    public int getMinOpaque()
    {
        Long l = (Long) map.get(MinOpaqueProperty);
        if (l == null)
        {
            return 50;
        }
        return l.intValue();
    }

    @Override
    public String toString()
    {
        return "Settings{" + "map=" + map + '}';
    }

    public static int convertAltToZoom(double range)
    {
        int zoom = (int) Math.round(Math.log(35200000 / range) / Math.log(2));
        if (zoom < 0)
        {
            zoom = 0;
        }
        else if (zoom > 19)
        {
            zoom = 19;
        }
        return zoom;
    }

    public static int convertZoomToAlt(double zoom)
    {
        int range = (int) (35200000 / (Math.pow(2, zoom)));
        if (range < 300)
        {
            range = 300;
        }
        return range;
    }
}
