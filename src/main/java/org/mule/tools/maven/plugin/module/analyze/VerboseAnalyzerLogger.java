/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

import org.apache.maven.plugin.logging.Log;

public class VerboseAnalyzerLogger implements AnalyzerLogger {

  private final Log logger;

  public VerboseAnalyzerLogger(Log logger) {
    this.logger = logger;
  }

  @Override
  public void log(String message) {
    logger.info(message);
  }
}
