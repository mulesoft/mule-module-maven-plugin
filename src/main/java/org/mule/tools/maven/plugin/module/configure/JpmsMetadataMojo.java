/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.configure;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.jpms.DefaultJavaModuleSystemApiAnalyzer.PROJECT_IS_NOT_A_JPMS_MODULE;
import static org.mule.tools.maven.plugin.module.util.JpmsModule.findTransitiveDependencies;

import static java.lang.String.format;

import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

import org.apache.maven.artifact.Artifact;
import org.mule.tools.maven.plugin.module.AbstractModuleMojo;
import org.mule.tools.maven.plugin.module.analyze.impl.logging.SilentAnalyzerLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.logging.VerboseAnalyzerLogger;
import org.mule.tools.maven.plugin.module.util.JpmsModule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "jpmsMetadataGenerator", requiresDependencyResolution = TEST, threadSafe = true, defaultPhase = PROCESS_CLASSES)
public class JpmsMetadataMojo extends AbstractModuleMojo {

  public static final String REV_API_CONFIG_FILE_NAME = "jpms-module.metadata";
  private static final String IMPLIED_READS_CONFIGURATION_TEMPLATE =
      "{\"impliedReads\": %s}";
  public static final String IMPLIED_READ_CONFIGURATION_TEMPLATE =
      "[{\"impliedRead\": {\"artifactId\": \"%s\", \"artifactVersion\": \"%s\", \"artifactFile\": \"%s\"}}]";

  /**
   * Skip plugin execution completely. Defaults to false.
   */
  @Parameter(property = "muleModule.jpmsMetadataGenerator.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Logs extra information about the plugin execution. Defaults to false.
   */
  @Parameter(property = "muleModule.jpmsMetadataGenerator.verbose", defaultValue = "false")
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
      generateJpmsMetadata();
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException(e);
    }
  }

  private void generateJpmsMetadata() throws MojoFailureException, DependencyResolutionRequiredException {
    Optional<java.lang.Module> projectModule =
        JpmsModule.discoverProjectModule(project, verbose ? new VerboseAnalyzerLogger(getLog()) : new SilentAnalyzerLogger());
    if (projectModule.isEmpty()) {
      getLog().info(PROJECT_IS_NOT_A_JPMS_MODULE);
    } else {
      try (final BufferedWriter jpmsModuleMetadataWriter =
          new BufferedWriter(new FileWriter(new File(project.getBuild().getOutputDirectory(),
                                                     "META-INF/" + REV_API_CONFIG_FILE_NAME)))) {
        Set<String> transitiveDependenciesPaths =
            findTransitiveDependencies(project, projectModule.get()).stream().map(URI::getPath).collect(Collectors.toSet());
        List<Artifact> transitiveArtifacts = project.getArtifacts().stream()
            .filter(artifact -> transitiveDependenciesPaths.contains(artifact.getFile().getAbsolutePath())).toList();
        if (transitiveArtifacts.size() != transitiveDependenciesPaths.size()) {
          throw new RuntimeException(format("Could not resolve all the identified artifacts. Expected: %s - Resolved: %s",
                                            transitiveDependenciesPaths, transitiveArtifacts));
        }
        jpmsModuleMetadataWriter.write(format(IMPLIED_READS_CONFIGURATION_TEMPLATE, transitiveArtifacts
            .stream()
            .map(transitiveArtifact -> format(IMPLIED_READ_CONFIGURATION_TEMPLATE, transitiveArtifact.getArtifactId(),
                                              transitiveArtifact.getVersion(), transitiveArtifact.getFile().getName()))
            .collect(Collectors.joining(","))));
      } catch (IOException e) {
        throw new RuntimeException("Error while trying to write jpms module metadata", e);
      }
    }
  }

  public boolean isSkip() {
    return skip;
  }
}
