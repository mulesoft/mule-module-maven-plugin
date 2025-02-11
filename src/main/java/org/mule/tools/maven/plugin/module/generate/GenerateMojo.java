/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.generate;

import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.EXPORT_CLASS_PACKAGES;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.EXPORT_OPTIONAL_PACKAGES;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.EXPORT_SERVICES;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.MODULE_NAME;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.MULE_MODULE_PROPERTIES_LOCATION;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.PRIVILEGED_ARTIFACT_IDS;
import static org.mule.tools.maven.plugin.module.analyze.impl.module.mms.MuleModuleSystemModuleFactory.PRIVILEGED_CLASS_PACKAGES;

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
import static java.util.stream.Collectors.toSet;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.apache.maven.artifact.Artifact.SCOPE_TEST;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

import org.mule.tools.maven.plugin.module.analyze.api.Module;
import org.mule.tools.maven.plugin.module.analyze.maven.AbstractModuleMojo;
import org.mule.tools.maven.plugin.module.analyze.impl.module.DefaultModule;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleApiAnalyzerException;
import org.mule.tools.maven.plugin.module.analyze.api.ProjectAnalysisResult;
import org.mule.tools.maven.plugin.module.analyze.impl.logging.SilentAnalyzerLogger;
import org.mule.tools.maven.plugin.module.analyze.impl.module.ServiceDefinition;
import org.mule.tools.maven.plugin.module.generate.mms.PrivilegedApiReflectiveWrapper;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generates mule-module.properties file form module-info.java if present.
 */
@Mojo(name = "generate",
    requiresDependencyResolution = TEST,
    requiresDependencyCollection = TEST,
    threadSafe = true,
    defaultPhase = PROCESS_CLASSES)
public class GenerateMojo extends AbstractModuleMojo {

  /**
   * Skip plugin execution completely.
   */
  @Parameter(property = "muleModule.generate.skip", defaultValue = "false")
  private boolean skip;

  @Parameter(defaultValue = "false")
  private boolean fillOptionalPackages;

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
    final Module muleModule = toMuleModule(currentModule);

    getLog().info("Setting properties for mule module '" + currentModule.getName() + "'...");
    final Properties properties = toMuleModuleProperties(muleModule);

    getLog().info("Saving generated files for module '" + currentModule.getName() + "'...");
    writeFiles(muleModule, properties);
  }

  private Module toMuleModule(final java.lang.Module currentModule)
      throws ClassNotFoundException, ModuleApiAnalyzerException, MojoExecutionException {
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
          serviceDefinition.setServiceImplementations(provides.providers()
              .stream()
              .collect(toList()));
          return serviceDefinition;
        })
        .collect(toCollection(TreeSet::new));

    Module generatedModule =
        new DefaultModule(currentModule.getName(),
                          exportedPackages,
                          exportedPrivilegedPackages,
                          optionalPackages,
                          modulePrivilegedArtifactIds,
                          moduleServiceDefinitions);

    if (fillOptionalPackages) {
      final ProjectAnalysisResult analysis = analyzer.get("mms")
          .analyze(project, generatedModule, new SilentAnalyzerLogger(), getLog());
      final Set<String> additionalOptionalPackages = analysis.getStandardApi().getPackagesToExport();
      if (!additionalOptionalPackages.isEmpty()) {
        optionalPackages.addAll(additionalOptionalPackages);
        generatedModule = new DefaultModule(currentModule.getName(),
                                            exportedPackages,
                                            exportedPrivilegedPackages,
                                            optionalPackages,
                                            modulePrivilegedArtifactIds,
                                            moduleServiceDefinitions);
      }
    }

    return generatedModule;
  }

  private boolean isProvidedServiceExported(final Set<String> exportedPrivilegedPackages, final Set<String> exportedPackages,
                                            String providedService) {
    final String packageName = providedService.substring(0, providedService.lastIndexOf("."));

    return exportedPackages.contains(packageName) || exportedPrivilegedPackages.contains(packageName);
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
          .map(cpe -> Paths.get(cpe))
          // Do not use xml-apis which causes a split package with the xml module in the JVM
          .filter(path -> !xmlApisPath.map(path::equals).orElse(false))
          .toArray(Path[]::new));

      final Configuration configuration = boot().configuration().resolve(finder, ofSystem(), roots);

      Controller controller = defineModulesWithOneLoader(configuration,
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

  private Properties toMuleModuleProperties(final Module muleModule) {
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
          if (isProvidedServiceExported(muleModule.getExportedPackages(), muleModule.getExportedPrivilegedPackages(),
                                        serviceImplementation)) {
            propertyValue.append(serviceDefinition.getServiceInterface())
                .append(":")
                .append(serviceImplementation)
                .append(",");
          }
        }
      }

      if (propertyValue.length() > 0) {
        properties.put(EXPORT_SERVICES,
                       propertyValue.substring(0, propertyValue.length() - 1));
      }
    }

    return properties;
  }

  private void writeFiles(final Module muleModule, final Properties properties)
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
    if (outputModuleFile.exists() && isExistingModulePropertiesFileEqual(properties, outputModuleFile)) {
      getLog().info("No changes detected for '" + outputModuleFile.getAbsolutePath() + "' skipping.");
      return;
    }

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

  private boolean isExistingModulePropertiesFileEqual(Properties newProperties, File moduleFile) {
    Properties existingProperties = new Properties();
    try (final FileInputStream fis = new FileInputStream(moduleFile)) {
      existingProperties.load(fis);
      return existingProperties.equals(newProperties);
    } catch (IOException e) {
      getLog().info("Unable to read existing module properties file'" + moduleFile.getAbsolutePath() + "'...");
      return false;
    }
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

}
