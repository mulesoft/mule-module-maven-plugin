/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * Analyzes the exported API in a mule module and checks that there are no missing
 * exported packages.
 */
@Mojo(name = "analyze", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class AnalyzeMojo extends AbstractMojo implements Contextualizable {

  public static final String NO_MODULE_API_PROBLEMS_FOUND = "No module API problems found";
  public static final String MODULE_API_PROBLEMS_FOUND = "Module API problems found";

  /**
   * The plexus context to look-up the right {@link ModuleApiAnalyzer} implementation depending on the mojo
   * configuration.
   */
  private Context context;

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /**
   * Logs extra information about analysis process
   */
  @Parameter(property = "muleModule.analyze.verbose", defaultValue = "false")
  private boolean verbose;

  /**
   * Skip plugin execution completely.
   */
  @Parameter(property = "muleModule.analyze.skip", defaultValue = "false")
  private boolean skip;

  /*
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute()
      throws MojoExecutionException, MojoFailureException {
    if (isSkip()) {
      getLog().info("Skipping plugin execution");
      return;
    }

    if ("pom".equals(project.getPackaging())) {
      getLog().info("Skipping pom project");
      return;
    }

    boolean error = checkDependencies();

    if (error) {
      throw new MojoExecutionException(MODULE_API_PROBLEMS_FOUND);
    }
  }

  protected ModuleApiAnalyzer createProjectDependencyAnalyzer() throws MojoExecutionException {
    final String role = ModuleApiAnalyzer.ROLE;
    final String roleHint = "default";

    try {
      final PlexusContainer container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);

      return (ModuleApiAnalyzer) container.lookup(role, roleHint);
    } catch (Exception exception) {
      throw new MojoExecutionException(
                                       "Failed to instantiate ModuleApiAnalyzer with role " + role + " / role-hint " + roleHint,
                                       exception);
    }
  }

  public void contextualize(Context context)
      throws ContextException {
    this.context = context;
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  private boolean checkDependencies() throws MojoExecutionException {
    ProjectDependencyAnalysis analysis;
    try {
      final AnalyzerLogger analyzerLogger = verbose ? new VerboseAnalyzerLogger(getLog()) : new SilentAnalyzerLogger();
      analysis = createProjectDependencyAnalyzer().analyze(project, analyzerLogger);
    } catch (ModuleApiAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze module API", exception);
    }

    final HashMap<String, Set<String>> undeclaredExportedPackages =
        new HashMap<String, Set<String>>(analysis.getUndeclaredPackageDeps());
    final HashSet<String> packagesToExport = new HashSet<String>(analysis.getPackagesToExport());

    boolean reported = false;
    boolean warning = false;


    if (!undeclaredExportedPackages.isEmpty()) {
      getLog().info("Used undeclared exported packages found:");

      if (verbose) {
        logUnExportedDependenciesPerPackage(analysis.getUndeclaredPackageDeps());
      }
      reported = true;
      warning = true;
    }

    if (!packagesToExport.isEmpty()) {
      logPackagesToExport(packagesToExport);
      reported = true;
      warning = true;
    }

    if (!reported) {
      getLog().info(NO_MODULE_API_PROBLEMS_FOUND);
    }

    return warning;
  }

  private void logUnExportedDependenciesPerPackage(Map<String, Set<String>> artifacts) {
    if (artifacts.isEmpty()) {
      getLog().info("   None");
    } else {
      for (String exportedPackageName : artifacts.keySet()) {
        StringBuilder builder = new StringBuilder();
        builder.append("Undeclared exported packages for ").append(exportedPackageName).append(":\n");
        final Set<String> undeclaredExportedPackages = artifacts.get(exportedPackageName);
        if (undeclaredExportedPackages == null || undeclaredExportedPackages.isEmpty()) {
          builder.append("NONE");
        } else {
          for (String undeclaredExportedPackage : undeclaredExportedPackages) {
            builder.append(undeclaredExportedPackage).append("\n");
          }

          getLog().info(builder.toString());
        }
      }
    }
  }

  private void logPackagesToExport(Set<String> packageNames) {
    StringBuilder builder = new StringBuilder();
    builder.append("Packages that must be exported:\n");
    if (packageNames.isEmpty()) {
      builder.append("   NONE");
    } else {
      for (String packageName : packageNames) {
        builder.append(packageName).append("\n");
      }
    }
    getLog().info(builder.toString());
  }

}
