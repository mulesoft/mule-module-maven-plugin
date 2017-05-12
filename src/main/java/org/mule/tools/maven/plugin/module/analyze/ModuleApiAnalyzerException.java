/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

public class ModuleApiAnalyzerException extends Exception {

  /**
   * The serialisation unique ID.
   */
  private static final long serialVersionUID = -5954447543668196977L;

  public ModuleApiAnalyzerException(String message) {
    super(message);
  }

  public ModuleApiAnalyzerException(String message, Throwable cause) {
    super(message, cause);
  }
}
