/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class ClassFieldExportTestCase extends AbstractExportTestCase
{

    public ClassFieldExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        super(builder);
    }

    @Test
    public void exportedPublicInstanceField() throws Exception
    {
        doExportABTest("exportedPublicInstanceField");
    }

    @Test
    public void exportedProtectedInstanceField() throws Exception
    {
        doExportABTest("exportedProtectedInstanceField");
    }

    @Test
    public void missingProtectedInstanceField() throws Exception
    {
        doExportAMissingBTest("missingProtectedInstanceField");
    }

    @Test
    public void missingPublicInstanceField() throws Exception
    {
        doExportAMissingBTest("missingPublicInstanceField");
    }

    @Test
    public void ignoresPrivateInstanceField() throws Exception
    {
        doExportABTest("ignoresPrivateInstanceField");
    }

    @Test
    public void ignoresPackageInstanceField() throws Exception
    {
        doExportABTest("ignoresPackageInstanceField");
    }

    @Test
    public void ignoresProtectedInstanceFieldInFinalClass() throws Exception
    {
        doExportABTest("ignoresProtectedInstanceFieldInFinalClass");
    }
}
