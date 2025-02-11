/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm;

import static org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm.AccessUtils.isFinal;
import static org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm.AccessUtils.isPackage;
import static org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm.AccessUtils.isPrivate;
import static org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm.AccessUtils.isProtected;
import static org.mule.tools.maven.plugin.module.analyze.impl.dependency.asm.AccessUtils.isPublic;

import org.mule.tools.maven.plugin.module.analyze.api.ModuleLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


/**
 * Computes the set of classes referenced by visited code. Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in
 * the ASM dependencies example.
 */
public class DefaultClassVisitor extends ClassVisitor {

  private final ResultCollector resultCollector;
  private final ModuleLogger analyzerLogger;

  private final String packageName;
  private final SignatureVisitor signatureVisitor;

  private final AnnotationVisitor annotationVisitor;

  private final FieldVisitor fieldVisitor;

  private final MethodVisitor methodVisitor;
  private boolean skipClass;
  private boolean isFinalClass;

  public DefaultClassVisitor(String packageName, SignatureVisitor signatureVisitor, AnnotationVisitor annotationVisitor,
                             FieldVisitor fieldVisitor, MethodVisitor methodVisitor,
                             ResultCollector resultCollector, ModuleLogger analyzerLogger) {
    super(Opcodes.ASM9);
    this.packageName = packageName;
    this.signatureVisitor = signatureVisitor;
    this.annotationVisitor = annotationVisitor;
    this.fieldVisitor = fieldVisitor;
    this.methodVisitor = methodVisitor;
    this.resultCollector = resultCollector;
    this.analyzerLogger = analyzerLogger;
  }

  @Override
  public void visit(final int version, final int access, final String name, final String signature,
                    final String superName, final String[] interfaces) {
    analyzerLogger.log("Analyzing class: " + name + (signature != null ? signature : ""));
    resultCollector.addName(packageName, name);

    skipClass = isPrivate(access) || isPackage(access);
    if (skipClass) {
      String accessString = isPrivate(access) ? "private" : "package";
      analyzerLogger.log("Skipping class: " + name + (signature != null ? signature : "") + " visibility: " + accessString);
    } else {
      isFinalClass = isFinal(access);

      if (signature == null) {
        resultCollector.addName(packageName, superName);
        resultCollector.addNames(packageName, interfaces);
      } else {
        addSignature(signature);
      }
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    if (skipClass) {
      return null;
    }

    if (visible) {
      resultCollector.addDesc(packageName, desc);

      return annotationVisitor;
    } else {
      return null;
    }
  }

  @Override
  public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
                                 final Object value) {
    if (skipClass) {
      return null;
    }

    if (isPublic(access) || (isProtected(access) && !isFinalClass)) {
      String accessString = isPublic(access) ? "public" : "protected";
      analyzerLogger.log("Analyzing field: " + name + " - " + accessString);
      if (signature == null) {
        resultCollector.addDesc(packageName, desc);
      } else {
        addTypeSignature(signature);
      }

      if (value instanceof Type) {
        resultCollector.addType(packageName, (Type) value);
      }

      return fieldVisitor;
    } else {
      String accessString = isPrivate(access) ? "private" : "package";
      analyzerLogger.log("Analyzing field: " + name + " - " + accessString);
      return null;
    }
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                                   final String[] exceptions) {
    if (skipClass) {
      return null;
    }

    if (isPrivate(access) || isPackage(access) || (isProtected(access) && isFinalClass)) {
      // Ignore method
      return null;
    }

    if (signature == null) {
      resultCollector.addMethodDesc(packageName, desc);
    } else {
      addSignature(signature);
    }

    resultCollector.addNames(packageName, exceptions);

    return methodVisitor;
  }

  private void addSignature(final String signature) {
    if (signature != null) {
      new SignatureReader(signature).accept(signatureVisitor);
    }
  }

  private void addTypeSignature(final String signature) {
    if (signature != null) {
      new SignatureReader(signature).acceptType(signatureVisitor);
    }
  }


}
