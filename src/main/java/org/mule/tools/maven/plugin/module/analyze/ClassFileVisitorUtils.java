/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.codehaus.plexus.util.DirectoryScanner;


/**
 * Utility to visit classes in a library given either as a jar file or an exploded directory.
 */
public final class ClassFileVisitorUtils
{

    private static final String[] CLASS_INCLUDES = {"**/*.class"};

    private ClassFileVisitorUtils()
    {
        // private constructor for utility class
    }

    public static void accept(URL url, ClassFileVisitor visitor)
            throws IOException
    {
        System.out.println("Analizyng: " + url);
        if (url.getPath().endsWith(".jar"))
        {
            acceptJar(url, visitor);
        }
        else if (url.getProtocol().equalsIgnoreCase("file"))
        {
            try
            {
                File file = new File(new URI(url.toString()));

                if (file.isDirectory())
                {
                    acceptDirectory(file, visitor);
                }
                else if (file.exists())
                {
                    throw new IllegalArgumentException("Cannot accept visitor on URL: " + url);
                }
            }
            catch (URISyntaxException exception)
            {
                IllegalArgumentException e = new IllegalArgumentException("Cannot accept visitor on URL: " + url);
                e.initCause(exception);
                throw e;
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot accept visitor on URL: " + url);
        }
    }

    private static void acceptJar(URL url, ClassFileVisitor visitor)
            throws IOException
    {
        JarInputStream in = new JarInputStream(url.openStream());
        try
        {
            JarEntry entry = null;

            while ((entry = in.getNextJarEntry()) != null)
            {
                String name = entry.getName();

                if (name.endsWith(".class"))
                {
                    visitClass(name, in, visitor);
                }
            }
        }
        finally
        {
            in.close();
        }
    }

    private static void acceptDirectory(File directory, ClassFileVisitor visitor)
            throws IOException
    {
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("File is not a directory");
        }

        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(directory);
        scanner.setIncludes(CLASS_INCLUDES);

        scanner.scan();

        String[] paths = scanner.getIncludedFiles();

        for (String path : paths)
        {
            path = path.replace(File.separatorChar, '/');

            File file = new File(directory, path);
            FileInputStream in = new FileInputStream(file);

            try
            {
                visitClass(path, in, visitor);
            }
            finally
            {
                in.close();
            }
        }
    }

    private static void visitClass(String path, InputStream in, ClassFileVisitor visitor)
    {
        if (!path.endsWith(".class"))
        {
            throw new IllegalArgumentException("Path is not a class");
        }

        String className = path.substring(0, path.length() - 6);

        className = className.replace('/', '.');

        visitor.visitClass(className, in);
    }
}
