/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import java.util.Set;

/**
 * Contains all the required information about a given Mule module that is needed for the API analysis.
 *
 * @since 1.0
 */
public class Module {

  private final String name;
  private final Set<String> exportedPackages;
  private final Set<String> exportedPrivilegedPackages;
  private final Set<String> optionalExportedPackages;

  /**
   * Creates a new module
   *
   * @param name                       name of the module.
   * @param exportedPackages           Java packages that are exported on the standard module's API.
   * @param exportedPrivilegedPackages Java packages that are exported on the privileged module's API.
   * @param optionalExportedPackages   Java packages that are exported optionally when present on another module.
   */
  public Module(String name, Set<String> exportedPackages, Set<String> exportedPrivilegedPackages,
                Set<String> optionalExportedPackages) {
    this.name = name;
    this.exportedPackages = exportedPackages;
    this.exportedPrivilegedPackages = exportedPrivilegedPackages;
    this.optionalExportedPackages = optionalExportedPackages;
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
}
