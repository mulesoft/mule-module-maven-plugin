/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class PrivilegedExportTestCase extends AbstractExportTestCase {

  public PrivilegedExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "privileged");
  }

  @Test
  public void exportInPrivilegedApi() throws Exception {
    doSuccessfulPrivilegedValidationTest("exportInPrivilegedApi", ANALYZED_CLASSES_A_B);
  }

  @Test
  public void missingExportInPrivilegedApi() throws Exception {
    doMissingPrivilegedExportTest("missingExportInPrivilegedApi", ANALYZED_CLASSES_A_B);
  }

  @Test
  public void duplicatedExportInPrivilegedApi() throws Exception {
    doDuplicatedPrivilegedExportTest("duplicatedExportInPrivilegedApi", "org.foo");
  }

}
