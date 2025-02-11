/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms;

import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzerException;
import org.mule.tools.maven.plugin.module.analyze.api.ProjectAnalysisResult;
import org.mule.tools.maven.plugin.module.analyze.impl.common.BaseMuleModuleApiAnalyzer;
import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzer;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.sisu.Typed;

@Named("mms")
@Singleton
@Typed(ModuleApiAnalyzer.class)
public class DefaultMuleModuleSystemApiAnalyzer extends BaseMuleModuleApiAnalyzer {

  public static final String PROJECT_IS_NOT_A_MULE_MODULE = "Project is not a mule module";

  private final MuleModuleSystemModuleDiscoverer muleModuleSystemModuleDiscoverer = new MuleModuleSystemModuleDiscoverer();

  public static String getPackageName(String className) {
    return (className.lastIndexOf(PACKAGE_SEPARATOR) < 0) ? EMPTY_PACKAGE
        : className.substring(0, className.lastIndexOf(PACKAGE_SEPARATOR));
  }

  @Override
  public ProjectAnalysisResult analyze(MavenProject project, ModuleLogger analyzerLogger, Log log)
      throws ModuleApiAnalyzerException {

    Module module = muleModuleSystemModuleDiscoverer.discoverProjectModule(project, analyzerLogger);
    if (module == null) {
      log.info(PROJECT_IS_NOT_A_MULE_MODULE);
      return new ProjectAnalysisResult(null, null);
    }

    return analyze(project, module, analyzerLogger, log);
  }

}
