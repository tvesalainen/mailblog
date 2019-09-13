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

import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * UTF_8_Fixer fixes content sent as UTF-8 but having charset other that UTF-8
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class UTF_8_Fixer
{
    public static String fix(String msg)
    {
        if (msg.indexOf(0xc3) != -1)
        {
            int len = msg.length();
            ByteBuffer bb = ByteBuffer.allocate(len);
            for (int ii=0;ii<len;ii++)
            {
                bb.put(ii, (byte) msg.charAt(ii));
            }
            return UTF_8.decode(bb).toString();
        }
        else
        {
            return msg;
        }
    }
}
