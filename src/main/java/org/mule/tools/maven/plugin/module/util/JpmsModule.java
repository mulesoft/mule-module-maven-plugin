/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.util;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.ModuleLayer.defineModulesWithOneLoader;
import static java.lang.module.ModuleFinder.ofSystem;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.maven.artifact.Artifact.SCOPE_TEST;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.MULE_MODULE_PROPERTIES_LOCATION;

import static java.lang.ModuleLayer.boot;
import static java.lang.module.ModuleDescriptor.Requires.Modifier.TRANSITIVE;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.module.DefaultModule;
import org.mule.tools.maven.plugin.module.analyze.impl.module.ServiceDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class JpmsModule {

  // TODO: Add logging.
  public static Optional<java.lang.Module> discoverProjectModule(MavenProject project, ModuleLogger analyzerLogger)
      throws MojoFailureException {
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
          .flatMap(controller.layer()::findModule);

    } catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

  public static Module toMuleModule(final java.lang.Module jpmsModule) {
    final PrivilegedApiReflectiveWrapper privilegedApiReflectiveWrapper = new PrivilegedApiReflectiveWrapper(jpmsModule);

    final Set<String> optionalPackages = Stream.of(privilegedApiReflectiveWrapper.getOptionalPackages())
        .collect(toCollection(TreeSet::new));

    final Set<String> exportedPrivilegedPackages = Stream.of(privilegedApiReflectiveWrapper.getPrivilegedPackages())
        .collect(toCollection(TreeSet::new));

    final Set<String> modulePrivilegedArtifactIds = Stream.of(privilegedApiReflectiveWrapper.getPrivilegedArtifactIds())
        .collect(toCollection(TreeSet::new));

    final Set<String> exportedPackages = resolveExportedPackages(jpmsModule, exportedPrivilegedPackages);

    final TreeSet<ServiceDefinition> moduleServiceDefinitions = jpmsModule.getDescriptor().provides()
        .stream()
        .map(provides -> {
          final ServiceDefinition serviceDefinition = new ServiceDefinition();
          serviceDefinition.setServiceInterface(provides.service());
          serviceDefinition.setServiceImplementations(new ArrayList<>(provides.providers()));
          return serviceDefinition;
        })
        .collect(toCollection(TreeSet::new));

    return new DefaultModule(jpmsModule.getName(),
                             exportedPackages,
                             exportedPrivilegedPackages,
                             optionalPackages,
                             modulePrivilegedArtifactIds,
                             moduleServiceDefinitions);

  }

  public static Set<String> resolveExportedPackages(final java.lang.Module module,
                                                    final Set<String> exportedPrivilegedPackages) {
    final Set<String> exportedPackages = module.getPackages()
        .stream()
        .filter(not(exportedPrivilegedPackages::contains))
        .filter(module::isExported)
        .collect(toCollection(TreeSet::new));

    exportedPackages.addAll(module
        .getDescriptor().requires()
        .stream()
        .filter(req -> req.modifiers().contains(TRANSITIVE))
        // only take into account modules brought by the artifact being built, not the ones from the jvm
        .filter(req -> boot().findModule(req.name()).isEmpty())
        .filter(not(req -> isMuleModuleRequired(module, req)))
        .map(req -> module.getLayer().findModule(req.name()).get())
        .flatMap(transitiveNonMuleModule -> resolveExportedPackages(transitiveNonMuleModule, exportedPrivilegedPackages).stream())
        .toList());

    return exportedPackages;
  }

  /**
   * Analyzes the given module dependencies, returning all the transitive ones.<br>
   * 
   * @param module The module whose dependencies will be analyzed.
   * @return All the module transitive dependencies.
   */
  public static Set<URI> findTransitiveDependencies(MavenProject project, final java.lang.Module module)
      throws DependencyResolutionRequiredException {
    Set<ModuleDescriptor> transitiveDependencies = module
        .getDescriptor().requires()
        .stream()
        .filter(req -> req.modifiers().contains(TRANSITIVE))
        // Only take into account modules brought by the artifact being built, not the ones from the jvm
        .filter(req -> boot().findModule(req.name()).isEmpty())
        .filter(not(req -> isMuleModuleRequired(module, req)))
        .map(req -> module.getLayer().findModule(req.name()).get())
        .map(java.lang.Module::getDescriptor)
        .collect(toSet());

    final ModuleFinder finder = ModuleFinder.of(project.getCompileClasspathElements().stream()
        .map(Paths::get)
        .toArray(Path[]::new));

    return finder.findAll().stream().filter(moduleReference -> transitiveDependencies.contains(moduleReference.descriptor()))
        .map(ModuleReference::location).filter(Optional::isPresent).map(Optional::get).collect(toSet());
  }

  private static Boolean isMuleModuleRequired(final java.lang.Module currentModule, ModuleDescriptor.Requires req) {
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
