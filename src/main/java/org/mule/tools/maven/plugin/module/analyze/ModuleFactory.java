/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Creates {@link Module} from Mule module descriptors
 *
 * @since 1.0
 */
public class ModuleFactory {

  /**
   * Creates a module from a Mule module descriptor
   *
   * @param analyzerLogger collects all the logging information generated during the project analysis
   * @param moduleName     name of the module to create
   * @param properties     module descriptor properties
   * @return a non null module that corresponds to the given properties
   * @throws IOException
   */
  public Module create(AnalyzerLogger analyzerLogger, String moduleName, Properties properties) throws IOException {
    final Set<String> modulePackages = getModuleExportedPackages(analyzerLogger, properties);
    final Set<String> moduleOptionalPackages = getModuleOptionalPackages(analyzerLogger, properties);
    final Set<String> modulePrivilegedPackages = getModulePrivilegedExportedPackages(analyzerLogger, properties);

    return new Module(moduleName, modulePackages, modulePrivilegedPackages, moduleOptionalPackages);
  }

  private Set<String> getModuleExportedPackages(AnalyzerLogger analyzerLogger, Properties properties) throws IOException {
    return getModulePackagesFromProperty(analyzerLogger, properties, "artifact.export.classPackages");
  }

  private Set<String> getModulePrivilegedExportedPackages(AnalyzerLogger analyzerLogger, Properties properties)
      throws IOException {
    return getModulePackagesFromProperty(analyzerLogger, properties, "artifact.privileged.classPackages");
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
}
