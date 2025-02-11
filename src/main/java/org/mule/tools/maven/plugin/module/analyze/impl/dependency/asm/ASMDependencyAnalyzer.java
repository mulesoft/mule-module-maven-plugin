/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm;

import org.mule.tools.maven.plugin.module.analyze.impl.dependency.ClassFileVisitorUtils;
import org.mule.tools.maven.plugin.module.analyze.impl.dependency.DependencyAnalyzer;
import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.sisu.Typed;

@Named
@Singleton
@Typed(DependencyAnalyzer.class)
public class ASMDependencyAnalyzer
    implements DependencyAnalyzer {

  /*
   * @see org.mule.tools.maven.plugin.module.analyze.DependencyAnalyzer#analyze(java.net.URL)
   */
  @Override
  public Map<String, Set<String>> analyze(URL url, ModuleLogger analyzerLogger)
      throws IOException {
    DependencyClassFileVisitor visitor = new DependencyClassFileVisitor(analyzerLogger);

    ClassFileVisitorUtils.accept(url, visitor, analyzerLogger);

    return visitor.getPackageDeps();
  }
}
