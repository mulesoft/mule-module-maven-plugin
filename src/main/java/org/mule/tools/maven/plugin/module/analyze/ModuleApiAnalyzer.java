/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Analyze a module API to ensure that there are not missing exported pacakges
 */
public interface ModuleApiAnalyzer {

  String ROLE = ModuleApiAnalyzer.class.getName();

  ProjectAnalysisResult analyze(MavenProject project, ModuleLogger analyzerLogger, Log log) throws ModuleApiAnalyzerException;

  ProjectAnalysisResult analyze(MavenProject project, org.mule.tools.maven.plugin.module.bean.Module projectModule,
                                ModuleLogger analyzerLogger, Log log)
      throws ModuleApiAnalyzerException;
}
