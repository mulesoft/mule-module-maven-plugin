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
import org.apache.maven.model.Resource;
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
    public ProjectDependencyAnalysis analyze(MavenProject project, AnalyzerLogger analyzerLogger)
            throws ModuleApiAnalyzerException
    {
        final Properties properties = getModuleProperties(project);
        if (properties == null)
        {
            analyzerLogger.log("Project is not a mule module");
            return new ProjectDependencyAnalysis();
        }


        try
        {
            Set<String> projectExportedPackages = getModuleExportedPackages(analyzerLogger, properties);
            Set<String> externalExportedPackages = discoverExternalExportedPackages(project, analyzerLogger, (String) properties.get("module.name"));

            final Map<String, Set<String>> projectPackageDependencies = findPackageDependencies(project, analyzerLogger);
            final Map<String, Set<String>> missingExportedPackages = new HashMap<>();

            for (String projectExportedPackage : projectExportedPackages)
            {
                final Set<String> packageDeps = projectPackageDependencies.get(projectExportedPackage);
                if (packageDeps != null)
                {
                    packageDeps.removeAll(projectExportedPackages);
                    packageDeps.removeAll(externalExportedPackages);
                    if (!packageDeps.isEmpty())
                    {
                        Set<String> packageToExport = new HashSet<>();
                        for (String packageDep : packageDeps)
                        {
                            if (!externalExportedPackages.contains(packageDep))
                            {
                                packageToExport.add(packageDep);
                            }
                        }
                        if (!packageToExport.isEmpty())
                        {
                            analyzerLogger.log("Missing export packages: " + packageDeps);
                            missingExportedPackages.put(projectExportedPackage, packageDeps);
                        }
                    }
                }
            }

            if (!missingExportedPackages.isEmpty())
            {
                final Map<String, Set<String>> externalPackageDeps = calculateExternalDeps(project, analyzerLogger);

                boolean dirty;
                do
                {
                    dirty = false;

                    for (String missingExportedPackage : new HashSet<String>(missingExportedPackages.keySet()))
                    {
                        final Set<String> missingExportedPackageDeps = missingExportedPackages.get(missingExportedPackage);
                        for (String missingExportedPackageDep : missingExportedPackageDeps)
                        {
                            final Set<String> packagesToAdd = externalPackageDeps.get(missingExportedPackageDep);
                            if (packagesToAdd != null && !packagesToAdd.isEmpty())
                            {
                                Set<String> currentNoExportedPackageDeps = missingExportedPackages.get(missingExportedPackageDep);
                                if (currentNoExportedPackageDeps == null)
                                {
                                    currentNoExportedPackageDeps = new HashSet<>();
                                    missingExportedPackages.put(missingExportedPackageDep, currentNoExportedPackageDeps);
                                }
                                for (String packageToAdd : packagesToAdd)
                                {
                                    if (!externalExportedPackages.contains(packageToAdd))
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
                }
                while (dirty);
            }

            Set<String> packagesToExport = new HashSet<String>();
            for (String packageDeps : missingExportedPackages.keySet())
            {
                packagesToExport.addAll(missingExportedPackages.get(packageDeps));
            }

            return new ProjectDependencyAnalysis(missingExportedPackages, packagesToExport);
        }
        catch (Exception exception)
        {
            throw new ModuleApiAnalyzerException("Cannot analyze dependencies", exception);
        }
    }

    private Properties getModuleProperties(MavenProject project) throws ModuleApiAnalyzerException
    {
        Properties properties = null;
        try
        {
            final List<Resource> projectResources = project.getBuild().getResources();
            File result = null;
            for (int i = 0; i <projectResources.size();i++)
            {
                final Resource resource = projectResources.get(i);

                File moduleProperties1 = new File(resource.getDirectory(), "META-INF" + File.separator + "mule-module.properties");
                if (moduleProperties1.exists())
                {
                    result = moduleProperties1;
                    break;
                }
            }

            final File moduleProperties = result;
            if (moduleProperties != null)
            {
                properties = loadProperties(moduleProperties.toURI().toURL());
            }
        }
        catch (IOException e)
        {
            throw new ModuleApiAnalyzerException("Cannot access module properties", e);
        }
        return properties;
    }

    private Set<String> discoverExternalExportedPackages(MavenProject project, AnalyzerLogger analyzerLogger, String projectModuleName) throws ModuleApiAnalyzerException
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
                    Properties properties = loadProperties(url);

                    // Skips project module properties
                    if (!properties.get("module.name").equals(projectModuleName))
                    {
                        final Set<String> modulePackages = getModuleExportedPackages(analyzerLogger, properties);
                        result.addAll(modulePackages);
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

    private Set<String> getModuleExportedPackages(AnalyzerLogger analyzerLogger, Properties properties) throws IOException
    {
        final Set<String> modulePackages = new HashSet<String>();

        final String classPackages = (String) properties.get("artifact.export.classPackages");
        StringBuilder builder = new StringBuilder("Found module: " + properties.get("module.name") + " exporting:");
        for (String classPackage : classPackages.split(","))
        {
            if (classPackage != null)
            {
                classPackage = classPackage.trim();
                if (classPackage != null)
                {
                    modulePackages.add(classPackage);
                    builder.append("\n").append(classPackage);
                }
            }
        }
        analyzerLogger.log(builder.toString());
        return modulePackages;
    }

    private Properties loadProperties(URL url) throws IOException
    {
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
        return properties;
    }

    private Map<String, Set<String>> calculateExternalDeps(MavenProject project, AnalyzerLogger analyzerLogger) throws IOException
    {
        final Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Object projectArtifact : project.getArtifacts())
        {
            final Artifact artifact = (Artifact) projectArtifact;
            if ("test".equals(artifact.getScope()))
            {
                analyzerLogger.log("Skipping test artifact: " + artifact.getFile().toString());
                continue;
            }
            final Map<String, Set<String>> artifactExternalPackageDeps = findPackageDependencies(artifact.getFile().toString(), analyzerLogger);

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

    protected Map<String, Set<String>> findPackageDependencies(MavenProject project, AnalyzerLogger analyzerLogger)
            throws IOException
    {
        String outputDirectory = project.getBuild().getOutputDirectory();
        final Map<String, Set<String>> packageDeps = findPackageDependencies(outputDirectory, analyzerLogger);

        return packageDeps;
    }

    private Map<String, Set<String>> findPackageDependencies(String path, AnalyzerLogger analyzerLogger)
            throws IOException
    {
        URL url = new File(path).toURI().toURL();

        return dependencyAnalyzer.analyze(url, analyzerLogger);
    }

}
