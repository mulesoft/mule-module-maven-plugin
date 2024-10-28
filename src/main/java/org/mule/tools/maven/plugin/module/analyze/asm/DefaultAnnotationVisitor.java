/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.asm;

import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Computes the set of classes referenced by visited code. Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in
 * the ASM dependencies example.
 */
public class DefaultAnnotationVisitor extends AnnotationVisitor {

  private final String packageName;
  private final ResultCollector resultCollector;
  private final ModuleLogger analyzerLogger;

  public DefaultAnnotationVisitor(String packageName, ResultCollector resultCollector, ModuleLogger analyzerLogger) {
    super(Opcodes.ASM5);
    this.packageName = packageName;
    this.resultCollector = resultCollector;
    this.analyzerLogger = analyzerLogger;
  }

  public void visit(final String name, final Object value) {
    if (value instanceof Type) {
      resultCollector.addType(packageName, (Type) value);
    }
  }

  public void visitEnum(final String name, final String desc, final String value) {
    resultCollector.addDesc(packageName, desc);
  }

  public AnnotationVisitor visitAnnotation(final String name, final String desc) {
    resultCollector.addDesc(packageName, desc);

    return this;
  }

  /*
   * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
   */
  public AnnotationVisitor visitArray(final String name) {
    return this;
  }

}
