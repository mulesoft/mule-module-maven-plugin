/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.integration;

import static org.mule.tools.maven.plugin.module.bean.Module.MODULE_NAME;
import static org.mule.tools.maven.plugin.module.bean.Module.MULE_MODULE_PROPERTIES_LOCATION;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.lang3.JavaVersion.JAVA_11;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assume.assumeTrue;

import org.mule.tools.maven.plugin.module.analyze.SilentAnalyzerLogger;
import org.mule.tools.maven.plugin.module.bean.Module;
import org.mule.tools.maven.plugin.module.bean.ModuleFactory;
import org.mule.tools.maven.plugin.module.bean.ServiceDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;

import org.junit.BeforeClass;
import org.junit.Test;

public class GenerateTestCase extends AbstractExportTestCase {

  @BeforeClass
  public static void checkJvmVersion() {
    assumeTrue(isJavaVersionAtLeast(JAVA_11));
  }

  public GenerateTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "generate", "11");
  }

  private MavenExecutionResult doRunMaven(final String string) throws Exception {
    MavenExecutionResult result = runMaven(string, "package", "mule-module:analyze");
    result.assertErrorFreeLog();
    assertThat(result.getLog(), hasItem("[INFO] No module API problems found"));
    return result;
  }

  private Module loadMuleModuleProperties(String submodule, MavenExecutionResult result)
      throws IOException, FileNotFoundException {
    final Properties properties = new Properties();
    try (final FileInputStream inStream =
        new FileInputStream(new File(result.getBasedir(), submodule + "/target/classes/" + MULE_MODULE_PROPERTIES_LOCATION))) {
      properties.load(inStream);
    }

    return new ModuleFactory().create(new SilentAnalyzerLogger(), properties.getProperty(MODULE_NAME), properties);
  }

  @Test
  public void exportedPackages() throws Exception {
    MavenExecutionResult result = doRunMaven("simpleModule");
    final Module muleModule = loadMuleModuleProperties(".", result);

    assertThat(muleModule.getName(), is("org.foo.simple"));
    assertThat(muleModule.getExportedPackages(), containsInAnyOrder("org.foo"));
    assertThat(muleModule.getExportedPrivilegedPackages(), empty());
    assertThat(muleModule.getModulePrivilegedArtifactIds(), empty());
    assertThat(muleModule.getModuleServiceDefinitions(), empty());
    assertThat(muleModule.getOptionalExportedPackages(), empty());
  }

  @Test
  public void somthingOnMetaInf() throws Exception {
    MavenExecutionResult result = doRunMaven("somethingOnMetaInf");
    final Module muleModule = loadMuleModuleProperties(".", result);

    assertThat(muleModule.getName(), is("org.foo.simple"));
    assertThat(muleModule.getExportedPackages(), containsInAnyOrder("org.foo"));
    assertThat(muleModule.getExportedPrivilegedPackages(), empty());
    assertThat(muleModule.getModulePrivilegedArtifactIds(), empty());
    assertThat(muleModule.getModuleServiceDefinitions(), empty());
    assertThat(muleModule.getOptionalExportedPackages(), empty());
  }

  @Test
  public void privilegedPackages() throws Exception {
    MavenExecutionResult result = doRunMaven("privilegedApi");
    final Module muleModule = loadMuleModuleProperties(".", result);

    assertThat(muleModule.getName(), is("org.foo.simple"));
    assertThat(muleModule.getExportedPackages(), empty());
    assertThat(muleModule.getExportedPrivilegedPackages(), containsInAnyOrder("org.foo"));
    assertThat(muleModule.getModulePrivilegedArtifactIds(), containsInAnyOrder("org.bar:some-extension"));
    assertThat(muleModule.getModuleServiceDefinitions(), empty());
    assertThat(muleModule.getOptionalExportedPackages(), empty());
  }

  @Test
  public void requiresTransitiveMuleModule() throws Exception {
    MavenExecutionResult result = doRunMaven("requiresTransitiveMuleModule");
    final Module muleModule =
        loadMuleModuleProperties("mule-module-with-mule-transitive", result);

    assertThat(muleModule.getName(), is("org.bar.simple.wrapper"));
    assertThat(muleModule.getExportedPackages(), containsInAnyOrder("org.bar"));
    assertThat(muleModule.getExportedPrivilegedPackages(), empty());
    assertThat(muleModule.getModulePrivilegedArtifactIds(), empty());
    assertThat(muleModule.getModuleServiceDefinitions(), empty());
    assertThat(muleModule.getOptionalExportedPackages(), empty());
  }

  @Test
  public void requiresTransitiveNonMuleModule() throws Exception {
    MavenExecutionResult result = doRunMaven("requiresTransitiveNonMuleModule");
    final Module muleModule =
        loadMuleModuleProperties("mule-module-with-non-mule-transitive", result);

    assertThat(muleModule.getName(), is("org.bar.simple.wrapper"));
    assertThat(muleModule.getExportedPackages(), containsInAnyOrder("org.foo", "org.bar"));
    assertThat(muleModule.getExportedPrivilegedPackages(), empty());
    assertThat(muleModule.getModulePrivilegedArtifactIds(), empty());
    assertThat(muleModule.getModuleServiceDefinitions(), empty());
    assertThat(muleModule.getOptionalExportedPackages(), empty());
  }

  @Test
  public void services() throws Exception {
    MavenExecutionResult result = doRunMaven("services");
    final Module muleModule =
        loadMuleModuleProperties("module-with-provides", result);

    assertThat(muleModule.getName(), is("org.bar.service"));
    assertThat(muleModule.getExportedPackages(), empty());
    assertThat(muleModule.getExportedPrivilegedPackages(), empty());
    assertThat(muleModule.getModulePrivilegedArtifactIds(), empty());
    assertThat(muleModule.getOptionalExportedPackages(), empty());

    final ServiceDefinition expectedServiceDefinition = new ServiceDefinition();
    expectedServiceDefinition.setServiceInterface("org.foo.A");
    expectedServiceDefinition.setServiceImplementations(asList("org.bar.B"));
    assertThat(muleModule.getModuleServiceDefinitions(), containsInAnyOrder(expectedServiceDefinition));

    try (final FileInputStream inStream =
        new FileInputStream(new File(result.getBasedir(), "module-with-provides/target/classes/META-INF/services/org.foo.A"))) {
      assertThat(readLines(inStream, UTF_8)
          .stream()
          .filter(line -> !line.startsWith("#"))
          .collect(toList()),
                 containsInAnyOrder("org.bar.B"));
    }
  }

}
