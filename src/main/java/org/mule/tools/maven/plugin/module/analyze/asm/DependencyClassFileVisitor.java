/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.asm;

import static org.mule.tools.maven.plugin.module.analyze.DefaultModuleApiAnalyzer.getPackageName;
import org.mule.tools.maven.plugin.module.analyze.AnalyzerLogger;
import org.mule.tools.maven.plugin.module.analyze.ClassFileVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Computes the set of classes referenced by visited class files
 */
public class DependencyClassFileVisitor implements ClassFileVisitor {

  private final ResultCollector resultCollector;
  private final AnalyzerLogger analyzerLogger;

  public DependencyClassFileVisitor(AnalyzerLogger analyzerLogger) {
    this.analyzerLogger = analyzerLogger;
    resultCollector = new ResultCollector(analyzerLogger);
  }

  /*
   * @see org.mule.tools.maven.plugin.module.analyze.ClassFileVisitor#visitClass(java.lang.String, java.io.InputStream)
   */
  public void visitClass(String className, InputStream in) {
    // TODO(pablo.kraan): MULE-14419 - ignoring classes defined inside META-INF folder and module-info until Java 9 is supported
    if (className.startsWith("META-INF.") || className.contains(".META-INF.") || className.equals("module-info")) {
      return;
    }

    try {
      final String packageName = getPackageName(className);
      ClassReader reader = new ClassReader(in);

      AnnotationVisitor annotationVisitor = new DefaultAnnotationVisitor(packageName, resultCollector, analyzerLogger);
      SignatureVisitor signatureVisitor = new DefaultSignatureVisitor(packageName, resultCollector, analyzerLogger);
      FieldVisitor fieldVisitor = new DefaultFieldVisitor(packageName, annotationVisitor, resultCollector, analyzerLogger);
      MethodVisitor mv =
          new DefaultMethodVisitor(packageName, annotationVisitor, signatureVisitor, resultCollector, analyzerLogger);
      ClassVisitor classVisitor =
          new DefaultClassVisitor(packageName, signatureVisitor, annotationVisitor, fieldVisitor, mv, resultCollector,
                                  analyzerLogger);

      reader.accept(classVisitor, 0);
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (IndexOutOfBoundsException e) {
      // some bug inside ASM causes an IOB exception. Log it and move on?
      // this happens when the class isn't valid.
      analyzerLogger.log("Unable to process: " + className);
    }
  }

  /**
   * @return the set of classes referenced by visited class files
   */
  public Set<String> getDependencies() {
    return resultCollector.getDependencies();
  }

  public Map<String, Set<String>> getPackageDeps() {
    return resultCollector.getPackageDeps();
  }


}
