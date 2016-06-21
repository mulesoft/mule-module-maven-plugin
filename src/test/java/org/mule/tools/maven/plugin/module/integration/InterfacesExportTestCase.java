/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class InterfacesExportTestCase extends AbstractExportTestCase
{

    public InterfacesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        super(builder);
    }

    @Test
    public void exportedSuperInterfaceInPublicInterface() throws Exception
    {
        doExportABTest("exportedSuperInterfaceInPublicInterface");
    }

    @Test
    public void missingSuperInterfaceInPublicInterface() throws Exception
    {
        doExportAMissingBTest("missingSuperInterfaceInPublicInterface");
    }

    @Test
    public void exportedSuperInterfaceInProtectedInnerInterface() throws Exception
    {
        doExportABTest("exportedSuperInterfaceInProtectedInnerInterface");
    }

    @Test
    public void missingSuperInterfaceInProtectedInnerInterface() throws Exception
    {
        doExportAMissingBTest("missingSuperInterfaceInProtectedInnerInterface");
    }

    @Test
    public void ignoresSuperInterfaceInPackageInterface() throws Exception
    {
        doExportABTest("ignoresSuperInterfaceInPackageInterface");
    }

    @Test
    public void ignoresSuperInterfaceInPrivateInnerInterface() throws Exception
    {
        doExportABTest("ignoresSuperInterfaceInPrivateInnerInterface");
    }
}
