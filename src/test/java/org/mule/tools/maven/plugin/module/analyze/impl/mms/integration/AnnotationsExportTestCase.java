/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.junit.MavenPluginTest;

public class AnnotationsExportTestCase extends AbstractExportTestCase {

  public AnnotationsExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "annotation");
  }

  @MavenPluginTest
  public void exportedRuntimeAnnotationInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicClass");
  }

  @MavenPluginTest
  public void missingRuntimeAnnotationInPublicClass() throws Exception {
    final String missingRuntimeAnnotationInPublicClass = "missingRuntimeAnnotationInPublicClass";
    doMissingStandardExportTest(missingRuntimeAnnotationInPublicClass);
  }

  @MavenPluginTest
  public void ignoresCompileAnnotationInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicClass");
  }

  @MavenPluginTest
  public void ignoresSourceAnnotationInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicClass");
  }

  @MavenPluginTest
  public void ignoresRuntimeAnnotationInPackageClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageClass");
  }

  @MavenPluginTest
  public void exportedRuntimeAnnotationInPublicMethod() throws Exception {
    // TODO(pablo.kraan): what about private/protected/package methods?
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicMethod");
  }

  @MavenPluginTest
  public void missingRuntimeAnnotationInPublicMethod() throws Exception {
    doMissingStandardExportTest("missingRuntimeAnnotationInPublicMethod");
  }

  @MavenPluginTest
  public void ignoresCompileAnnotationInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicMethod");
  }

  @MavenPluginTest
  public void ignoresSourceAnnotationInPublicMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicMethod");
  }

  @MavenPluginTest
  public void ignoresRuntimeAnnotationInPackageMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageMethod");
  }

  @MavenPluginTest
  public void exportedRuntimeAnnotationInPublicField() throws Exception {
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicField");
  }

  @MavenPluginTest
  public void missingRuntimeAnnotationInPublicField() throws Exception {
    doMissingStandardExportTest("missingRuntimeAnnotationInPublicField");
  }

  @MavenPluginTest
  public void ignoresCompileAnnotationInPublicField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicField");
  }

  @MavenPluginTest
  public void ignoresSourceAnnotationInPublicField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicField");
  }

  @MavenPluginTest
  public void ignoresRuntimeAnnotationInPackageField() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageMethod");
  }

  @MavenPluginTest
  public void exportedRuntimeAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("exportedRuntimeAnnotationInPublicMethodParam");
  }

  @MavenPluginTest
  public void missingRuntimeAnnotationInPublicMethodParam() throws Exception {
    doMissingStandardExportTest("missingRuntimeAnnotationInPublicMethodParam");
  }

  @MavenPluginTest
  public void ignoresCompileAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("ignoresCompileAnnotationInPublicMethodParam");
  }

  @MavenPluginTest
  public void ignoresSourceAnnotationInPublicMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSourceAnnotationInPublicMethodParam");
  }

  @MavenPluginTest
  public void ignoresRuntimeAnnotationInPackageMethodParam() throws Exception {
    doSuccessfulStandardValidationTest("ignoresRuntimeAnnotationInPackageMethodParam");
  }
}
