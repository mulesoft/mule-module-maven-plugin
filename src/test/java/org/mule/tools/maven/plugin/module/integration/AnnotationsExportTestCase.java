/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class AnnotationsExportTestCase extends AbstractExportTestCase {

  public AnnotationsExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "annotation");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicClass() throws Exception {
    doSuccessfulValidationTest("exportedRuntimeAnnotationInPublicClass");
  }

  @Test
  public void missingRuntimeAnnotationInPublicClass() throws Exception {
    final String missingRuntimeAnnotationInPublicClass = "missingRuntimeAnnotationInPublicClass";
    doMissingExportTest(missingRuntimeAnnotationInPublicClass);
  }

  @Test
  public void ignoresCompileAnnotationInPublicClass() throws Exception {
    doSuccessfulValidationTest("ignoresCompileAnnotationInPublicClass");
  }

  @Test
  public void ignoresSourceAnnotationInPublicClass() throws Exception {
    doSuccessfulValidationTest("ignoresSourceAnnotationInPublicClass");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageClass() throws Exception {
    doSuccessfulValidationTest("ignoresRuntimeAnnotationInPackageClass");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicMethod() throws Exception {
    //TODO(pablo.kraan): what about private/protected/package methods?
    doSuccessfulValidationTest("exportedRuntimeAnnotationInPublicMethod");
  }

  @Test
  public void missingRuntimeAnnotationInPublicMethod() throws Exception {
    doMissingExportTest("missingRuntimeAnnotationInPublicMethod");
  }

  @Test
  public void ignoresCompileAnnotationInPublicMethod() throws Exception {
    doSuccessfulValidationTest("ignoresCompileAnnotationInPublicMethod");
  }

  @Test
  public void ignoresSourceAnnotationInPublicMethod() throws Exception {
    doSuccessfulValidationTest("ignoresSourceAnnotationInPublicMethod");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageMethod() throws Exception {
    doSuccessfulValidationTest("ignoresRuntimeAnnotationInPackageMethod");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicField() throws Exception {
    doSuccessfulValidationTest("exportedRuntimeAnnotationInPublicField");
  }

  @Test
  public void missingRuntimeAnnotationInPublicField() throws Exception {
    doMissingExportTest("missingRuntimeAnnotationInPublicField");
  }

  @Test
  public void ignoresCompileAnnotationInPublicField() throws Exception {
    doSuccessfulValidationTest("ignoresCompileAnnotationInPublicField");
  }

  @Test
  public void ignoresSourceAnnotationInPublicField() throws Exception {
    doSuccessfulValidationTest("ignoresSourceAnnotationInPublicField");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageField() throws Exception {
    doSuccessfulValidationTest("ignoresRuntimeAnnotationInPackageMethod");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulValidationTest("exportedRuntimeAnnotationInPublicMethodParam");
  }

  @Test
  public void missingRuntimeAnnotationInPublicMethodParam() throws Exception {
    doMissingExportTest("missingRuntimeAnnotationInPublicMethodParam");
  }

  @Test
  public void ignoresCompileAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulValidationTest("ignoresCompileAnnotationInPublicMethodParam");
  }

  @Test
  public void ignoresSourceAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulValidationTest("ignoresSourceAnnotationInPublicMethodParam");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageMethodParam() throws Exception {
    doSuccessfulValidationTest("ignoresRuntimeAnnotationInPackageMethodParam");
  }
}
