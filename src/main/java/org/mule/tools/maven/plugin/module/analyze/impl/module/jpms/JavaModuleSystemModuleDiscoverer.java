/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.module.jpms;

import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;
import org.mule.tools.maven.plugin.module.util.JpmsModule;

import java.util.Optional;

import org.apache.maven.project.MavenProject;

/**
 * Discovers the Mule modules used on the Maven project under analysis
 *
 * @since 1.0
 */
public class JavaModuleSystemModuleDiscoverer {

  /**
   * Discovers the module corresponding to the Maven project under analysis.
   * 
   * @param project        project being analyzed.
   * @param analyzerLogger collects all the logging information generated during the project analysis
   * @return a module corresponding to the project being analyzed, null if the project is not a Mule module
   */
  public Optional<Module> discoverProjectModule(MavenProject project, ModuleLogger analyzerLogger)
      throws Exception {
    return JpmsModule.discoverProjectModule(project, analyzerLogger).map(JpmsModule::toMuleModule);
  }

}
