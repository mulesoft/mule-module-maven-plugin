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
public class MethodParameterPackageTestCase
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public MethodParameterPackageTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        this.mavenRuntime = builder.build();
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

    private void doExportABTest(String projectName) throws Exception
    {
        //TODO(pablo.kraan): this test should be different when org.bar is exported in the module, otherwise there is no safety that the code is doing the real thing
        File basedir = resources.getBasedir(projectName);
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertNoLogText("Packages that must be exported:");
    }

    private void doExportAMissingBTest(String projectName) throws Exception
    {
        File basedir = resources.getBasedir(projectName);
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertLogText("Packages that must be exported:");
        result.assertLogText("org.bar");
    }
}
