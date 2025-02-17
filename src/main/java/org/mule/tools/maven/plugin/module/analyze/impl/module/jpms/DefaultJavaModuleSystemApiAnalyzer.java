/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.module.jpms;

import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzer;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.BaseModuleApiAnalyzer;
import org.mule.tools.maven.plugin.module.analyze.impl.module.DefaultModule;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzerException;
import org.mule.tools.maven.plugin.module.analyze.api.ProjectAnalysisResult;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.sisu.Typed;
import org.mule.tools.maven.plugin.module.analyze.impl.logging.SilentAnalyzerLogger;

import java.util.Optional;
import java.util.Set;

@Named("jpms")
@Singleton
@Typed(ModuleApiAnalyzer.class)
public class DefaultJavaModuleSystemApiAnalyzer extends BaseModuleApiAnalyzer implements JavaModuleSystemApiAnalyzer {

  public static final String PROJECT_IS_NOT_A_JPMS_MODULE = "Project is not a jpms module";
  private boolean isFillOptionalPackages = false;

  private final JavaModuleSystemModuleDiscoverer javaModuleSystemModuleDiscoverer = new JavaModuleSystemModuleDiscoverer();

  @Override
  public ProjectAnalysisResult analyze(MavenProject project, ModuleLogger analyzerLogger, Log log)
      throws ModuleApiAnalyzerException {
    Optional<Module> module;
    try {
      module = javaModuleSystemModuleDiscoverer.discoverProjectModule(project, analyzerLogger);
      if (module.isEmpty()) {
        log.info(PROJECT_IS_NOT_A_JPMS_MODULE);
        return new ProjectAnalysisResult(null, null);
      }
      if (isFillOptionalPackages) {
        final ProjectAnalysisResult analysis = this
            .analyze(project, module.get(), new SilentAnalyzerLogger(), log);
        final Set<String> additionalOptionalPackages = analysis.getStandardApi().getPackagesToExport();
        if (!additionalOptionalPackages.isEmpty()) {
          Module moduleFound = module.get();
          additionalOptionalPackages.addAll(moduleFound.getOptionalExportedPackages());
          module = Optional.of(new DefaultModule(module.get().getName(),
                                                 moduleFound.getExportedPackages(),
                                                 moduleFound.getExportedPrivilegedPackages(),
                                                 additionalOptionalPackages,
                                                 moduleFound.getModulePrivilegedArtifactIds(),
                                                 moduleFound.getModuleServiceDefinitions()));
        }
      }
      return analyze(project, module.get(), analyzerLogger, log);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setIsFillOptionalPackages(boolean isFillOptionalPackages) {
    this.isFillOptionalPackages = isFillOptionalPackages;
  }
}
