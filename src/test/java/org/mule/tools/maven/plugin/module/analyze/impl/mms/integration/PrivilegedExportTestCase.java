/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms.integration;

import java.util.List;
import java.util.Map;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class PrivilegedExportTestCase extends AbstractExportTestCase {

  public PrivilegedExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "privileged");
  }

  @Test
  public void exportInPrivilegedApi() throws Exception {
    doSuccessfulPrivilegedValidationTest("exportInPrivilegedApi", ANALYZED_CLASSES_A_B);
  }

  @Test
  public void missingExportInPrivilegedApi() throws Exception {
    doMissingPrivilegedExportTest("missingExportInPrivilegedApi", ANALYZED_CLASSES_A_B);
  }

  @Test
  public void duplicatedExportInPrivilegedApi() throws Exception {
    doDuplicatedPrivilegedExportTest("duplicatedExportInPrivilegedApi", "org.foo");
  }

  @Test
  public void noPrivilegedExportPackageFromModule() throws Exception {
    Map<String, List<String>> logs = buildMultiModule("noPrivilegedExportPackageFromModule");

    List<String> barLog = logs.get(BAR_MODULE_ID);
    assertAnalyzedClasses(barLog, PATH_CLASS_B);
    assertValidModuleApi(barLog);

    List<String> fooLog = logs.get(FOO_MODULE_ID);
    assertAnalyzedClasses(fooLog, ANALYZED_CLASSES_A_B);
    assertValidModuleApi(fooLog);
  }

  @Test
  public void redundantPrivilegedExportPackageFromModule() throws Exception {
    doDuplicatedPrivilegedExportTest("redundantPrivilegedExportPackageFromModule", BAR_PACKAGE);
  }
}
