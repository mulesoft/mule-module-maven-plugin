/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

/**
 * Maintains the result of analyzing a project API.
 */
public class ProjectAnalysisResult {

  private final ApiAnalysisResult standardApi;
  private final ApiAnalysisResult privilegedAPi;

  /**
   * Creates a new analysis result for the project
   *
   * @param standardApi   analysis result for the standard module API.
   * @param privilegedAPi analysis result for the privileged module API.
   */
  public ProjectAnalysisResult(ApiAnalysisResult standardApi, ApiAnalysisResult privilegedAPi) {
    this.standardApi = standardApi;
    this.privilegedAPi = privilegedAPi;
  }

  public ApiAnalysisResult getStandardApi() {
    return standardApi;
  }

  public ApiAnalysisResult getPrivilegedAPi() {
    return privilegedAPi;
  }
}
