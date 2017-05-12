/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class ClassesExportTestCase extends AbstractExportTestCase {

  public ClassesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "class");
  }

  @Test
  public void exportedSuperClassInPublicClass() throws Exception {
    doExportABTest("exportedSuperClassInPublicClass");
  }

  @Test
  public void missingSuperClassInPublicClass() throws Exception {
    doExportAMissingBTest("missingSuperClassInPublicClass");
  }

  @Test
  public void exportedSuperClassInProtectedInnerClass() throws Exception {
    doExportABTest("exportedSuperClassInProtectedInnerClass");
  }

  @Test
  public void missingSuperClassInProtectedInnerClass() throws Exception {
    doExportAMissingBTest("missingSuperClassInProtectedInnerClass");
  }

  @Test
  public void ignoresSuperClassInPackageClass() throws Exception {
    doExportABTest("ignoresSuperClassInPackageClass");
  }

  @Test
  public void ignoresSuperClassInPrivateInnerClass() throws Exception {
    doExportABTest("ignoresSuperClassInPrivateInnerClass");
  }
}
