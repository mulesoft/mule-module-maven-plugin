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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

/**
 * Discovers the Mule modules used on the Maven project under analysis
 *
 * @since 1.0
 */
public class ModuleDiscoverer {

  private final ModuleFactory moduleFactory = new ModuleFactory();

  /**
   * Discovers the module corresponding to the Maven project under analysis
   *
   * @param project project being analyzed.
   * @param analyzerLogger collects all the logging information generated during the project analysis
   * @return a module corresponding to the project being analyzed, null if the project is not a Mule module
   * @throws ModuleApiAnalyzerException
   */
  public Module discoverProjectModule(MavenProject project, AnalyzerLogger analyzerLogger)
      throws ModuleApiAnalyzerException {
    Module module = null;
    Properties properties = getModuleProperties(project);
    if (properties != null) {
      try {
        module = moduleFactory.create(analyzerLogger, (String) properties.get("module.name"), properties);
      } catch (IOException e) {
        throw new ModuleApiAnalyzerException("Cannot read project's mule-module.properties", e);
      }
    }

    return module;
  }

  /**
   * Discovers all the Mule modules used as dependencies on the Maven project under analysis
   *
   * @param project project being analyzed.
   * @param analyzerLogger collects all the logging information generated during the project analysis
   * @param projectModuleName name of the module that corresponds to the project being analyzed
   * @return a list containing all the Mule modules that are dependencies of the analyzed project.
   * @throws ModuleApiAnalyzerException
   */
  public List<Module> discoverExternalModules(MavenProject project, AnalyzerLogger analyzerLogger,
                                              String projectModuleName)
      throws ModuleApiAnalyzerException {
    final List<Module> result = new LinkedList<>();

    Set<URL> urls = new HashSet<>();
    List<String> elements;
    try {
      elements = project.getRuntimeClasspathElements();
      elements.addAll(project.getCompileClasspathElements());

      for (String element : elements) {
        urls.add(new File(element).toURI().toURL());
      }

      ClassLoader contextClassLoader = URLClassLoader.newInstance(
                                                                  urls.toArray(new URL[0]),
                                                                  Thread.currentThread().getContextClassLoader());

      try {
        final Enumeration<URL> resources = contextClassLoader.getResources("META-INF/mule-module.properties");
        while (resources.hasMoreElements()) {
          final URL url = resources.nextElement();
          Properties properties = loadProperties(url);

          // Skips project module properties
          String moduleName = (String) properties.get("module.name");
          if (!moduleName.equals(projectModuleName)) {
            result.add(moduleFactory.create(analyzerLogger, moduleName, properties));
          }
        }
      } catch (Exception e) {
        throw new ModuleApiAnalyzerException("Cannot read mule-module.properties", e);
      }
    } catch (Exception e) {
      throw new ModuleApiAnalyzerException("Error getting project resources", e);
    }

    return result;
  }

  private Properties loadProperties(URL url) throws IOException {
    Properties properties = new Properties();

    InputStream resourceStream = null;
    try {
      resourceStream = url.openStream();
      properties.load(resourceStream);
    } finally {
      if (resourceStream != null) {
        resourceStream.close();
      }
    }
    return properties;
  }

  private Properties getModuleProperties(MavenProject project) throws ModuleApiAnalyzerException {
    Properties properties = null;
    try {
      final List<Resource> projectResources = project.getBuild().getResources();
      File result = null;
      for (int i = 0; i < projectResources.size(); i++) {
        final Resource resource = projectResources.get(i);

        File moduleProperties1 = new File(resource.getDirectory(), "META-INF" + File.separator + "mule-module.properties");
        if (moduleProperties1.exists()) {
          result = moduleProperties1;
          break;
        }
      }

      final File moduleProperties = result;
      if (moduleProperties != null) {
        properties = loadProperties(moduleProperties.toURI().toURL());
      }
    } catch (IOException e) {
      throw new ModuleApiAnalyzerException("Cannot access module properties", e);
    }
    return properties;
  }

}
