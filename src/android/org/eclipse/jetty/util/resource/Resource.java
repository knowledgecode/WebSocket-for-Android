//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/* ------------------------------------------------------------ */
/**g
 * Abstract resource class.
 */
public abstract class Resource
{
    private static final Logger LOG = Log.getLogger(Resource.class);
    public static boolean __defaultUseCaches = true;
    volatile Object _associate;

    /* ------------------------------------------------------------ */
    /**
     * Change the default setting for url connection caches.
     * Subsequent URLConnections will use this default.
     * @param useCaches
     */
    public static void setDefaultUseCaches (boolean useCaches)
    {
        __defaultUseCaches=useCaches;
    }

    /* ------------------------------------------------------------ */
    public static boolean getDefaultUseCaches ()
    {
        return __defaultUseCaches;
    }

    /* ------------------------------------------------------------ */
    /** Construct a resource from a url.
     * @param url A URL.
     * @return A Resource object.
     * @throws IOException Problem accessing URL
     */
    public static Resource newResource(URL url)
        throws IOException
    {
        return newResource(url, __defaultUseCaches);
    }

    /* ------------------------------------------------------------ */
    /**
     * Construct a resource from a url.
     * @param url the url for which to make the resource
     * @param useCaches true enables URLConnection caching if applicable to the type of resource
     * @return
     */
    static Resource newResource(URL url, boolean useCaches)
    {
        if (url==null)
            return null;

        return new URLResource(url,null,useCaches);
    }

    /* ------------------------------------------------------------ */
    /** Construct a resource from a string.
     * @param resource A URL or filename.
     * @return A Resource object.
     */
    public static Resource newResource(String resource)
        throws MalformedURLException, IOException
    {
        return newResource(resource, __defaultUseCaches);
    }

    /* ------------------------------------------------------------ */
    /** Construct a resource from a string.
     * @param resource A URL or filename.
     * @param useCaches controls URLConnection caching
     * @return A Resource object.
     */
    public static Resource newResource (String resource, boolean useCaches)
    throws MalformedURLException, IOException
    {
        URL url=null;
        try
        {
            // Try to format as a URL?
            url = new URL(resource);
        }
        catch(MalformedURLException e)
        {
            LOG.warn("Bad Resource: "+resource);
            throw e;
        }

        return newResource(url);
    }

    /* ------------------------------------------------------------ */
    public static boolean isContainedIn (Resource r, Resource containingResource) throws MalformedURLException
    {
        return r.isContainedIn(containingResource);
    }

    /* ------------------------------------------------------------ */
    @Override
    protected void finalize()
    {
        release();
    }

    /* ------------------------------------------------------------ */
    public abstract boolean isContainedIn (Resource r) throws MalformedURLException;


    /* ------------------------------------------------------------ */
    /** Release any temporary resources held by the resource.
     */
    public abstract void release();


    /* ------------------------------------------------------------ */
    /**
     * Returns true if the respresened resource exists.
     */
    public abstract boolean exists();


    /* ------------------------------------------------------------ */
    /**
     * Returns true if the respresenetd resource is a container/directory.
     * If the resource is not a file, resources ending with "/" are
     * considered directories.
     */
    public abstract boolean isDirectory();

    /* ------------------------------------------------------------ */
    /**
     * Returns the last modified time
     */
    public abstract long lastModified();


    /* ------------------------------------------------------------ */
    /**
     * Return the length of the resource
     */
    public abstract long length();


    /* ------------------------------------------------------------ */
    /**
     * Returns an URL representing the given resource
     */
    public abstract URL getURL();

    /* ------------------------------------------------------------ */
    /**
     * Returns an URI representing the given resource
     */
    public URI getURI()
    {
        try
        {
            return getURL().toURI();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /* ------------------------------------------------------------ */
    /**
     * Returns an File representing the given resource or NULL if this
     * is not possible.
     */
    public abstract File getFile()
        throws IOException;


    /* ------------------------------------------------------------ */
    /**
     * Returns the name of the resource
     */
    public abstract String getName();


    /* ------------------------------------------------------------ */
    /**
     * Returns an input stream to the resource
     */
    public abstract InputStream getInputStream()
        throws java.io.IOException;

    /* ------------------------------------------------------------ */
    /**
     * Returns an output stream to the resource
     */
    public abstract OutputStream getOutputStream()
        throws java.io.IOException, SecurityException;

    /* ------------------------------------------------------------ */
    /**
     * Deletes the given resource
     */
    public abstract boolean delete()
        throws SecurityException;

    /* ------------------------------------------------------------ */
    /**
     * Rename the given resource
     */
    public abstract boolean renameTo( Resource dest)
        throws SecurityException;

    /* ------------------------------------------------------------ */
    /**
     * Returns a list of resource names contained in the given resource
     * The resource names are not URL encoded.
     */
    public abstract String[] list();

    /* ------------------------------------------------------------ */
    public Object getAssociate()
    {
        return _associate;
    }

    /* ------------------------------------------------------------ */
    public void setAssociate(Object o)
    {
        _associate=o;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The canonical Alias of this resource or null if none.
     */
    public URL getAlias()
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param out
     * @param start First byte to write
     * @param count Bytes to write or -1 for all of them.
     */
    public void writeTo(OutputStream out,long start,long count)
        throws IOException
    {
        InputStream in = getInputStream();
        try
        {
            in.skip(start);
            if (count<0)
                IO.copy(in,out);
            else
                IO.copy(in,out,count);
        }
        finally
        {
            in.close();
        }
    }

    /* ------------------------------------------------------------ */
    public void copyTo(File destination)
        throws IOException
    {
        if (destination.exists())
            throw new IllegalArgumentException(destination+" exists");
        writeTo(new FileOutputStream(destination),0,-1);
    }

    /* ------------------------------------------------------------ */
    /** Generate a properly encoded URL from a {@link File} instance.
     * @param file Target file.
     * @return URL of the target file.
     * @throws MalformedURLException
     */
    public static URL toURL(File file) throws MalformedURLException
    {
        return file.toURI().toURL();
    }
}
