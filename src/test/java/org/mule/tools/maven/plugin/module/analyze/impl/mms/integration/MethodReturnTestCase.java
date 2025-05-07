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
public class MethodReturnTestCase extends AbstractExportTestCase {

  public MethodReturnTestCase(MavenRuntime.MavenRuntimeBuilder builder, String moduleSystem) throws Exception {
    super(builder, moduleSystem, "return");
  }

  @MavenPluginTest
  public void exportedReturnPackageInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedReturnInPublicMethod");
  }

  @MavenPluginTest
  public void exportedReturnPackageInProtectedMethod() throws Exception {
    doSuccessfulStandardValidationTest("exportedReturnInProtectedMethod");
  }

  @MavenPluginTest
  public void missingExportedReturnPackageInPublicMethod() throws Exception {
    doMissingStandardExportTest("missingReturnInPublicMethod");
  }

  @MavenPluginTest
  public void missingExportedReturnPackageInProtectedMethod() throws Exception {
    doMissingStandardExportTest("missingReturnInProtectedMethod");
  }

  @MavenPluginTest
  public void ignoresReturnPackageInPrivateMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresReturnInPrivateMethod");
  }

  @MavenPluginTest
  public void ignoresReturnPackageInPackageMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresReturnInPackageMethod");
  }

  @MavenPluginTest
  public void ignoresReturnPackageInProtectedMethodFromFinalClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresReturnInProtectedMethodFromFinalClass");
  }
}
