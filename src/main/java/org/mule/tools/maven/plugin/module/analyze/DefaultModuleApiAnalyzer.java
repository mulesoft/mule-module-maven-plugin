/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import static org.mule.tools.maven.plugin.module.analyze.JrePackageFinder.find;

import org.mule.tools.maven.plugin.module.bean.Module;
import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = ModuleApiAnalyzer.class)
public class DefaultModuleApiAnalyzer implements ModuleApiAnalyzer {

  public static final String PROJECT_IS_NOT_A_MULE_MODULE = "Project is not a mule module";

  // TODO(pablo.kraan): move these things ot a util class
  private static final char PACKAGE_SEPARATOR = '.';
  private static final String EMPTY_PACKAGE = "";

  /**
   * DependencyAnalyzer
   */
  @Requirement
  private DependencyAnalyzer dependencyAnalyzer;
  private final ModuleDiscoverer moduleDiscoverer = new ModuleDiscoverer();

  public static String getPackageName(String className) {
    return (className.lastIndexOf(PACKAGE_SEPARATOR) < 0) ? EMPTY_PACKAGE
        : className.substring(0, className.lastIndexOf(PACKAGE_SEPARATOR));
  }

  @Override
  public ProjectAnalysisResult analyze(MavenProject project, ModuleLogger analyzerLogger, Log log)
      throws ModuleApiAnalyzerException {

    Module module = moduleDiscoverer.discoverProjectModule(project, analyzerLogger);
    if (module == null) {
      log.info(PROJECT_IS_NOT_A_MULE_MODULE);
      return new ProjectAnalysisResult(null, null);
    }

    return analyze(project, module, analyzerLogger, log);
  }

  @Override
  public ProjectAnalysisResult analyze(MavenProject project, Module module, ModuleLogger analyzerLogger, Log log)
      throws ModuleApiAnalyzerException {
    try {
      List<Module> modules = moduleDiscoverer.discoverExternalModules(project, analyzerLogger, module.getName());

      checkExportedOptionalPackage(analyzerLogger, module.getExportedPackages(), module.getOptionalExportedPackages());

      Set<String> externalExportedPackages = getExternalExportedPackages(modules);

      final Map<String, Set<String>> projectPackageDependencies = findPackageDependencies(project, analyzerLogger);
      final Map<String, Set<String>> externalPackageDeps = calculateExternalDeps(project, analyzerLogger);

      ApiAnalysisResult standardApiAnalysisResult =
          analyzeApi(analyzerLogger, module.getExportedPackages(), module.getOptionalExportedPackages(), externalExportedPackages,
                     projectPackageDependencies,
                     externalPackageDeps);

      logPackageClosure(analyzerLogger, standardApiAnalysisResult.getExportedPackageClosure());

      ApiAnalysisResult privilegedApiAnalysisResult = null;

      if (!module.getExportedPrivilegedPackages().isEmpty()) {
        Set<String> externalPrivilegedExportedPackages = getExternalExportedPrivilegedPackages(modules);
        Set<String> exportedPackages = new HashSet<>(module.getExportedPackages());
        exportedPackages.addAll(externalExportedPackages);
        exportedPackages.addAll(externalPrivilegedExportedPackages);

        privilegedApiAnalysisResult =
            analyzeApi(analyzerLogger, module.getExportedPrivilegedPackages(), module.getOptionalExportedPackages(),
                       exportedPackages,
                       projectPackageDependencies,
                       externalPackageDeps);
      }

      logPrivilegedPackageClosure(analyzerLogger, standardApiAnalysisResult.getExportedPackageClosure());

      return new ProjectAnalysisResult(standardApiAnalysisResult, privilegedApiAnalysisResult);
    } catch (Exception exception) {
      throw new ModuleApiAnalyzerException("Cannot analyze dependencies", exception);
    }
  }

  private ApiAnalysisResult analyzeApi(ModuleLogger analyzerLogger, Set<String> projectExportedPackages,
                                       Set<String> projectOptionalPackages, Set<String> externalExportedPackages,
                                       Map<String, Set<String>> projectPackageDependencies,
                                       Map<String, Set<String>> externalPackageDeps) {

    Set<String> duplicatedPackages = removeExternalModuleExportedPackage(projectExportedPackages, externalExportedPackages);
    projectExportedPackages.removeAll(duplicatedPackages);

    final Set<String> exportedPackageClosure = new HashSet<>(projectExportedPackages);
    final Set<String> missingAnalyzedPackages = new HashSet<>();
    final Set<String> jrePackages = find();
    final Map<String, Set<String>> missingExportedPackages = new HashMap<>();

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

    Set<String> packagesToExport =
        sanitizePackagesToExport(projectExportedPackages, projectOptionalPackages, exportedPackageClosure);

    return new ApiAnalysisResult(missingExportedPackages, packagesToExport, missingAnalyzedPackages, duplicatedPackages,
                                 exportedPackageClosure);
  }

  private Set<String> sanitizePackagesToExport(Set<String> projectExportedPackages, Set<String> projectOptionalPackages,
                                               Set<String> exportedPackageClosure) {
    Set<String> packagesToExport = new HashSet<>(exportedPackageClosure);
    packagesToExport.removeAll(projectExportedPackages);
    packagesToExport.removeAll(projectOptionalPackages);
    return packagesToExport;
  }

  private void logPackageClosure(ModuleLogger analyzerLogger, Set<String> exportedPackageClosure) {
    logPackageClosure(analyzerLogger, exportedPackageClosure, "Exported package closure:");
  }

  private void logPrivilegedPackageClosure(ModuleLogger analyzerLogger, Set<String> exportedPackageClosure) {
    logPackageClosure(analyzerLogger, exportedPackageClosure, "Exported privileged package closure:");
  }

  private void logPackageClosure(ModuleLogger analyzerLogger, Set<String> exportedPackageClosure, String message) {
    StringBuilder builder = new StringBuilder(message);
    for (String exportedPackage : exportedPackageClosure) {
      builder.append("\n").append(exportedPackage);
    }
    analyzerLogger.log(builder.toString());
  }

  private void checkExportedOptionalPackage(ModuleLogger analyzerLogger,
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

  private Set<String> removeExternalModuleExportedPackage(Set<String> projectExportedPackages,
                                                          Set<String> externalExportedPackages) {
    Set<String> duplicatedPackages = new HashSet<>();

    for (String externalExportedPackage : externalExportedPackages) {
      if (projectExportedPackages.contains(externalExportedPackage)) {
        duplicatedPackages.add(externalExportedPackage);
      }
    }
    return duplicatedPackages;
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

  private boolean ignorePackage(ModuleLogger analyzerLogger, String packageName, Set<String> jrePackages,
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

  private Set<String> getExternalExportedPackages(List<Module> modules)
      throws ModuleApiAnalyzerException {
    Set<String> result = new HashSet<>();
    for (Module module : modules) {
      result.addAll(module.getExportedPackages());
    }
    return result;
  }

  private Set<String> getExternalExportedPrivilegedPackages(List<Module> modules)
      throws ModuleApiAnalyzerException {
    Set<String> result = new HashSet<>();
    for (Module module : modules) {
      result.addAll(module.getExportedPrivilegedPackages());
    }
    return result;
  }

  private Map<String, Set<String>> calculateExternalDeps(MavenProject project, ModuleLogger analyzerLogger) throws IOException {
    final Map<String, Set<String>> result = new HashMap<>();
    for (Object projectArtifact : project.getArtifacts()) {
      final Artifact artifact = (Artifact) projectArtifact;
      if ("test".equals(artifact.getScope())) {
        analyzerLogger.log("Skipping test artifact: " + artifact.getFile().toString());
        continue;
      }
      if ("pom".equals(artifact.getType())) {
        analyzerLogger.log("Skipping POM artifact: " + artifact.getFile().toString());
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

  protected Map<String, Set<String>> findPackageDependencies(MavenProject project, ModuleLogger analyzerLogger)
      throws IOException {
    String outputDirectory = project.getBuild().getOutputDirectory();
    final Map<String, Set<String>> packageDeps = findPackageDependencies(outputDirectory, analyzerLogger);

    return packageDeps;
  }

  private Map<String, Set<String>> findPackageDependencies(String path, ModuleLogger analyzerLogger)
      throws IOException {
    URL url = new File(path).toURI().toURL();

    return dependencyAnalyzer.analyze(url, analyzerLogger);
  }

}
