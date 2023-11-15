/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.generate;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.ModuleLayer.boot;
import static java.lang.ModuleLayer.defineModulesWithOneLoader;
import static java.lang.module.ModuleDescriptor.Requires.Modifier.TRANSITIVE;
import static java.lang.module.ModuleFinder.ofSystem;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

import org.mule.tools.maven.plugin.module.bean.ServiceDefinition;

import java.io.IOException;
import java.lang.ModuleLayer.Controller;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates mule-module.properties file form module-info.java if present.
 */
@Mojo(name = "generate", requiresDependencyResolution = COMPILE, threadSafe = true, defaultPhase = PROCESS_CLASSES)
public class GenerateMojo extends org.apache.maven.plugin.AbstractMojo {

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      doExecute();
    } catch (MojoExecutionException | MojoFailureException e) {
      throw e;
    } catch (Exception e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

  public void doExecute() throws MojoFailureException, Exception {
    final Module currentModule = resolveCurrentModule();
    final org.mule.tools.maven.plugin.module.bean.Module muleModule = toMuleModule(currentModule);

    // TODO generate and save .properties
    // TODO generate service descriptors

    System.out.println(muleModule);
  }

  private org.mule.tools.maven.plugin.module.bean.Module toMuleModule(final Module currentModule) throws ClassNotFoundException {
    final PrivilegedApiReflectiveWrapper privilegedApiReflectiveWrapper = new PrivilegedApiReflectiveWrapper(currentModule);

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
          serviceDefinition.setServiceImplementations(provides.providers());
          return serviceDefinition;
        })
        .collect(toCollection(TreeSet::new));

    return new org.mule.tools.maven.plugin.module.bean.Module(currentModule.getName(),
                                                              exportedPackages,
                                                              exportedPrivilegedPackages,
                                                              emptySet(),
                                                              modulePrivilegedArtifactIds,
                                                              moduleServiceDefinitions);
  }

  private Set<String> resolveExportedPackages(final Module currentModule, final Set<String> exportedPrivilegedPackages) {
    final Set<String> exportedPackages = currentModule.getPackages()
        .stream()
        .filter(not(exportedPrivilegedPackages::contains))
        .filter(pkg -> currentModule.isExported(pkg))
        .collect(toCollection(TreeSet::new));

    exportedPackages.addAll(currentModule
        .getDescriptor().requires()
        .stream()
        .filter(req -> req.modifiers().contains(TRANSITIVE))
        .filter(req -> currentModule.getLayer().findModule(req.name())
            .map(reqMod -> {
              try {
                return reqMod.getResourceAsStream("/META-INF/mule-module.properties") == null;
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .orElse(false))
        .map(req -> currentModule.getLayer().findModule(req.name()).get())
        .flatMap(transitiveNonMuleModule -> resolveExportedPackages(transitiveNonMuleModule, exportedPrivilegedPackages).stream())
        .collect(toList()));

    return exportedPackages;
  }

  private Module resolveCurrentModule() throws MojoFailureException {
    try {
      final ModuleFinder finder = ModuleFinder.of(project.getCompileClasspathElements()
          .stream()
          .map(cpe -> Paths.get(cpe))
          .toArray(Path[]::new));
      final List<String> roots = finder.findAll()
          .stream()
          .map(moduleRef -> moduleRef.descriptor().name())
          .collect(toList());

      final Configuration configuration = boot().configuration().resolve(finder, ofSystem(), roots);

      Controller controller = defineModulesWithOneLoader(configuration,
                                                         singletonList(boot()),
                                                         getSystemClassLoader());

      final String currentModuleName = finder.findAll()
          .stream()
          .filter(modRef -> modRef.location().map(loc -> !loc.toString().endsWith(".jar")).orElse(false))
          .map(modRef -> modRef.descriptor().name())
          .findFirst()
          .get();
      return controller.layer()
          .findModule(currentModuleName)
          .get();
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

}
