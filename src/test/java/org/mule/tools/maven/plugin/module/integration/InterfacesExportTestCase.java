/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class InterfacesExportTestCase extends AbstractExportTestCase {

  public InterfacesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "interface");
  }

  @Test
  public void exportedSuperInterfaceInPublicInterface() throws Exception {
    doSuccessfulStandardValidationTest("exportedSuperInterfaceInPublicInterface");
  }

  @Test
  public void missingSuperInterfaceInPublicInterface() throws Exception {
    doMissingStandardExportTest("missingSuperInterfaceInPublicInterface");
  }

  @Test
  public void exportedSuperInterfaceInProtectedInnerInterface() throws Exception {
    doSuccessfulStandardValidationTest("exportedSuperInterfaceInProtectedInnerInterface", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void missingSuperInterfaceInProtectedInnerInterface() throws Exception {
    doMissingStandardExportTest("missingSuperInterfaceInProtectedInnerInterface", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void ignoresSuperInterfaceInPackageInterface() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSuperInterfaceInPackageInterface");
  }

  @Test
  public void ignoresSuperInterfaceInPrivateInnerInterface() throws Exception {
    doSuccessfulStandardValidationTest("ignoresSuperInterfaceInPrivateInnerInterface", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void exportedInterfaceInPublicClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedInterfaceInPublicClass");
  }

  @Test
  public void ignoresInterfaceInPrivateInnerClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresInterfaceInPrivateInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void missingInterfaceInPublicClass() throws Exception {
    doMissingStandardExportTest("missingInterfaceInPublicClass");
  }

  @Test
  public void exportedInterfaceInProtectedInnerClass() throws Exception {
    doSuccessfulStandardValidationTest("exportedInterfaceInProtectedInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void missingInterfaceInProtectedInnerClass() throws Exception {
    doMissingStandardExportTest("missingInterfaceInProtectedInnerClass", ANALYZED_CLASSES_A_B_C);
  }

  @Test
  public void ignoresInterfaceInPackageClass() throws Exception {
    doSuccessfulStandardValidationTest("ignoresInterfaceInPackageClass");
  }
}
