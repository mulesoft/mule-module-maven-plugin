/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.common;

import org.mule.tools.maven.plugin.module.analyze.ModuleApiAnalyzer;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

public abstract class AbstractModuleMojo extends org.apache.maven.plugin.AbstractMojo implements Contextualizable {

  /**
   * The plexus context to look-up the right {@link ModuleApiAnalyzer} implementation depending on the mojo configuration.
   */
  private Context context;

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  protected ModuleApiAnalyzer analyzer;

  @Override
  public void contextualize(Context context)
      throws ContextException {
    this.context = context;
    this.analyzer = createProjectDependencyAnalyzer();
  }

  protected ModuleApiAnalyzer createProjectDependencyAnalyzer() throws ContextException {
    final String role = ModuleApiAnalyzer.ROLE;
    final String roleHint = "default";

    try {
      final PlexusContainer container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);

      return (ModuleApiAnalyzer) container.lookup(role, roleHint);
    } catch (ContextException | ComponentLookupException exception) {
      throw new ContextException("Failed to instantiate ModuleApiAnalyzer with role " + role + " / role-hint " + roleHint,
                                 exception);
    }
  }


}
