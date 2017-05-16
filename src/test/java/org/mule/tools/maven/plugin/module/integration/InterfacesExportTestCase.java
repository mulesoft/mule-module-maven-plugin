/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
    doSuccessfulValidationTest("exportedSuperInterfaceInPublicInterface");
  }

  @Test
  public void missingSuperInterfaceInPublicInterface() throws Exception {
    doMissingExportTest("missingSuperInterfaceInPublicInterface");
  }

  @Test
  public void exportedSuperInterfaceInProtectedInnerInterface() throws Exception {
    doSuccessfulValidationTest("exportedSuperInterfaceInProtectedInnerInterface",
                               new String[] {PATH_CLASS_A, PATH_CLASS_B, "org/foo/A$C"});
  }

  @Test
  public void missingSuperInterfaceInProtectedInnerInterface() throws Exception {
    doMissingExportTest("missingSuperInterfaceInProtectedInnerInterface",
                        new String[] {PATH_CLASS_A, PATH_CLASS_B, "org/foo/A$C"});
  }

  @Test
  public void ignoresSuperInterfaceInPackageInterface() throws Exception {
    doSuccessfulValidationTest("ignoresSuperInterfaceInPackageInterface");
  }

  @Test
  public void ignoresSuperInterfaceInPrivateInnerInterface() throws Exception {
    doSuccessfulValidationTest("ignoresSuperInterfaceInPrivateInnerInterface",
                               new String[] {PATH_CLASS_A, PATH_CLASS_B, "org/foo/A$C"});
  }

  @Test
  public void exportedInterfaceInPublicClass() throws Exception {
    doSuccessfulValidationTest("exportedInterfaceInPublicClass");
  }

  @Test
  public void ignoresInterfaceInPrivateInnerClass() throws Exception {
    doSuccessfulValidationTest("ignoresInterfaceInPrivateInnerClass", new String[] {PATH_CLASS_A, PATH_CLASS_B, "org/foo/A$C"});
  }

  @Test
  public void missingInterfaceInPublicClass() throws Exception {
    doMissingExportTest("missingInterfaceInPublicClass");
  }

  @Test
  public void exportedInterfaceInProtectedInnerClass() throws Exception {
    doSuccessfulValidationTest("exportedInterfaceInProtectedInnerClass",
                               new String[] {PATH_CLASS_A, PATH_CLASS_B, "org/foo/A$C"});
  }

  @Test
  public void missingInterfaceInProtectedInnerClass() throws Exception {
    doMissingExportTest("missingInterfaceInProtectedInnerClass", new String[] {PATH_CLASS_A, PATH_CLASS_B, "org/foo/A$C"});
  }

  @Test
  public void ignoresInterfaceInPackageClass() throws Exception {
    doSuccessfulValidationTest("ignoresInterfaceInPackageClass");
  }
}
