/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.generate.mms;

import org.mule.api.annotation.jpms.OptionalPackages;
import org.mule.api.annotation.jpms.PrivilegedApi;

import java.lang.annotation.Annotation;

// Use reflection to work around the annotation being loaded by two different classloaders.
public class PrivilegedApiReflectiveWrapper {

  private final Class optionalPackagesAnnotationClass;
  private final Class privilegedApiAnnotationClass;
  private final Annotation optionalPackagesInfo;
  private final Annotation privilegedApiInfo;

  public PrivilegedApiReflectiveWrapper(Module currentModule) {
    Class tmpOptionalPackagesAnnotationClass;
    Class tmpPrivilegedApiAnnotationClass;
    Annotation tmpOptionalPackagesInfo;
    Annotation tmpPrivilegedApiInfo;

    try {
      tmpOptionalPackagesAnnotationClass = currentModule.getClassLoader().loadClass(OptionalPackages.class.getName());
      tmpPrivilegedApiAnnotationClass = currentModule.getClassLoader().loadClass(PrivilegedApi.class.getName());
      tmpOptionalPackagesInfo = currentModule.getAnnotation(tmpOptionalPackagesAnnotationClass);
      tmpPrivilegedApiInfo = currentModule.getAnnotation(tmpPrivilegedApiAnnotationClass);
    } catch (ClassNotFoundException e) {
      // no annotations in the module definition. do nothing.
      tmpOptionalPackagesAnnotationClass = null;
      tmpPrivilegedApiAnnotationClass = null;
      tmpOptionalPackagesInfo = null;
      tmpPrivilegedApiInfo = null;
    }

    this.optionalPackagesAnnotationClass = tmpOptionalPackagesAnnotationClass;
    this.privilegedApiAnnotationClass = tmpPrivilegedApiAnnotationClass;
    this.optionalPackagesInfo = tmpOptionalPackagesInfo;
    this.privilegedApiInfo = tmpPrivilegedApiInfo;
  }

  public String[] getOptionalPackages() {
    if (optionalPackagesInfo == null) {
      return new String[0];
    }

    try {
      return (String[]) optionalPackagesAnnotationClass.getDeclaredMethod("value")
          .invoke(optionalPackagesInfo);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public String[] getPrivilegedPackages() {
    if (privilegedApiInfo == null) {
      return new String[0];
    }

    try {
      return (String[]) privilegedApiAnnotationClass.getDeclaredMethod("privilegedPackages")
          .invoke(privilegedApiInfo);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public String[] getPrivilegedArtifactIds() {
    if (privilegedApiInfo == null) {
      return new String[0];
    }

    try {
      return (String[]) privilegedApiAnnotationClass.getDeclaredMethod("privilegedArtifactIds")
          .invoke(privilegedApiInfo);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
