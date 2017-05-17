/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class ClassFieldExportTestCase extends AbstractExportTestCase {

  public ClassFieldExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "field");
  }

  @Test
  public void exportedPublicInstanceField() throws Exception {
    doSuccessfulValidationTest("exportedPublicInstanceField");
  }

  @Test
  public void exportedProtectedInstanceField() throws Exception {
    doSuccessfulValidationTest("exportedProtectedInstanceField");
  }

  @Test
  public void missingProtectedInstanceField() throws Exception {
    doMissingExportTest("missingProtectedInstanceField");
  }

  @Test
  public void missingPublicInstanceField() throws Exception {
    doMissingExportTest("missingPublicInstanceField");
  }

  @Test
  public void ignoresPrivateInstanceField() throws Exception {
    doSuccessfulValidationTest("ignoresPrivateInstanceField");
  }

  @Test
  public void ignoresPackageInstanceField() throws Exception {
    doSuccessfulValidationTest("ignoresPackageInstanceField");
  }

  @Test
  public void ignoresProtectedInstanceFieldInFinalClass() throws Exception {
    doSuccessfulValidationTest("ignoresProtectedInstanceFieldInFinalClass");
  }
}
