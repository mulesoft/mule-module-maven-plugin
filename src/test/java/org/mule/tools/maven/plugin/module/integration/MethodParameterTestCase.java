/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class MethodParameterTestCase extends AbstractExportTestCase {

  public MethodParameterTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "parameter");
  }

  @Test
  public void exportedParameterInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedParameterInPublicMethod");
  }

  @Test
  public void exportedParameterInProtectedMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedParameterInProtectedMethod");
  }

  @Test
  public void missingExportedParameterInPublicMethod() throws Exception {
    doMissingStandardExportTest("missingParameterInPublicMethod");
  }

  @Test
  public void missingExportedParameterInProtectedMethod() throws Exception {
    doMissingStandardExportTest("missingParameterInProtectedMethod");
  }

  @Test
  public void ignoresParameterInPrivateMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresParameterInPrivateMethod");
  }

  @Test
  public void ignoresParameterInPackageMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresParameterInPackageMethod");
  }

  @Test
  public void ignoresParamInProtectedMethodFromFinalClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresParameterInProtectedMethodFromFinalClass");
  }

}
