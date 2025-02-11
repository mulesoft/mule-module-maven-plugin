/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.jpms;

import static org.mule.tools.maven.plugin.module.analyze.impl.mms.MuleModuleSystemModuleFactory.MULE_MODULE_PROPERTIES_LOCATION;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.ModuleLayer.boot;
import static java.lang.ModuleLayer.defineModulesWithOneLoader;
import static java.lang.module.ModuleDescriptor.Requires.Modifier.TRANSITIVE;
import static java.lang.module.ModuleFinder.ofSystem;
import static java.util.Collections.singletonList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static org.apache.maven.artifact.Artifact.SCOPE_TEST;

import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.common.DefaultModule;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzerException;
import org.mule.tools.maven.plugin.module.analyze.impl.common.ServiceDefinition;
import org.mule.tools.maven.plugin.module.generate.mms.PrivilegedApiReflectiveWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Discovers the Mule modules used on the Maven project under analysis
 *
 * @since 1.0
 */
public class JavaModuleSystemModuleDiscoverer {

  /**
   * Discovers the module corresponding to the Maven project under analysis
   *
   * @param project        project being analyzed.
   * @param analyzerLogger collects all the logging information generated during the project analysis
   * @return a module corresponding to the project being analyzed, null if the project is not a Mule module
   * @throws ModuleApiAnalyzerException
   */
  public Optional<Module> discoverProjectModule(MavenProject project, ModuleLogger analyzerLogger)
      throws Exception {
    return resolveCurrentModule(project, analyzerLogger);
  }

  /**
   * Discovers all the Mule modules used as dependencies on the Maven project under analysis
   *
   * @param project           project being analyzed.
   * @param analyzerLogger    collects all the logging information generated during the project analysis
   * @param projectModuleName name of the module that corresponds to the project being analyzed
   * @return a list containing all the Mule modules that are dependencies of the analyzed project.
   * @throws ModuleApiAnalyzerException
   */
  public List<DefaultModule> discoverExternalModules(MavenProject project, ModuleLogger analyzerLogger,
                                                     String projectModuleName)
      throws ModuleApiAnalyzerException {
    // TODO: Implement (probably not needed).
    return null;
  }

  private Optional<Module> resolveCurrentModule(MavenProject project, ModuleLogger analyzerLogger) throws Exception {
    try {
      final ModuleFinder currentModuleFinder = ModuleFinder.of(Path.of(project.getBuild().getOutputDirectory()));
      final List<String> roots = currentModuleFinder.findAll()
          .stream()
          .map(moduleRef -> moduleRef.descriptor().name())
          .collect(toList());

      final Optional<Path> xmlApisPath = Optional.ofNullable(project.getArtifactMap().get("xml-apis:xml-apis"))
          .map(xmlApisArtifact -> xmlApisArtifact.getFile().toPath());

      // Include test dependencies overriding a transitive compile dependency
      Collection<String> directTestDependencies = project.getDependencies()
          .stream()
          .filter(dependency -> SCOPE_TEST.equals(dependency.getScope()))
          .filter(dependency -> "jar".equals(dependency.getType()))
          .filter(dependency -> !"tests".equals(dependency.getClassifier()))
          .map(dependency -> project.getArtifactMap().get(dependency.getGroupId() + ":" + dependency.getArtifactId()))
          .map(artifact -> artifact.getFile().getAbsolutePath())
          .collect(toSet());

      final ModuleFinder finder = ModuleFinder.of(Stream.concat(project.getCompileClasspathElements()
          .stream(), directTestDependencies.stream())
          .map(Paths::get)
          // Do not use xml-apis which causes a split package with the xml module in the JVM
          .filter(path -> !xmlApisPath.map(path::equals).orElse(false))
          .toArray(Path[]::new));

      final Configuration configuration = boot().configuration().resolve(finder, ofSystem(), roots);

      ModuleLayer.Controller controller = defineModulesWithOneLoader(configuration,
                                                                     singletonList(boot()),
                                                                     getSystemClassLoader());

      return finder.findAll()
          .stream()
          .filter(modRef -> roots.contains(modRef.descriptor().name()))
          .map(modRef -> modRef.descriptor().name())
          .findFirst()
          .flatMap(controller.layer()::findModule)
          .map(module -> toMuleModule(project, module));

    } catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

  private Module toMuleModule(MavenProject project, final java.lang.Module currentModule) {
    final PrivilegedApiReflectiveWrapper privilegedApiReflectiveWrapper = new PrivilegedApiReflectiveWrapper(currentModule);

    final Set<String> optionalPackages = Stream.of(privilegedApiReflectiveWrapper.getOptionalPackages())
        .collect(toCollection(TreeSet::new));

    final Set<String> exportedPrivilegedPackages = Stream.of(privilegedApiReflectiveWrapper.getPrivilegedPackages())
        .collect(toCollection(TreeSet::new));

    final Set<String> modulePrivilegedArtifactIds = Stream.of(privilegedApiReflectiveWrapper.getPrivilegedArtifactIds())
        .collect(toCollection(TreeSet::new));

    final Set<String> exportedPackages = resolveExportedPackages(currentModule, exportedPrivilegedPackages);

    final TreeSet<ServiceDefinition> moduleServiceDefinitions = currentModule.getDescriptor().provides()
        .stream()
        .map(provides -> {
          final ServiceDefinition serviceDefinition = new ServiceDefinition();
          serviceDefinition.setServiceInterface(provides.service());
          serviceDefinition.setServiceImplementations(new ArrayList<>(provides.providers()));
          return serviceDefinition;
        })
        .collect(toCollection(TreeSet::new));

    return new DefaultModule(currentModule.getName(),
                             exportedPackages,
                             exportedPrivilegedPackages,
                             optionalPackages,
                             modulePrivilegedArtifactIds,
                             moduleServiceDefinitions);

  }

  private Set<String> resolveExportedPackages(final java.lang.Module currentModule,
                                              final Set<String> exportedPrivilegedPackages) {
    final Set<String> exportedPackages = currentModule.getPackages()
        .stream()
        .filter(not(exportedPrivilegedPackages::contains))
        .filter(currentModule::isExported)
        .collect(toCollection(TreeSet::new));

    exportedPackages.addAll(currentModule
        .getDescriptor().requires()
        .stream()
        .filter(req -> req.modifiers().contains(TRANSITIVE))
        // only take into account modules brought by the artifact being built, not the ones from the jvm
        .filter(req -> boot().findModule(req.name()).isEmpty())
        .filter(not(req -> isMuleModuleRequired(currentModule, req)))
        .map(req -> currentModule.getLayer().findModule(req.name()).get())
        .flatMap(transitiveNonMuleModule -> resolveExportedPackages(transitiveNonMuleModule, exportedPrivilegedPackages).stream())
        .toList());

    return exportedPackages;
  }

  private Boolean isMuleModuleRequired(final java.lang.Module currentModule, ModuleDescriptor.Requires req) {
    return currentModule.getLayer().findModule(req.name())
        .map(reqMod -> {
          try (InputStream muleModuleSystemDescriptor = reqMod.getResourceAsStream(MULE_MODULE_PROPERTIES_LOCATION)) {
            return muleModuleSystemDescriptor != null;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .orElse(false);
  }

}
