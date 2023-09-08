/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.integration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.maven.plugin.module.analyze.DefaultModuleApiAnalyzer.buildRemovedJrePackageMessage;
import static org.mule.tools.maven.plugin.module.analyze.DefaultModuleApiAnalyzer.buildRemovedSunPackageMessage;

import java.util.List;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class MiscellaneousTestCase extends AbstractExportTestCase {

  public MiscellaneousTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "miscellaneous");
  }

  @Test
  public void redundantExportedPackage() throws Exception {
    doMissingAnalyzedPackageTest("redundantExportedPackage");
  }

  @Test
  public void ignoresJreExportedPackage() throws Exception {
    List<String> log = buildSingleModule("ignoresJreExportedPackage");

    assertValidModuleApi(log);
    assertThat(log, hasItem(containsString(buildRemovedJrePackageMessage("org.w3c.dom"))));
    assertThat(log, hasItem(containsString(buildRemovedSunPackageMessage("com.sun.corba.se.pept.transport"))));
    assertThat(log, hasItem(containsString(buildRemovedSunPackageMessage("sun.misc"))));
  }
}
