/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.PACKAGES_TO_EXPORT_ERROR;
import static org.mule.tools.maven.plugin.module.analyze.DefaultModuleApiAnalyzer.PROJECT_IS_NOT_A_MULE_MODULE;
import static org.mule.tools.maven.plugin.module.analyze.DefaultModuleApiAnalyzer.buildOptionalPackageExportedMessage;

import java.util.List;
import java.util.Map;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class ModuleTestCase extends AbstractExportTestCase {


  private static final String BAR_LIBRARY_ID = "Bar Library 1.0-SNAPSHOT";

  public ModuleTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "module");
  }

  @Test
  public void exportsPackageFromLibrary() throws Exception {
    Map<String, List<String>> logs = buildMultiModule("exportsPackageFromLibrary");

    List<String> barLog = logs.get(BAR_LIBRARY_ID);
    assertThat(barLog, hasItem(INFO_LOG_PREFIX + PROJECT_IS_NOT_A_MULE_MODULE));

    List<String> fooLog = logs.get(FOO_MODULE_ID);
    assertAnalyzedClasses(fooLog, ANALYZED_CLASSES_A_B);
    assertValidModuleApi(fooLog);
  }

  @Test
  public void missingExportsPackageFromLibrary() throws Exception {
    Map<String, List<String>> logs = buildMultiModule("missingExportsPackageFromLibrary");

    List<String> barLog = logs.get(BAR_LIBRARY_ID);
    assertThat(barLog, hasItem(INFO_LOG_PREFIX + PROJECT_IS_NOT_A_MULE_MODULE));

    List<String> fooLog = logs.get(FOO_MODULE_ID);
    assertAnalyzedClasses(fooLog, ANALYZED_CLASSES_A_B);
    assertMissingExportedPackages(fooLog, PACKAGES_TO_EXPORT_ERROR, BAR_PACKAGE);
  }

  @Test
  public void noExportsPackageFromModule() throws Exception {
    Map<String, List<String>> logs = buildMultiModule("noExportsPackageFromModule");

    List<String> barLog = logs.get(BAR_MODULE_ID);
    assertAnalyzedClasses(barLog, PATH_CLASS_B);
    assertValidModuleApi(barLog);

    List<String> fooLog = logs.get(FOO_MODULE_ID);
    assertAnalyzedClasses(fooLog, ANALYZED_CLASSES_A_B);
    assertValidModuleApi(fooLog);
  }

  @Test
  public void redundantExportPackageFromModule() throws Exception {
    doDuplicatedExportTest("redundantExportPackageFromModule", BAR_PACKAGE);
  }

  @Test
  public void ignoresOptionalPackageFromLibrary() throws Exception {
    Map<String, List<String>> logs = buildMultiModule("ignoresOptionalPackageFromLibrary");

    List<String> barLog = logs.get(BAR_LIBRARY_ID);
    assertThat(barLog, hasItem(INFO_LOG_PREFIX + PROJECT_IS_NOT_A_MULE_MODULE));

    List<String> fooLog = logs.get(FOO_MODULE_ID);
    assertAnalyzedClasses(fooLog, ANALYZED_CLASSES_A_B);
    assertValidModuleApi(fooLog);
  }

  @Test
  public void detectsExportedOptionalPackageFromLibrary() throws Exception {
    Map<String, List<String>> logs = buildMultiModule("detectsExportedOptionalPackageFromLibrary");

    List<String> barLog = logs.get(BAR_LIBRARY_ID);
    assertThat(barLog, hasItem(INFO_LOG_PREFIX + PROJECT_IS_NOT_A_MULE_MODULE));

    List<String> fooLog = logs.get(FOO_MODULE_ID);
    assertThat(fooLog, hasItem(containsString(buildOptionalPackageExportedMessage(singletonList(BAR_PACKAGE)))));
  }
}
