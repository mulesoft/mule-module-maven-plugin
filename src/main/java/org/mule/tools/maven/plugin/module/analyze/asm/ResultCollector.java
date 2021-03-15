/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.asm;

import static org.mule.tools.maven.plugin.module.analyze.DefaultModuleApiAnalyzer.getPackageName;

import org.mule.tools.maven.plugin.module.analyze.AnalyzerLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

public class ResultCollector {

  private final Set<String> classes = new HashSet<String>();
  private final Map<String, Set<String>> packageDeps = new HashMap<String, Set<String>>();
  private final AnalyzerLogger analyzerLogger;

  public ResultCollector(AnalyzerLogger analyzerLogger) {
    this.analyzerLogger = analyzerLogger;
  }

  public Set<String> getDependencies() {
    return classes;
  }

  public Map<String, Set<String>> getPackageDeps() {
    return packageDeps;
  }

  public void addName(String packageName, String name) {
    if (name == null) {
      return;
    }

    // decode arrays
    if (name.startsWith("[L") && name.endsWith(";")) {
      name = name.substring(2, name.length() - 1);
    }

    // decode internal representation
    name = name.replace('/', '.');

    classes.add(name);
    addPackageDependency(packageName, name);
  }

  private void addPackageDependency(String packageName, String name) {
    if (name.startsWith("java.")) {
      // Ignore JDK dependencies
      return;
    }

    Set<String> deps = packageDeps.get(packageName);
    if (deps == null) {
      deps = new HashSet<String>();
      packageDeps.put(packageName, deps);
    }
    final String depPackageName = getPackageName(name);
    // TODO(pablo.kraan): is OK just to ignore the default package?
    if (!packageName.equals(depPackageName) && !"".equals(depPackageName) && !deps.contains(depPackageName)) {
      analyzerLogger.log("Adding dependency from " + packageName + " to " + depPackageName);
      deps.add(depPackageName);
    }
  }

  void addDesc(String packageName, final String desc) {
    addType(packageName, Type.getType(desc));
  }

  void addType(String packageName, final Type t) {
    switch (t.getSort()) {
      case Type.ARRAY:
        addType(packageName, t.getElementType());
        break;

      case Type.OBJECT:
        addName(packageName, t.getClassName().replace('.', '/'));
        break;

      default:
    }
  }

  public void add(String packageName, String name) {
    classes.add(name);
    addPackageDependency(packageName, name);
  }

  void addNames(String packageName, final String[] names) {
    if (names == null) {
      return;
    }

    for (String name : names) {
      addName(packageName, name);
    }
  }

  void addMethodDesc(String packageName, final String desc) {
    addType(packageName, Type.getReturnType(desc));

    Type[] types = Type.getArgumentTypes(desc);

    for (Type type : types) {
      addType(packageName, type);
    }
  }
}
