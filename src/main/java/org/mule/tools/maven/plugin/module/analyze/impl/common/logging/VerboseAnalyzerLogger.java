/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.common.logging;

import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;

import org.apache.maven.plugin.logging.Log;

public class VerboseAnalyzerLogger implements ModuleLogger {

  private final Log logger;

  public VerboseAnalyzerLogger(Log logger) {
    this.logger = logger;
  }

  @Override
  public void log(String message) {
    logger.info(message);
  }
}
