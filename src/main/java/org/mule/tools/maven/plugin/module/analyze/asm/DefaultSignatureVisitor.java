/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.asm;

import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Computes the set of classes referenced by visited code. Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in
 * the ASM dependencies example.
 */
public class DefaultSignatureVisitor
    extends SignatureVisitor {

  private final String packageName;
  private final ResultCollector resultCollector;
  private final ModuleLogger analyzerLogger;

  public DefaultSignatureVisitor(String packageName, ResultCollector resultCollector, ModuleLogger analyzerLogger) {
    super(Opcodes.ASM5);
    this.packageName = packageName;
    this.resultCollector = resultCollector;
    this.analyzerLogger = analyzerLogger;
  }

  public void visitClassType(final String name) {
    resultCollector.addName(packageName, name);
  }

  public void visitInnerClassType(final String name) {
    resultCollector.addName(packageName, name);
  }
}
