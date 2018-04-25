/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RemoteHelper
{
    
    protected RemoteApiOptions options;
    protected RemoteApiInstaller installer;

    public RemoteHelper()
    {
    }

    @Before
    public void before() throws IOException
    {
        options = new RemoteApiOptions();
        options.server("adventurersblog.appspot.com", 443);
        options.useApplicationDefaultCredential();
        installer = new RemoteApiInstaller();
        installer.install(options);
    }

    @After
    public void after()
    {
        installer.uninstall();
    }
    
}
