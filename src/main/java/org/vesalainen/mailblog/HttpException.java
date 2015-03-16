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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Timo Vesalainen
 */
public class HttpException extends Exception
{
    private int statusCode;

    public HttpException(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public HttpException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(int statusCode, String message, Throwable cause)
    {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpException(int statusCode, Throwable cause)
    {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return statusCode;
    }
    
    public void sendError(HttpServletResponse response) throws IOException
    {
        String msg = getMessage();
        if (msg != null)
        {
            response.sendError(statusCode, msg);
        }
        else
        {
            response.sendError(statusCode);
        }
    }
}
