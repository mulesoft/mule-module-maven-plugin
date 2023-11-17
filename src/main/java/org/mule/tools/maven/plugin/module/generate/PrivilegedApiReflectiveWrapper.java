/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.generate;

import org.mule.api.annotation.jpms.PrivilegedApi;

import java.lang.annotation.Annotation;

// Use reflection to workaround the annotation being loaded by two different classloaders.
public class PrivilegedApiReflectiveWrapper {

  private final Class privilegedApiAnnotationClass;
  private final Annotation privilegedApiInfo;

  public PrivilegedApiReflectiveWrapper(Module currentModule) throws ClassNotFoundException {
    privilegedApiAnnotationClass = currentModule.getClassLoader().loadClass(PrivilegedApi.class.getName());
    privilegedApiInfo = currentModule.getAnnotation(privilegedApiAnnotationClass);
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
