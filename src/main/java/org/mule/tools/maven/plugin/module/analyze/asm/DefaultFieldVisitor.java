/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.asm;

import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Computes the set of classes referenced by visited code. Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in
 * the ASM dependencies example.
 */
public class DefaultFieldVisitor
    extends FieldVisitor {

  private final String packageName;
  private final AnnotationVisitor annotationVisitor;

  private final ResultCollector resultCollector;
  private final ModuleLogger analyzerLogger;

  public DefaultFieldVisitor(String packageName, AnnotationVisitor annotationVisitor, ResultCollector resultCollector,
                             ModuleLogger analyzerLogger) {
    super(Opcodes.ASM5);
    this.packageName = packageName;
    this.annotationVisitor = annotationVisitor;
    this.resultCollector = resultCollector;
    this.analyzerLogger = analyzerLogger;
  }

  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    if (visible) {
      resultCollector.addDesc(packageName, desc);

      return annotationVisitor;
    } else {
      return null;
    }
  }

}
