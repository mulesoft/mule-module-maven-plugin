/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.generate;

import static org.mule.tools.maven.plugin.module.bean.Module.EXPORT_CLASS_PACKAGES;
import static org.mule.tools.maven.plugin.module.bean.Module.EXPORT_OPTIONAL_PACKAGES;
import static org.mule.tools.maven.plugin.module.bean.Module.EXPORT_SERVICES;
import static org.mule.tools.maven.plugin.module.bean.Module.MODULE_NAME;
import static org.mule.tools.maven.plugin.module.bean.Module.MULE_MODULE_PROPERTIES_LOCATION;
import static org.mule.tools.maven.plugin.module.bean.Module.PRIVILEGED_ARTIFACT_IDS;
import static org.mule.tools.maven.plugin.module.bean.Module.PRIVILEGED_CLASS_PACKAGES;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.ModuleLayer.boot;
import static java.lang.ModuleLayer.defineModulesWithOneLoader;
import static java.lang.module.ModuleDescriptor.Requires.Modifier.TRANSITIVE;
import static java.lang.module.ModuleFinder.ofSystem;
import static java.util.Collections.singletonList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

import org.mule.tools.maven.plugin.module.bean.ServiceDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ModuleLayer.Controller;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates mule-module.properties file form module-info.java if present.
 */
@Mojo(name = "generate", requiresDependencyResolution = COMPILE, threadSafe = true, defaultPhase = PROCESS_CLASSES)
public class GenerateMojo extends AbstractMojo {

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /**
   * Skip plugin execution completely.
   */
  @Parameter(property = "muleModule.generate.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!project.getPackaging().equals("jar")) {
      getLog().info("Project is of type '" + project.getPackaging() + "', not 'jar'. Skipping...");
      return;
    }

    try {
      doExecute();
    } catch (MojoExecutionException | MojoFailureException e) {
      throw e;
    } catch (Exception e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

  public void doExecute() throws MojoFailureException, Exception {
    if (isSkip()) {
      getLog().info("Skipping plugin execution");
      return;
    }

    getLog().info("Resolving current module...");
    final Optional<java.lang.Module> currentModuleOpt = resolveCurrentModule();
    if (currentModuleOpt.isEmpty()) {
      getLog().info("No module found. Skipping...");
      return;
    }
    java.lang.Module currentModule = currentModuleOpt.get();

    getLog().info("Loading module information for '" + currentModule.getName() + "'...");
    final org.mule.tools.maven.plugin.module.bean.Module muleModule = toMuleModule(currentModule);

    getLog().info("Setting properties for mule module '" + currentModule.getName() + "'...");
    final Properties properties = toMuleModuleProperties(muleModule);

    getLog().info("Saving generated files for module '" + currentModule.getName() + "'...");
    writeFiles(muleModule, properties);
  }

  private org.mule.tools.maven.plugin.module.bean.Module toMuleModule(final java.lang.Module currentModule)
      throws ClassNotFoundException {
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
          serviceDefinition.setServiceImplementations(provides.providers());
          return serviceDefinition;
        })
        .collect(toCollection(TreeSet::new));

    return new org.mule.tools.maven.plugin.module.bean.Module(currentModule.getName(),
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
        .filter(pkg -> currentModule.isExported(pkg))
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
        .collect(toList()));

    return exportedPackages;
  }

  private Boolean isMuleModuleRequired(final java.lang.Module currentModule, Requires req) {
    return currentModule.getLayer().findModule(req.name())
        .map(reqMod -> {
          try {
            return reqMod.getResourceAsStream(MULE_MODULE_PROPERTIES_LOCATION) != null;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .orElse(false);
  }

  private Optional<java.lang.Module> resolveCurrentModule() throws MojoFailureException {
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

      return finder.findAll()
          .stream()
          .filter(modRef -> modRef.location().map(loc -> !loc.toString().endsWith(".jar")).orElse(false))
          .map(modRef -> modRef.descriptor().name())
          .findFirst()
          .flatMap(controller.layer()::findModule);
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

  private Properties toMuleModuleProperties(final org.mule.tools.maven.plugin.module.bean.Module muleModule) {
    final Properties properties = new Properties();

    properties.put(MODULE_NAME, muleModule.getName());

    if (!muleModule.getExportedPackages().isEmpty()) {
      properties.put(EXPORT_CLASS_PACKAGES,
                     muleModule.getExportedPackages().stream().collect(joining(",")));
    }

    if (!muleModule.getExportedPrivilegedPackages().isEmpty()) {
      properties.put(PRIVILEGED_CLASS_PACKAGES,
                     muleModule.getExportedPrivilegedPackages().stream().collect(joining(",")));
    }

    if (!muleModule.getModulePrivilegedArtifactIds().isEmpty()) {
      properties.put(PRIVILEGED_ARTIFACT_IDS,
                     muleModule.getModulePrivilegedArtifactIds().stream().collect(joining(",")));
    }

    if (!muleModule.getOptionalExportedPackages().isEmpty()) {
      properties.put(EXPORT_OPTIONAL_PACKAGES,
                     muleModule.getOptionalExportedPackages().stream().collect(joining(",")));
    }

    if (!muleModule.getModuleServiceDefinitions().isEmpty()) {
      StringBuilder propertyValue = new StringBuilder();
      for (ServiceDefinition serviceDefinition : muleModule.getModuleServiceDefinitions()) {
        for (String serviceImplementation : serviceDefinition.getServiceImplementations()) {
          propertyValue.append(serviceDefinition.getServiceInterface())
              .append(":")
              .append(serviceImplementation)
              .append(",");
        }
      }

      properties.put(EXPORT_SERVICES,
                     propertyValue.substring(0, propertyValue.length() - 1));
    }

    return properties;
  }

  private void writeFiles(final org.mule.tools.maven.plugin.module.bean.Module muleModule, final Properties properties)
      throws MojoFailureException, IOException, FileNotFoundException {
    if (!muleModule.getModuleServiceDefinitions().isEmpty()) {
      for (ServiceDefinition serviceDefinition : muleModule.getModuleServiceDefinitions()) {
        final File outputServiceFile =
            new File(project.getBuild().getOutputDirectory(), "META-INF/services/" + serviceDefinition.getServiceInterface());
        if (!outputServiceFile.getParentFile().isDirectory() || !outputServiceFile.getParentFile().exists()) {
          if (!outputServiceFile.getParentFile().mkdirs()) {
            throw new MojoFailureException("Could not create directory '" + outputServiceFile.getParentFile().getAbsolutePath()
                + "'.");
          }
        }

        if (!outputServiceFile.exists()) {
          getLog().info("Writing '" + outputServiceFile.getAbsolutePath() + "'...");
          try (final OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputServiceFile))) {
            write("#Generated by org.mule.tools.maven.plugin.module.generate.GenerateMojo" + LINE_SEPARATOR_UNIX, fw);
            writeLines(serviceDefinition.getServiceImplementations(), LINE_SEPARATOR_UNIX, fw);

            fw.flush();
          }
        } else {
          getLog().info("Service descriptor for '" + serviceDefinition.getServiceInterface() + "' already present, skipping.");
        }
      }
    }

    final File outputModuleFile = new File(project.getBuild().getOutputDirectory(), MULE_MODULE_PROPERTIES_LOCATION);
    if (!outputModuleFile.getParentFile().isDirectory() || !outputModuleFile.getParentFile().exists()) {
      if (!outputModuleFile.getParentFile().mkdirs()) {
        throw new MojoFailureException("Could not create directory '" + outputModuleFile.getParentFile().getAbsolutePath()
            + "'.");
      }
    }

    getLog().info("Writing '" + outputModuleFile.getAbsolutePath() + "'...");
    try (final FileOutputStream fos = new FileOutputStream(outputModuleFile)) {
      properties.store(fos, "Generated by org.mule.tools.maven.plugin.module.generate.GenerateMojo");
      fos.flush();
    }
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

}
