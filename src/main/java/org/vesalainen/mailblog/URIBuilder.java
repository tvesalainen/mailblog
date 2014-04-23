/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.mailblog;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author tkv
 */
public class URIBuilder
{
    private final HttpServletRequest request;
    private URI uri;

    public URIBuilder(HttpServletRequest req)
    {
        this.request = req;
        try
        {
            System.err.println(req.getServletPath());
            String pathInfo = req.getPathInfo();
            if (pathInfo == null)
            {
                pathInfo = "";
            }
            this.uri = new URI(
                    req.getScheme(),
                    null,
                    req.getServerName(),
                    -1,
                    req.getServletPath()+pathInfo,
                    req.getQueryString(),
                    null);
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public URIBuilder setPath(String path)
    {
        try
        {
            if (path == null)
            {
                path = "";
            }
            this.uri = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    request.getServletPath()+path,
                    uri.getQuery(),
                    uri.getFragment());
            return this;
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public URIBuilder setQuery(String query)
    {
        try
        {
            this.uri = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    query,
                    uri.getFragment());
            return this;
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public URIBuilder setFragment(String fragment)
    {
        try
        {
            this.uri = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    fragment);
            return this;
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public URI getUri()
    {
        return uri;
    }

    @Override
    public String toString()
    {
        return uri.toString();
    }
    
}
