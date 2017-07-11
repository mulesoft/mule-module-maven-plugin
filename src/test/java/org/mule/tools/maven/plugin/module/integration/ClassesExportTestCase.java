/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class ClassesExportTestCase extends AbstractExportTestCase {

  public ClassesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "class");
  }

  @Test
  public void exportedSuperClassInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedSuperClassInPublicClass");
  }

  @Test
  public void missingSuperClassInPublicClass() throws Exception {
    doMissingStandardExportTest("missingSuperClassInPublicClass");
  }

  @Test
  public void exportedSuperClassInProtectedInnerClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedSuperClassInProtectedInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void missingSuperClassInProtectedInnerClass() throws Exception {
    doMissingStandardExportTest("missingSuperClassInProtectedInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void ignoresSuperClassInPackageClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSuperClassInPackageClass");
  }

  @Test
  public void ignoresSuperClassInPrivateInnerClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSuperClassInPrivateInnerClass", ANALYZED_CLASSES_A_B_C);
  }
}
