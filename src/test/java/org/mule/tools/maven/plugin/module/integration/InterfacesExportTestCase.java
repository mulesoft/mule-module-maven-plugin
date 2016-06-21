/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.DEPENDENCY_PROBLEMS_FOUND;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.NO_DEPENDENCY_PROBLEMS_FOUND;

import java.io.File;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
public class InterfacesExportTestCase
{
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public InterfacesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        this.mavenRuntime = builder.build();
    }

    @Test
    public void exportedSuperInterfaceInPublicInterface() throws Exception
    {
        File basedir = resources.getBasedir("exportedSuperInterfaceInPublicInterface");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertNoLogText("Packages that must be exported:");
    }


    @Test
    public void missingSuperInterfaceInPublicInterface() throws Exception
    {
        File basedir = resources.getBasedir("missingSuperInterfaceInPublicInterface");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertLogText("Packages that must be exported:");
    }

    @Test
    public void exportedSuperInterfaceInProtectedInnerInterface() throws Exception
    {
        File basedir = resources.getBasedir("exportedSuperInterfaceInProtectedInnerInterface");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertNoLogText("Packages that must be exported:");
    }

    @Test
    public void missingSuperInterfaceInProtectedInnerInterface() throws Exception
    {
        File basedir = resources.getBasedir("missingSuperInterfaceInProtectedInnerInterface");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertLogText("Visiting class: org/foo/A");
        //result.assertLogText("Visiting class: org/bar/A$C");
        result.assertLogText("Packages that must be exported:");
    }

    @Test
    public void ignoresSuperInterfaceInPackageInterface() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSuperInterfaceInPackageInterface");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertNoLogText("Packages that must be exported:");
    }

    @Test
    public void ignoresSuperInterfaceInPrivateInnerInterface() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSuperInterfaceInPrivateInnerInterface");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        //result.assertLogText("Visiting class: org/bar/A$C");
        result.assertNoLogText("Packages that must be exported:");
    }
}
