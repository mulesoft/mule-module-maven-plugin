/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.bean;

import static org.mule.tools.maven.plugin.module.bean.Module.EXPORT_CLASS_PACKAGES;
import static org.mule.tools.maven.plugin.module.bean.Module.EXPORT_OPTIONAL_PACKAGES;
import static org.mule.tools.maven.plugin.module.bean.Module.EXPORT_SERVICES;
import static org.mule.tools.maven.plugin.module.bean.Module.PRIVILEGED_ARTIFACT_IDS;
import static org.mule.tools.maven.plugin.module.bean.Module.PRIVILEGED_CLASS_PACKAGES;

import static java.util.stream.Collectors.toCollection;

import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
  public Module create(ModuleLogger analyzerLogger, String moduleName, Properties properties) throws IOException {
    final Set<String> modulePackages = getModuleExportedPackages(analyzerLogger, properties);
    final Set<String> moduleOptionalPackages = getModuleOptionalPackages(analyzerLogger, properties);
    final Set<String> modulePrivilegedPackages = getModulePrivilegedExportedPackages(analyzerLogger, properties);
    final Set<String> modulePrivilegedArtifactIds = getModulePrivilegedArtifactIds(analyzerLogger, properties);
    final Set<ServiceDefinition> moduleServiceDefinitions = getModuleServiceDefinitions(analyzerLogger, properties);

    return new Module(moduleName,
                      modulePackages,
                      modulePrivilegedPackages,
                      moduleOptionalPackages,
                      modulePrivilegedArtifactIds,
                      moduleServiceDefinitions);
  }

  private Set<String> getModuleExportedPackages(ModuleLogger analyzerLogger, Properties properties) throws IOException {
    return getValuesFromProperty(analyzerLogger, properties, EXPORT_CLASS_PACKAGES);
  }

  private Set<String> getModulePrivilegedExportedPackages(ModuleLogger analyzerLogger, Properties properties)
      throws IOException {
    return getValuesFromProperty(analyzerLogger, properties, PRIVILEGED_CLASS_PACKAGES);
  }

  private Set<String> getModuleOptionalPackages(ModuleLogger analyzerLogger, Properties properties) throws IOException {
    return getValuesFromProperty(analyzerLogger, properties, EXPORT_OPTIONAL_PACKAGES);
  }

  private Set<String> getModulePrivilegedArtifactIds(ModuleLogger analyzerLogger, Properties properties) throws IOException {
    return getValuesFromProperty(analyzerLogger, properties, PRIVILEGED_ARTIFACT_IDS);
  }

  private Set<ServiceDefinition> getModuleServiceDefinitions(ModuleLogger analyzerLogger, Properties properties) {
    final Set<String> rawServiceDefinitions = getValuesFromProperty(analyzerLogger, properties, EXPORT_SERVICES);

    Map<String, List<String>> services = new TreeMap<>();

    for (String valueFromProperty : rawServiceDefinitions) {
      final String[] split = valueFromProperty.split(":");

      if (split.length != 2) {
        throw new IllegalStateException("Invalid service definition '" + valueFromProperty
            + "'. Must be of format '<interface fqcn>:<implementation fqcn>'");
      }

      services.computeIfAbsent(split[0], k -> new ArrayList<>())
          .add(split[1]);
    }

    return services.entrySet().stream()
        .map(entry -> {
          final ServiceDefinition serviceDefinition = new ServiceDefinition();
          serviceDefinition.setServiceInterface(entry.getKey());
          serviceDefinition.setServiceImplementations(entry.getValue());
          return serviceDefinition;
        })
        .collect(toCollection(TreeSet::new));
  }

  private Set<String> getValuesFromProperty(ModuleLogger analyzerLogger, Properties properties, String key) {
    final Set<String> values = new TreeSet<>();

    final String rawValues = (String) properties.get(key);
    if (rawValues != null) {
      StringBuilder builder = new StringBuilder("Found module: " + properties.get("module.name") + " with property=" + key + ":");
      for (String value : rawValues.split(",")) {
        if (value != null) {
          value = value.trim();
          if (value != null && !"".equals(value)) {
            values.add(value);
            builder.append("\n").append(value);
          }
        }
      }
      analyzerLogger.log(builder.toString());
    }
    return values;
  }
}
