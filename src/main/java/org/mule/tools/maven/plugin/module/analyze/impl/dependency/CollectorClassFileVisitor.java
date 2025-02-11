/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.dependency;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Simply collects the set of visited classes.
 */
public class CollectorClassFileVisitor
    implements ClassFileVisitor {

  private final Set<String> classes;

  public CollectorClassFileVisitor() {
    classes = new HashSet<String>();
  }

  /*
   * @see org.mule.tools.maven.plugin.module.analyze.ClassFileVisitor#visitClass(java.lang.String, java.io.InputStream)
   */
  public void visitClass(String className, InputStream in) {
    classes.add(className);
  }

  public Set<String> getClasses() {
    return classes;
  }
}
