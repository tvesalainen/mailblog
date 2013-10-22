/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Timo Vesalainen
 */
public class CommonSettingsServlet extends BaseSettingsServlet
{

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        RunInNamespace rin = new RunInNamespace() 
        {
            @Override
            protected Object run()
            {
                try
                {
                    superGet(req, resp);
                }
                catch (ServletException | IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
                return null;
            }
        };
        rin.doIt(null);
    }

    private void superGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        super.doGet(req, resp);
    }
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        RunInNamespace rin = new RunInNamespace() 
        {
            @Override
            protected Object run()
            {
                try
                {
                    superPost(req, resp);
                }
                catch (ServletException | IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
                return null;
            }
        };
        rin.doIt(null);
    }

    private void superPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        super.doPost(req, resp);
    }
}
