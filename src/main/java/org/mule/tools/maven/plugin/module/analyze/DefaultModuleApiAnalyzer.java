/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = ModuleApiAnalyzer.class)
public class DefaultModuleApiAnalyzer implements ModuleApiAnalyzer
{

    //TODO(pablo.kraan): move these things ot a util class
    private static final char PACKAGE_SEPARATOR = '.';
    private static final String EMPTY_PACKAGE = "";

    /**
     * DependencyAnalyzer
     */
    @Requirement
    private DependencyAnalyzer dependencyAnalyzer;

    public static String getPackageName(String className)
    {
        return (className.lastIndexOf(PACKAGE_SEPARATOR) < 0) ? EMPTY_PACKAGE : className.substring(0, className.lastIndexOf(PACKAGE_SEPARATOR));
    }

    /*
     * @see org.mule.tools.maven.plugin.module.analyze.ProjectDependencyAnalyzer#analyze(org.apache.maven.project.MavenProject)
     */
    public ProjectDependencyAnalysis analyze(MavenProject project)
            throws ModuleApiAnalyzerException
    {
        //TODO(pablo.kraan): must ensure that there is a exported properties in the current module, otherwise there is nothing to check
        Set<String> exportedPackages = discoverExportedPackages(project);

        try
        {
            final Map<String, Set<String>> artifactPackageDeps = findPackageDependencies(project);
            final Map<String, Set<String>> noExportedPackageDeps = new HashMap<>();

            for (String exportedPackage : exportedPackages)
            {
                final Set<String> exportedPackageDeps = artifactPackageDeps.get(exportedPackage);
                if (exportedPackageDeps != null)
                {
                    exportedPackageDeps.removeAll(exportedPackages);
                    if (!exportedPackageDeps.isEmpty())
                    {
                        noExportedPackageDeps.put(exportedPackage, exportedPackageDeps);
                    }
                }
            }

            if (!noExportedPackageDeps.isEmpty())
            {
                final Map<String, Set<String>> externalPackageDeps = calculateExternalDeps(project);

                boolean dirty;
                do
                {
                    dirty = false;

                    for (String exportedPackage : new HashSet<String>(noExportedPackageDeps.keySet()))
                    {
                        final Set<String> noExportedPackageNames = noExportedPackageDeps.get(exportedPackage);
                        for (String noExportedPackageName : noExportedPackageNames)
                        {
                            final Set<String> packagesToAdd = externalPackageDeps.get(noExportedPackageName);
                            if (packagesToAdd != null && !packagesToAdd.isEmpty())
                            {
                                Set<String> currentNoExportedPackageDeps = noExportedPackageDeps.get(noExportedPackageName);
                                if (currentNoExportedPackageDeps == null)
                                {
                                    currentNoExportedPackageDeps = new HashSet<>();
                                    noExportedPackageDeps.put(noExportedPackageName, currentNoExportedPackageDeps);
                                }
                                for (String packageToAdd : packagesToAdd)
                                {
                                    if (currentNoExportedPackageDeps.add(packageToAdd))
                                    {
                                        dirty = true;
                                    }
                                }
                            }
                        }
                    }
                }
                while (dirty);
            }

            Set<String> packagesToExport = new HashSet<String>();
            for (String packageDeps : noExportedPackageDeps.keySet())
            {
                packagesToExport.addAll(noExportedPackageDeps.get(packageDeps));
            }

            return new ProjectDependencyAnalysis(noExportedPackageDeps, packagesToExport);
        }
        catch (IOException exception)
        {
            throw new ModuleApiAnalyzerException("Cannot analyze dependencies", exception);
        }
    }

    private Set<String> discoverExportedPackages(MavenProject project) throws ModuleApiAnalyzerException
    {
        final Set<String> result = new HashSet<String>();
        Set<URL> urls = new HashSet<URL>();
        List<String> elements = null;
        try
        {
            elements = project.getRuntimeClasspathElements();
            elements.addAll(project.getCompileClasspathElements());

            for (String element : elements)
            {
                urls.add(new File(element).toURI().toURL());
            }

            ClassLoader contextClassLoader = URLClassLoader.newInstance(
                    urls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());

            try
            {
                final Enumeration<URL> resources = contextClassLoader.getResources("META-INF/mule-module.properties");
                while (resources.hasMoreElements())
                {
                    final URL url = resources.nextElement();
                    System.out.println("Found module: " + url);
                    Properties properties = new Properties();

                    InputStream resourceStream = null;
                    try
                    {
                        resourceStream = url.openStream();
                        properties.load(resourceStream);
                    }
                    finally
                    {
                        if (resourceStream != null)
                        {
                            resourceStream.close();
                        }
                    }

                    final String classPackages = (String) properties.get("artifact.export.classPackages");
                    for (String classPackage : classPackages.split(","))
                    {
                        if (classPackage != null)
                        {
                            classPackage = classPackage.trim();
                            if (classPackage != null)
                            {
                                result.add(classPackage);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new ModuleApiAnalyzerException("Cannot read mule-module.properties", e);
            }
        }
        catch (Exception e)
        {
            throw new ModuleApiAnalyzerException("Error getting project resources", e);
        }

        return result;
    }

    private Map<String, Set<String>> calculateExternalDeps(MavenProject project) throws IOException
    {
        final Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Object projectArtifact : project.getArtifacts())
        {
            final Artifact artifact = (Artifact) projectArtifact;
            if (artifact.getScope() == "test")
            {
                System.out.println("Skipping test artifact: " + artifact.getFile().toString());
                continue;
            }
            final Map<String, Set<String>> artifactExternalPackageDeps = findPackageDependencies(artifact.getFile().toString());

            for (String externalPackageName : artifactExternalPackageDeps.keySet())
            {
                final Set<String> packageDeps = artifactExternalPackageDeps.get(externalPackageName);
                if (packageDeps != null && !packageDeps.isEmpty())
                {
                    Set<String> externalPackageDeps = result.get(externalPackageName);
                    if (externalPackageDeps == null)
                    {
                        externalPackageDeps = new HashSet<String>();
                        result.put(externalPackageName, externalPackageDeps);
                    }
                    externalPackageDeps.addAll(packageDeps);
                }
            }
        }

        return result;
    }

    protected Map<String, Set<String>> findPackageDependencies(MavenProject project)
            throws IOException
    {
        String outputDirectory = project.getBuild().getOutputDirectory();
        final Map<String, Set<String>> packageDeps = findPackageDependencies(outputDirectory);

        return packageDeps;
    }

    private Map<String, Set<String>> findPackageDependencies(String path)
            throws IOException
    {
        URL url = new File(path).toURI().toURL();

        return dependencyAnalyzer.analyze(url);
    }

}
