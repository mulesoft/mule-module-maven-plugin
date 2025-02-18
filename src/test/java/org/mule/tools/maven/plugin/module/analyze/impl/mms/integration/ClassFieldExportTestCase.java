/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.junit.MavenPluginTest;

public class ClassFieldExportTestCase extends AbstractExportTestCase {

  public ClassFieldExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "field");
  }

  @MavenPluginTest
  public void exportedPublicInstanceField() throws Exception {
    doSuccessfulStandardValidationTest("exportedPublicInstanceField");
  }

  @MavenPluginTest
  public void exportedProtectedInstanceField() throws Exception {
    doSuccessfulStandardValidationTest("exportedProtectedInstanceField");
  }

  @MavenPluginTest
  public void missingProtectedInstanceField() throws Exception {
    doMissingStandardExportTest("missingProtectedInstanceField");
  }

  @MavenPluginTest
  public void missingPublicInstanceField() throws Exception {
    doMissingStandardExportTest("missingPublicInstanceField");
  }

  @MavenPluginTest
  public void ignoresPrivateInstanceField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresPrivateInstanceField");
  }

  @MavenPluginTest
  public void ignoresPackageInstanceField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresPackageInstanceField");
  }

  @MavenPluginTest
  public void ignoresProtectedInstanceFieldInFinalClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresProtectedInstanceFieldInFinalClass");
  }
}
