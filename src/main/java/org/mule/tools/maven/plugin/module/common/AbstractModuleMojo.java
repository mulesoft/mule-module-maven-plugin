/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.common;

import org.mule.tools.maven.plugin.module.analyze.ModuleApiAnalyzer;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractModuleMojo extends org.apache.maven.plugin.AbstractMojo {

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  @Inject
  protected ModuleApiAnalyzer analyzer;

}
