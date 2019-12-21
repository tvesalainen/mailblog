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

import java.util.Collections;
import java.util.List;
import javax.mail.MessagingException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImagePropertyHandlerTest
{
    
    public ImagePropertyHandlerTest()
    {
    }

    //@Test
    public void testYouTube() throws MessagingException
    {
        ImagePropertyHandler iph = new ImagePropertyHandler(Collections.EMPTY_LIST);
        assertEquals(
                "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/lp1b7DXqAEk\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>", 
                iph.replace("${youtube=https://www.youtube.com/embed/lp1b7DXqAEk}"));
    }
    
}
