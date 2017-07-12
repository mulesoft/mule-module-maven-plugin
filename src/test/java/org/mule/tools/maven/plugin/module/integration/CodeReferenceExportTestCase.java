/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class CodeReferenceExportTestCase extends AbstractExportTestCase {

  public CodeReferenceExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    super(builder, "code");
  }

  @Test
  public void ignoresTypeReferenceInMethod() throws Exception {
    doSuccessfulStandardValidationTest("ignoresTypeReferenceInMethod");
  }

  @Test
  public void ignoresTypeReferenceInStaticInitializer() throws Exception {
    doSuccessfulStandardValidationTest("ignoresTypeReferenceInStaticInitializer");
  }

}
