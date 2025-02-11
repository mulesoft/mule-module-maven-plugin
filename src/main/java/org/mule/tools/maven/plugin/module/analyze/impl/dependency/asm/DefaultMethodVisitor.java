/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm;

import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;


/**
 * Computes the set of classes referenced by visited code. Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in
 * the ASM dependencies example.
 */
public class DefaultMethodVisitor
    extends MethodVisitor {

  private final String packageName;
  private final AnnotationVisitor annotationVisitor;

  private final SignatureVisitor signatureVisitor;

  private final ResultCollector resultCollector;
  private final ModuleLogger analyzerLogger;

  public DefaultMethodVisitor(String packageName, AnnotationVisitor annotationVisitor, SignatureVisitor signatureVisitor,
                              ResultCollector resultCollector, ModuleLogger analyzerLogger) {
    super(Opcodes.ASM5);
    this.packageName = packageName;
    this.annotationVisitor = annotationVisitor;
    this.signatureVisitor = signatureVisitor;
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


  public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
    if (visible) {
      resultCollector.addDesc(packageName, desc);

      return annotationVisitor;
    } else {
      return null;
    }
  }
}
