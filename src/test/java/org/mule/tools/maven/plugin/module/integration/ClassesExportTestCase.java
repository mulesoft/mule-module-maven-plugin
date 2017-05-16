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

  private static final String CLASS_PATH_C = "org/foo/A$C";
  private static final String[] EXPECTED_ANALYZED_CLASSES = {PATH_CLASS_A, PATH_CLASS_B, CLASS_PATH_C};

  public ClassesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "class");
  }

  @Test
  public void exportedSuperClassInPublicClass() throws Exception {
    doSuccessfulValidationTest("exportedSuperClassInPublicClass");
  }

  @Test
  public void missingSuperClassInPublicClass() throws Exception {
    doMissingExportTest("missingSuperClassInPublicClass");
  }

  @Test
  public void exportedSuperClassInProtectedInnerClass() throws Exception {
    doSuccessfulValidationTest("exportedSuperClassInProtectedInnerClass", EXPECTED_ANALYZED_CLASSES);
  }

  @Test
  public void missingSuperClassInProtectedInnerClass() throws Exception {
    doMissingExportTest("missingSuperClassInProtectedInnerClass", EXPECTED_ANALYZED_CLASSES);
  }

  @Test
  public void ignoresSuperClassInPackageClass() throws Exception {
    doSuccessfulValidationTest("ignoresSuperClassInPackageClass");
  }

  @Test
  public void ignoresSuperClassInPrivateInnerClass() throws Exception {
    doSuccessfulValidationTest("ignoresSuperClassInPrivateInnerClass", EXPECTED_ANALYZED_CLASSES);
  }
}
