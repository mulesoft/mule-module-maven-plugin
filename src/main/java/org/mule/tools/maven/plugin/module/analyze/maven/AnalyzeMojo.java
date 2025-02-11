/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.maven;

import static java.lang.System.lineSeparator;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

import org.mule.tools.maven.plugin.module.analyze.impl.jpms.JavaModuleSystemApiAnalyzer;
import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;

import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzerException;
import org.mule.tools.maven.plugin.module.analyze.api.ProjectAnalysisResult;
import org.mule.tools.maven.plugin.module.analyze.impl.common.logging.SilentAnalyzerLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.common.logging.VerboseAnalyzerLogger;

/**
 * Analyzes the exported API in a mule module and checks that there are no missing exported packages.
 */
@Mojo(name = "analyze", requiresDependencyResolution = TEST, threadSafe = true, defaultPhase = VALIDATE)
public class AnalyzeMojo extends AbstractModuleMojo {

  public static final String NO_MODULE_API_PROBLEMS_FOUND = "No module API problems found";
  public static final String MODULE_API_PROBLEMS_FOUND = "Module API problems found";
  public static final String PACKAGES_TO_EXPORT_ERROR = "Packages that must be exported:";
  public static final String PRIVILEGED_PACKAGES_TO_EXPORT_ERROR = "Privileged packages that must be exported:";
  public static final String NOT_ANALYZED_PACKAGES_ERROR = "Following packages were not analyzed:";
  public static final String NOT_ANALYZED_PRIVILEGED_PACKAGES_ERROR = "Following privileged packages were not analyzed:";
  public static final String DUPLICATED_EXPORTED_PACKAGES = "Following packages are already exported by a module dependency:";
  public static final String DUPLICATED_PRIVILEGED_EXPORTED_PACKAGES =
      "Following privileged packages are already exported by a module dependency:";

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

  /**
   * Workaround for 3rd party modules that do not export packages that are indeed part of their API.<br>
   * When set to true, such un-exported packages will be added to the {@link Module#getOptionalExportedPackages()} as if they were
   * part of the API.
   */
  @Parameter(defaultValue = "false")
  private boolean fillOptionalPackages;

  @Parameter(property = "muleModule.analyze.moduleSystem", defaultValue = "mms")
  private String moduleSystem;

  /*
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
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

    // Parameters can be injected into maven Mojos only, so we need this initialization (sad).
    if (fillOptionalPackages && moduleSystem.equals("jpms")) {
      ((JavaModuleSystemApiAnalyzer) analyzer.get(moduleSystem)).setIsFillOptionalPackages(fillOptionalPackages);
    }

    boolean error = checkModuleApi();

    if (error) {
      throw new MojoExecutionException(MODULE_API_PROBLEMS_FOUND);
    } else {
      getLog().info(NO_MODULE_API_PROBLEMS_FOUND);
    }
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  private boolean checkModuleApi() throws MojoExecutionException {
    ProjectAnalysisResult analysis;
    try {
      final ModuleLogger analyzerLogger = verbose ? new VerboseAnalyzerLogger(getLog()) : new SilentAnalyzerLogger();
      analysis = analyzer.get(moduleSystem).analyze(project, analyzerLogger, getLog());
    } catch (ModuleApiAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze module API", exception);
    }

    boolean stardardApiError = false;
    boolean privilegedApiError = false;

    if (analysis.getStandardApi() != null) {
      stardardApiError = processStandardApiResult(analysis);
    }

    if (analysis.getPrivilegedAPi() != null) {
      privilegedApiError = processPrivilegedApiResult(analysis);
    }

    return stardardApiError || privilegedApiError;
  }

  private boolean processStandardApiResult(ProjectAnalysisResult analysis) {
    boolean error = false;

    final Map<String, Set<String>> undeclaredExportedPackages = analysis.getStandardApi().getUndeclaredPackageDeps();
    final Set<String> packagesToExport = analysis.getStandardApi().getPackagesToExport();
    final Set<String> noAnalyzedPackages = analysis.getStandardApi().getNotAnalyzedPackages();
    final Set<String> duplicatedPackages = analysis.getStandardApi().getDuplicatedPackages();

    if (!undeclaredExportedPackages.isEmpty()) {
      getLog().info("Used undeclared exported packages found:");

      if (verbose) {
        logUnExportedDependenciesPerPackage(analysis.getStandardApi().getUndeclaredPackageDeps());
      }
      error = true;
    }

    if (!noAnalyzedPackages.isEmpty()) {
      getLog().info(buildPackageErrorMessage(NOT_ANALYZED_PACKAGES_ERROR, noAnalyzedPackages));
      error = true;
    }

    if (!packagesToExport.isEmpty()) {
      getLog().info(buildPackageErrorMessage(PACKAGES_TO_EXPORT_ERROR, packagesToExport));
      error = true;
    }

    if (!duplicatedPackages.isEmpty()) {
      getLog().info(buildPackageErrorMessage(DUPLICATED_EXPORTED_PACKAGES, duplicatedPackages));
      error = true;
    }

    return error;
  }

  private boolean processPrivilegedApiResult(ProjectAnalysisResult analysis) {
    boolean error = false;

    final Map<String, Set<String>> undeclaredExportedPackages = analysis.getPrivilegedAPi().getUndeclaredPackageDeps();
    final Set<String> packagesToExport = analysis.getPrivilegedAPi().getPackagesToExport();
    final Set<String> noAnalyzedPackages = analysis.getPrivilegedAPi().getNotAnalyzedPackages();
    final Set<String> duplicatedPackages = analysis.getPrivilegedAPi().getDuplicatedPackages();

    if (!undeclaredExportedPackages.isEmpty()) {
      getLog().info("Used undeclared privileged exported packages found:");

      if (verbose) {
        logUnExportedDependenciesPerPackage(analysis.getPrivilegedAPi().getUndeclaredPackageDeps());
      }
      error = true;
    }

    if (!noAnalyzedPackages.isEmpty()) {
      getLog().info(buildPackageErrorMessage(NOT_ANALYZED_PRIVILEGED_PACKAGES_ERROR, noAnalyzedPackages));
      error = true;
    }

    if (!packagesToExport.isEmpty()) {
      getLog().info(buildPackageErrorMessage(PRIVILEGED_PACKAGES_TO_EXPORT_ERROR, packagesToExport));
      error = true;
    }

    if (!duplicatedPackages.isEmpty()) {
      getLog().info(buildPackageErrorMessage(DUPLICATED_PRIVILEGED_EXPORTED_PACKAGES, duplicatedPackages));
      error = true;
    }

    return error;
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

  private static String buildPackageErrorMessage(String message, Set<String> packageNames) {
    StringBuilder builder = new StringBuilder();
    builder.append(message);

    for (String packageName : packageNames) {
      builder.append(lineSeparator()).append(packageName);
    }
    return builder.toString();
  }

}
