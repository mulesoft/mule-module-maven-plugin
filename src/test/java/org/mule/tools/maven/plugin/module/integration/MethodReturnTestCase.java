/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class MethodReturnTestCase extends AbstractExportTestCase {

  public MethodReturnTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "return");
  }

  @Test
  public void exportedReturnPackageInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedReturnInPublicMethod");
  }

  @Test
  public void exportedReturnPackageInProtectedMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedReturnInProtectedMethod");
  }

  @Test
  public void missingExportedReturnPackageInPublicMethod() throws Exception {
    doMissingStandardExportTest("missingReturnInPublicMethod");
  }

  @Test
  public void missingExportedReturnPackageInProtectedMethod() throws Exception {
    doMissingStandardExportTest("missingReturnInProtectedMethod");
  }

  @Test
  public void ignoresReturnPackageInPrivateMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresReturnInPrivateMethod");
  }

  @Test
  public void ignoresReturnPackageInPackageMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresReturnInPackageMethod");
  }

  @Test
  public void ignoresReturnPackageInProtectedMethodFromFinalClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresReturnInProtectedMethodFromFinalClass");
  }
}
