/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import static org.mule.tools.maven.plugin.module.bean.Module.MODULE_NAME;
import static org.mule.tools.maven.plugin.module.bean.Module.MULE_MODULE_PROPERTIES;
import static org.mule.tools.maven.plugin.module.bean.Module.MULE_MODULE_PROPERTIES_LOCATION;

import static java.lang.Thread.currentThread;

import org.mule.tools.maven.plugin.module.bean.Module;
import org.mule.tools.maven.plugin.module.bean.ModuleFactory;
import org.mule.tools.maven.plugin.module.common.ModuleLogger;

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
   * @param project        project being analyzed.
   * @param analyzerLogger collects all the logging information generated during the project analysis
   * @return a module corresponding to the project being analyzed, null if the project is not a Mule module
   * @throws ModuleApiAnalyzerException
   */
  public Module discoverProjectModule(MavenProject project, ModuleLogger analyzerLogger)
      throws ModuleApiAnalyzerException {
    Module module = null;
    Properties properties = getModuleProperties(project);
    if (properties != null) {
      try {
        module = moduleFactory.create(analyzerLogger, (String) properties.get(MODULE_NAME), properties);
      } catch (IOException e) {
        throw new ModuleApiAnalyzerException("Cannot read project's " + MULE_MODULE_PROPERTIES, e);
      }
    }

    return module;
  }

  /**
   * Discovers all the Mule modules used as dependencies on the Maven project under analysis
   *
   * @param project           project being analyzed.
   * @param analyzerLogger    collects all the logging information generated during the project analysis
   * @param projectModuleName name of the module that corresponds to the project being analyzed
   * @return a list containing all the Mule modules that are dependencies of the analyzed project.
   * @throws ModuleApiAnalyzerException
   */
  public List<Module> discoverExternalModules(MavenProject project, ModuleLogger analyzerLogger,
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
                                                                  currentThread().getContextClassLoader());

      final Enumeration<URL> resources;
      try {
        resources = contextClassLoader.getResources(MULE_MODULE_PROPERTIES_LOCATION);
      } catch (Exception e) {
        throw new ModuleApiAnalyzerException("Cannot read " + MULE_MODULE_PROPERTIES_LOCATION, e);
      }

      while (resources.hasMoreElements()) {
        final URL url = resources.nextElement();
        try {
          Properties properties = loadProperties(url);

          // Skips project module properties
          String moduleName = (String) properties.get(MODULE_NAME);
          if (!moduleName.equals(projectModuleName)) {
            result.add(moduleFactory.create(analyzerLogger, moduleName, properties));
          }
        } catch (Exception e) {
          throw new ModuleApiAnalyzerException("Cannot read " + MULE_MODULE_PROPERTIES_LOCATION + " from " + url.toString(), e);
        }
      }
    } catch (Exception e) {
      throw new ModuleApiAnalyzerException("Error getting project resources", e);
    }

    return result;
  }

  private Properties loadProperties(URL url) throws IOException {
    Properties properties = new Properties();

    try (InputStream resourceStream = url.openStream()) {
      properties.load(resourceStream);
    }
    return properties;
  }

  private Properties getModuleProperties(MavenProject project) throws ModuleApiAnalyzerException {
    Properties properties = null;
    try {
      File result = null;
      File modulePropertiesCompiled = new File(project.getBuild().getOutputDirectory(), MULE_MODULE_PROPERTIES_LOCATION);
      if (modulePropertiesCompiled.exists()) {
        result = modulePropertiesCompiled;
      }

      if (result == null) {
        final List<Resource> projectResources = project.getBuild().getResources();
        for (final Resource resource : projectResources) {
          File moduleProperties = new File(resource.getDirectory(), MULE_MODULE_PROPERTIES_LOCATION);
          if (moduleProperties.exists()) {
            result = moduleProperties;
            break;
          }
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
