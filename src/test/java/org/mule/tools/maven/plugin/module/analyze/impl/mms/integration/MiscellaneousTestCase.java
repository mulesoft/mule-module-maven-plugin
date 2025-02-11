/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.mms.integration;

import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.DefaultMuleModuleSystemApiAnalyzer.buildRemovedJrePackageMessage;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.DefaultMuleModuleSystemApiAnalyzer.buildRemovedSunPackageMessage;

import static org.apache.commons.lang3.JavaVersion.JAVA_11;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;

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
    assumeFalse(isJavaVersionAtLeast(JAVA_11));

    List<String> log = buildSingleModule("ignoresJreExportedPackage");

    assertValidModuleApi(log);
    assertThat(log, hasItem(containsString(buildRemovedJrePackageMessage("org.w3c.dom"))));
    assertThat(log, hasItem(containsString(buildRemovedSunPackageMessage("com.sun.corba.se.pept.transport"))));
    assertThat(log, hasItem(containsString(buildRemovedSunPackageMessage("sun.misc"))));
  }

  @Test
  public void exportedServices() throws Exception {
    List<String> log = buildSingleModule("services");

    assertValidModuleApi(log);
  }

  @Test
  public void invalidExportedServices() throws Exception {
    List<String> log = buildSingleModule("servicesInvalid");

    assertThat(log, hasItem(containsString("Cannot analyze module API: Cannot read project's mule-module.properties: "
        + "Invalid service definition 'notAServiceDefinition'. Must be of format '<interface fqcn>:<implementation fqcn>")));
  }

  @Test
  public void emptyExportedServices() throws Exception {
    List<String> log = buildSingleModule("servicesEmpty");

    assertValidModuleApi(log);
  }
}
