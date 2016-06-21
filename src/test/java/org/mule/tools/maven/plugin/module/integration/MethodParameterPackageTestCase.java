/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class MethodParameterPackageTestCase extends AbstractExportTestCase
{

    public MethodParameterPackageTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        super(builder);
    }

    //// Parameter type

    @Test
    public void exportedParameterInPublicMethod() throws Exception
    {
        doExportABTest("exportedParameterInPublicMethod");
    }

    @Test
    public void exportedParameterInProtectedMethod() throws Exception
    {
        doExportABTest("exportedParameterInProtectedMethod");
    }

    @Test
    public void missingExportedParameterInPublicMethod() throws Exception
    {
        doExportAMissingBTest("missingParameterInPublicMethod");
    }

    @Test
    public void missingExportedParameterInProtectedMethod() throws Exception
    {
        doExportAMissingBTest("missingParameterInProtectedMethod");
    }

    @Test
    public void ignoresParameterInPrivateMethod() throws Exception
    {
        doExportABTest("ignoresParameterInPrivateMethod");
    }

    @Test
    public void ignoresParameterInPackageMethod() throws Exception
    {
        doExportABTest("ignoresParameterInPackageMethod");
    }

    @Test
    public void ignoresParamInProtectedMethodFromFinalClass() throws Exception
    {
        doExportABTest("ignoresParameterInProtectedMethodFromFinalClass");
    }

    //// Return type


    @Test
    public void exportedReturnPackageInPublicMethod() throws Exception
    {
        doExportABTest("exportedReturnInPublicMethod");
    }

    @Test
    public void exportedReturnPackageInProtectedMethod() throws Exception
    {
        doExportABTest("exportedReturnInProtectedMethod");
    }

    @Test
    public void missingExportedReturnPackageInPublicMethod() throws Exception
    {
        doExportAMissingBTest("missingReturnInPublicMethod");
    }

    @Test
    public void missingExportedReturnPackageInProtectedMethod() throws Exception
    {
        doExportAMissingBTest("missingReturnInProtectedMethod");
    }

    @Test
    public void ignoresReturnPackageInPrivateMethod() throws Exception
    {
        doExportABTest("ignoresReturnInPrivateMethod");
    }

    @Test
    public void ignoresReturnPackageInPackageMethod() throws Exception
    {
        doExportABTest("ignoresReturnInPackageMethod");
    }

    @Test
    public void ignoresReturnPackageInProtectedMethodFromFinalClass() throws Exception
    {
        doExportABTest("ignoresReturnInProtectedMethodFromFinalClass");
    }

}
