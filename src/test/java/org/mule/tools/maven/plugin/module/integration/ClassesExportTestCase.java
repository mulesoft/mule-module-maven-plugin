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
public class ClassesExportTestCase
{
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public ClassesExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        this.mavenRuntime = builder.build();
    }

    @Test
    public void exportedSuperClassInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("exportedSuperClassInPublicClass");
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
    public void missingSuperClassInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("missingSuperClassInPublicClass");
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
    public void exportedSuperClassInProtectedInnerClass() throws Exception
    {
        File basedir = resources.getBasedir("exportedSuperClassInProtectedInnerClass");
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
    public void missingSuperClassInProtectedInnerClass() throws Exception
    {
        File basedir = resources.getBasedir("missingSuperClassInProtectedInnerClass");
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
    public void ignoresSuperClassInPackageClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSuperClassInPackageClass");
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
    public void ignoresSuperClassInPrivateInnerClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSuperClassInPrivateInnerClass");
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

    @Test
    public void exportedInterfaceInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("exportedInterfaceInPublicClass");
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
    public void missingInterfaceInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("missingInterfaceInPublicClass");
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
    public void exportedInterfaceInProtectedInnerClass() throws Exception
    {
        File basedir = resources.getBasedir("exportedInterfaceInProtectedInnerClass");
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
    public void missingInterfaceInProtectedInnerClass() throws Exception
    {
        File basedir = resources.getBasedir("missingInterfaceInProtectedInnerClass");
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
    public void ignoresInterfaceInPackageClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresInterfaceInPackageClass");
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
    public void ignoresInterfaceInPrivateInnerClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresInterfaceInPrivateInnerClass");
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
