/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.api;

import org.mule.tools.maven.plugin.module.analyze.impl.common.ServiceDefinition;

import java.util.Set;

public interface Module {

  String getName();

  Set<String> getExportedPackages();

  Set<String> getExportedPrivilegedPackages();

  Set<String> getOptionalExportedPackages();

  Set<String> getModulePrivilegedArtifactIds();

  Set<ServiceDefinition> getModuleServiceDefinitions();

}
