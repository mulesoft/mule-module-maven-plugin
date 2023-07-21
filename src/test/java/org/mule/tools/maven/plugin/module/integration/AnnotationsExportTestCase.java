/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicClass");
  }

  @Test
  public void missingRuntimeAnnotationInPublicClass() throws Exception {
    final String missingRuntimeAnnotationInPublicClass = "missingRuntimeAnnotationInPublicClass";
    doMissingStandardExportTest(missingRuntimeAnnotationInPublicClass);
  }

  @Test
  public void ignoresCompileAnnotationInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicClass");
  }

  @Test
  public void ignoresSourceAnnotationInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicClass");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageClass");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicMethod() throws Exception {
    // TODO(pablo.kraan): what about private/protected/package methods?
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicMethod");
  }

  @Test
  public void missingRuntimeAnnotationInPublicMethod() throws Exception {
    doMissingStandardExportTest("missingRuntimeAnnotationInPublicMethod");
  }

  @Test
  public void ignoresCompileAnnotationInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicMethod");
  }

  @Test
  public void ignoresSourceAnnotationInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicMethod");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageMethod");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicField() throws Exception {
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicField");
  }

  @Test
  public void missingRuntimeAnnotationInPublicField() throws Exception {
    doMissingStandardExportTest("missingRuntimeAnnotationInPublicField");
  }

  @Test
  public void ignoresCompileAnnotationInPublicField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicField");
  }

  @Test
  public void ignoresSourceAnnotationInPublicField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicField");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageMethod");
  }

  @Test
  public void exportedRuntimeAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicMethodParam");
  }

  @Test
  public void missingRuntimeAnnotationInPublicMethodParam() throws Exception {
    doMissingStandardExportTest("missingRuntimeAnnotationInPublicMethodParam");
  }

  @Test
  public void ignoresCompileAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicMethodParam");
  }

  @Test
  public void ignoresSourceAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicMethodParam");
  }

  @Test
  public void ignoresRuntimeAnnotationInPackageMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageMethodParam");
  }
}
