/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.configure;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.Mojo;
import org.mule.tools.maven.plugin.module.AbstractModuleMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.maven.plugin.module.analyze.impl.logging.SilentAnalyzerLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.logging.VerboseAnalyzerLogger;
import org.mule.tools.maven.plugin.module.util.JpmsModule;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.jpms.DefaultJavaModuleSystemApiAnalyzer.PROJECT_IS_NOT_A_JPMS_MODULE;

@Mojo(name = "revApiConfiguration", requiresDependencyResolution = TEST, threadSafe = true, defaultPhase = VALIDATE)
public class RevApiConfigurationMojo extends AbstractModuleMojo {

  /**
   * Skip plugin execution completely. Defaults to false.
   */
  @Parameter(property = "muleModule.revapiConfiguration.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Logs extra information about the plugin execution. Defaults to false.
   */
  @Parameter(property = "muleModule.revapiConfiguration.verbose", defaultValue = "false")
  private boolean verbose;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (isSkip()) {
      getLog().info("Skipping plugin execution");
      return;
    }

    if ("pom".equals(project.getPackaging())) {
      getLog().info("Skipping pom project");
      return;
    }

    try {
      configureRevApi();
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException(e);
    }
  }

  private void configureRevApi() throws MojoFailureException, DependencyResolutionRequiredException {
    Optional<java.lang.Module> projectModule =
        JpmsModule.discoverProjectModule(project, verbose ? new VerboseAnalyzerLogger(getLog()) : new SilentAnalyzerLogger());
    if (projectModule.isEmpty()) {
      getLog().info(PROJECT_IS_NOT_A_JPMS_MODULE);
    } else {
      Set<URI> transitiveDependencies = JpmsModule.findTransitiveDependencies(project, projectModule.get());
    }
  }

  public boolean isSkip() {
    return skip;
  }
}
