/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

import static org.mule.tools.maven.plugin.module.analyze.JrePackageFinder.find;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
public class DefaultModuleApiAnalyzer implements ModuleApiAnalyzer {

  public static final String PROJECT_IS_NOT_A_MULE_MODULE = "Project is not a mule module";

  //TODO(pablo.kraan): move these things ot a util class
  private static final char PACKAGE_SEPARATOR = '.';
  private static final String EMPTY_PACKAGE = "";

  /**
   * DependencyAnalyzer
   */
  @Requirement
  private DependencyAnalyzer dependencyAnalyzer;

  public static String getPackageName(String className) {
    return (className.lastIndexOf(PACKAGE_SEPARATOR) < 0) ? EMPTY_PACKAGE
        : className.substring(0, className.lastIndexOf(PACKAGE_SEPARATOR));
  }

  /*
   * @see org.mule.tools.maven.plugin.module.analyze.ProjectDependencyAnalyzer#analyze(org.apache.maven.project.MavenProject)
   */
  public ProjectDependencyAnalysis analyze(MavenProject project, AnalyzerLogger analyzerLogger)
      throws ModuleApiAnalyzerException {
    final Properties properties = getModuleProperties(project);
    if (properties == null) {
      analyzerLogger.log(PROJECT_IS_NOT_A_MULE_MODULE);
      return new ProjectDependencyAnalysis();
    }

    try {
      Set<String> projectExportedPackages = getModuleExportedPackages(analyzerLogger, properties);
      Set<String> projectOptionalPackages = getModuleOptionalPackages(analyzerLogger, properties);
      checkExportedOptionalPackage(analyzerLogger, projectExportedPackages, projectOptionalPackages);

      Set<String> externalExportedPackages =
          discoverExternalExportedPackages(project, analyzerLogger, (String) properties.get("module.name"));

      removeExternalModuleExportedPackage(analyzerLogger, projectExportedPackages, externalExportedPackages);

      final Map<String, Set<String>> projectPackageDependencies = findPackageDependencies(project, analyzerLogger);
      final Map<String, Set<String>> missingExportedPackages = new HashMap<>();
      final Map<String, Set<String>> externalPackageDeps = calculateExternalDeps(project, analyzerLogger);

      Set<String> exportedPackageClosure = new HashSet<>(projectExportedPackages);
      Set<String> missingAnalyzedPackages = new HashSet<>();
      final Set<String> jrePackages = find();

      boolean dirty;
      do {
        Set<String> diff = new HashSet<>();
        Set<String> exportedByOtherModule = new HashSet<>();
        for (String exportedPackage : exportedPackageClosure) {
          if (!ignorePackage(analyzerLogger, exportedPackage, jrePackages, externalExportedPackages)) {
            Set<String> packageDeps = projectPackageDependencies.get(exportedPackage);
            if (packageDeps != null) {
              for (String packageDep : packageDeps) {
                if (!exportedPackageClosure.contains(packageDep)
                    && !ignorePackage(analyzerLogger, packageDep, jrePackages, externalExportedPackages)) {
                  diff.add(packageDep);
                }
              }
            } else {
              packageDeps = externalPackageDeps.get(exportedPackage);
              if (packageDeps != null) {
                for (String packageDep : packageDeps) {
                  if (!exportedPackageClosure.contains(packageDep)
                      && !ignorePackage(analyzerLogger, packageDep, jrePackages, externalExportedPackages)) {
                    diff.add(packageDep);
                  }
                }
              } else {
                if (!externalExportedPackages.contains(exportedPackage) && !projectOptionalPackages.contains(exportedPackage)) {
                  missingAnalyzedPackages.add(exportedPackage);
                }
              }
            }
          }
        }
        exportedPackageClosure.addAll(diff);
        exportedPackageClosure.removeAll(exportedByOtherModule);
        diff.removeAll(exportedByOtherModule);
        dirty = !diff.isEmpty();
      } while (dirty);

      StringBuilder builder = new StringBuilder("Exported package closure:");
      for (String exportedPackage : exportedPackageClosure) {
        builder.append("\n").append(exportedPackage);
      }
      analyzerLogger.log(builder.toString());

      Set<String> packagesToExport = new HashSet<>(exportedPackageClosure);
      packagesToExport.removeAll(projectExportedPackages);
      packagesToExport.removeAll(projectOptionalPackages);

      return new ProjectDependencyAnalysis(missingExportedPackages, packagesToExport, missingAnalyzedPackages);
    } catch (Exception exception) {
      throw new ModuleApiAnalyzerException("Cannot analyze dependencies", exception);
    }
  }

  private void checkExportedOptionalPackage(AnalyzerLogger analyzerLogger,
                                            Set<String> projectExportedPackages, Set<String> projectOptionalPackages)
      throws ModuleApiAnalyzerException {
    List<String> packages = new ArrayList<>();
    for (String projectOptionalPackage : projectOptionalPackages) {
      if (projectExportedPackages.contains(projectOptionalPackage)) {
        packages.add(projectOptionalPackage);
      }
    }

    if (!packages.isEmpty()) {
      String message = buildOptionalPackageExportedMessage(packages);
      analyzerLogger.log(message);
      throw new ModuleApiAnalyzerException(message);
    }
  }

  private void removeExternalModuleExportedPackage(AnalyzerLogger analyzerLogger, Set<String> projectExportedPackages,
                                                   Set<String> externalExportedPackages) {
    for (String externalExportedPackage : externalExportedPackages) {
      if (projectExportedPackages.contains(externalExportedPackage)) {
        analyzerLogger.log(buildRedundantExportedPackageMessage(externalExportedPackage));
        projectExportedPackages.remove(externalExportedPackage);
      }
    }
  }

  /**
   * @param packages list of class packages
   * @return a message describing which optional packages are also exported.
   */
  public static String buildOptionalPackageExportedMessage(List<String> packages) {
    return "Module exports packages defined as optional: " + packages;
  }

  /**
   * @param packageName a class package
   * @return a message for a redundantly exported package
   */
  public static String buildRedundantExportedPackageMessage(String packageName) {
    return "Exported package '" + packageName + "' already exported by a module dependency";
  }

  /**
   * @param packageName a class package
   * @return a message indicating that the package was removed from the package closure
   */
  public static String buildRemovedProvidedPackageMessage(String packageName) {
    return "Package " + packageName + " already exported by a module dependency. Removing from export package closure";
  }

  /**
   * @param packageName an internal SUN class package
   * @return a message indicating that the package was removed from the package closure
   */
  public static String buildRemovedSunPackageMessage(String packageName) {
    return "Removing internal SUN package " + packageName + " from export package closure";
  }

  /**
   * @param packageName a package providede by the JRE
   * @return a message indicating that the package was removed from the package closure
   */
  public static String buildRemovedJrePackageMessage(String packageName) {
    return "Removing JRE package " + packageName + " from export package closure";
  }

  private boolean ignorePackage(AnalyzerLogger analyzerLogger, String packageName, Set<String> jrePackages,
                                Set<String> otherModuleExportedPackages) {
    boolean result = false;

    if (otherModuleExportedPackages.contains(packageName)) {
      analyzerLogger
          .log(buildRemovedProvidedPackageMessage(packageName));
      result = true;
    } else if (jrePackages.contains(packageName)) {
      analyzerLogger.log(
                         buildRemovedJrePackageMessage(packageName));
      result = true;
    } else if (packageName.startsWith("sun.") || packageName.startsWith("com.sun.")) {
      analyzerLogger.log(buildRemovedSunPackageMessage(packageName));
      result = true;
    }

    return result;
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

  private Set<String> discoverExternalExportedPackages(MavenProject project, AnalyzerLogger analyzerLogger,
                                                       String projectModuleName)
      throws ModuleApiAnalyzerException {
    final Set<String> result = new HashSet<>();
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
          if (!properties.get("module.name").equals(projectModuleName)) {
            final Set<String> modulePackages = getModuleExportedPackages(analyzerLogger, properties);
            result.addAll(modulePackages);
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

  private Set<String> getModuleExportedPackages(AnalyzerLogger analyzerLogger, Properties properties) throws IOException {
    return getModulePackagesFromProperty(analyzerLogger, properties, "artifact.export.classPackages");
  }

  private Set<String> getModuleOptionalPackages(AnalyzerLogger analyzerLogger, Properties properties) throws IOException {
    return getModulePackagesFromProperty(analyzerLogger, properties, "artifact.export.optionalPackages");
  }

  private Set<String> getModulePackagesFromProperty(AnalyzerLogger analyzerLogger, Properties properties, String key) {
    final Set<String> optionalPackages = new HashSet<>();

    final String classPackages = (String) properties.get(key);
    if (classPackages != null) {
      StringBuilder builder = new StringBuilder("Found module: " + properties.get("module.name") + " with property=" + key + ":");
      for (String classPackage : classPackages.split(",")) {
        if (classPackage != null) {
          classPackage = classPackage.trim();
          if (classPackage != null) {
            optionalPackages.add(classPackage);
            builder.append("\n").append(classPackage);
          }
        }
      }
      analyzerLogger.log(builder.toString());
    }
    return optionalPackages;
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

  private Map<String, Set<String>> calculateExternalDeps(MavenProject project, AnalyzerLogger analyzerLogger) throws IOException {
    final Map<String, Set<String>> result = new HashMap<>();
    for (Object projectArtifact : project.getArtifacts()) {
      final Artifact artifact = (Artifact) projectArtifact;
      if ("test".equals(artifact.getScope())) {
        analyzerLogger.log("Skipping test artifact: " + artifact.getFile().toString());
        continue;
      }
      final Map<String, Set<String>> artifactExternalPackageDeps =
          findPackageDependencies(artifact.getFile().toString(), analyzerLogger);

      for (String externalPackageName : artifactExternalPackageDeps.keySet()) {
        final Set<String> packageDeps = artifactExternalPackageDeps.get(externalPackageName);
        Set<String> externalPackageDeps = result.get(externalPackageName);
        if (externalPackageDeps == null) {
          externalPackageDeps = new HashSet<>();
          result.put(externalPackageName, externalPackageDeps);
        }
        externalPackageDeps.addAll(packageDeps);
      }
    }

    return result;
  }

  protected Map<String, Set<String>> findPackageDependencies(MavenProject project, AnalyzerLogger analyzerLogger)
      throws IOException {
    String outputDirectory = project.getBuild().getOutputDirectory();
    final Map<String, Set<String>> packageDeps = findPackageDependencies(outputDirectory, analyzerLogger);

    return packageDeps;
  }

  private Map<String, Set<String>> findPackageDependencies(String path, AnalyzerLogger analyzerLogger)
      throws IOException {
    URL url = new File(path).toURI().toURL();

    return dependencyAnalyzer.analyze(url, analyzerLogger);
  }

}
