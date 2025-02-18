/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.junit.MavenPluginTest;

public class ClassesExportTestCase extends AbstractExportTestCase {

  public ClassesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "class");
  }

  @MavenPluginTest
  public void exportedSuperClassInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedSuperClassInPublicClass");
  }

  @MavenPluginTest
  public void missingSuperClassInPublicClass() throws Exception {
    doMissingStandardExportTest("missingSuperClassInPublicClass");
  }

  @MavenPluginTest
  public void exportedSuperClassInProtectedInnerClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedSuperClassInProtectedInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @MavenPluginTest
  public void missingSuperClassInProtectedInnerClass() throws Exception {
    doMissingStandardExportTest("missingSuperClassInProtectedInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @MavenPluginTest
  public void ignoresSuperClassInPackageClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSuperClassInPackageClass");
  }

  @MavenPluginTest
  public void ignoresSuperClassInPrivateInnerClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSuperClassInPrivateInnerClass", ANALYZED_CLASSES_A_B_C);
  }
}
