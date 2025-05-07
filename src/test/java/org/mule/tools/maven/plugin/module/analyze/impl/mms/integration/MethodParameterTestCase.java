/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.junit.MavenPluginTest;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AbstractExportTestCase.ExportTestCaseContextProvider.class)
public class MethodParameterTestCase extends AbstractExportTestCase {

  public MethodParameterTestCase(MavenRuntime.MavenRuntimeBuilder builder, String moduleSystem) throws Exception {
    super(builder, moduleSystem, "parameter");
  }

  @MavenPluginTest
  public void exportedParameterInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedParameterInPublicMethod");
  }

  @MavenPluginTest
  public void exportedParameterInProtectedMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedParameterInProtectedMethod");
  }

  @MavenPluginTest
  public void missingExportedParameterInPublicMethod() throws Exception {
    doMissingStandardExportTest("missingParameterInPublicMethod");
  }

  @MavenPluginTest
  public void missingExportedParameterInProtectedMethod() throws Exception {
    doMissingStandardExportTest("missingParameterInProtectedMethod");
  }

  @MavenPluginTest
  public void ignoresParameterInPrivateMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresParameterInPrivateMethod");
  }

  @MavenPluginTest
  public void ignoresParameterInPackageMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresParameterInPackageMethod");
  }

  @MavenPluginTest
  public void ignoresParamInProtectedMethodFromFinalClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresParameterInProtectedMethodFromFinalClass");
  }

}
