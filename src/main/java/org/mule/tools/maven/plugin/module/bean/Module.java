/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.bean;

import java.util.Set;

/**
 * Contains all the required information about a given Mule module that is needed for the API analysis.
 *
 * @since 1.0
 */
public class Module {

  public static final String MULE_MODULE_PROPERTIES = "mule-module.properties";
  public static final String MULE_MODULE_PROPERTIES_LOCATION = "META-INF/" + MULE_MODULE_PROPERTIES;

  public static final String MODULE_NAME = "module.name";
  public static final String EXPORT_CLASS_PACKAGES = "artifact.export.classPackages";
  public static final String PRIVILEGED_CLASS_PACKAGES = "artifact.privileged.classPackages";
  public static final String PRIVILEGED_ARTIFACT_IDS = "artifact.privileged.artifactIds";
  public static final String EXPORT_OPTIONAL_PACKAGES = "artifact.export.optionalPackages";
  public static final String EXPORT_SERVICES = "artifact.export.services";

  private final String name;
  private final Set<String> exportedPackages;
  private final Set<String> exportedPrivilegedPackages;
  private final Set<String> optionalExportedPackages;
  private final Set<String> modulePrivilegedArtifactIds;
  private final Set<ServiceDefinition> moduleServiceDefinitions;

  /**
   * Creates a new module
   *
   * @param name                        name of the module.
   * @param exportedPackages            Java packages that are exported on the standard module's API.
   * @param exportedPrivilegedPackages  Java packages that are exported on the privileged module's API.
   * @param optionalExportedPackages    Java packages that are exported optionally when present on another module.
   * @param moduleServiceDefinitions
   * @param modulePrivilegedArtifactIds
   */
  public Module(String name,
                Set<String> exportedPackages,
                Set<String> exportedPrivilegedPackages,
                Set<String> optionalExportedPackages,
                Set<String> modulePrivilegedArtifactIds,
                Set<ServiceDefinition> moduleServiceDefinitions) {
    this.name = name;
    this.exportedPackages = exportedPackages;
    this.exportedPrivilegedPackages = exportedPrivilegedPackages;
    this.optionalExportedPackages = optionalExportedPackages;
    this.modulePrivilegedArtifactIds = modulePrivilegedArtifactIds;
    this.moduleServiceDefinitions = moduleServiceDefinitions;
  }

  public String getName() {
    return name;
  }

  public Set<String> getExportedPackages() {
    return exportedPackages;
  }

  public Set<String> getExportedPrivilegedPackages() {
    return exportedPrivilegedPackages;
  }

  public Set<String> getOptionalExportedPackages() {
    return optionalExportedPackages;
  }

  public Set<String> getModulePrivilegedArtifactIds() {
    return modulePrivilegedArtifactIds;
  }

  public Set<ServiceDefinition> getModuleServiceDefinitions() {
    return moduleServiceDefinitions;
  }
}
