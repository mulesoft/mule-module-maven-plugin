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
public class AnnotationsExportTestCase
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public AnnotationsExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        this.mavenRuntime = builder.build();
    }


    @Test
    public void exportedRuntimeAnnotationInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("exportedRuntimeAnnotationInPublicClass");
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
    public void missingRuntimeAnnotationInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("missingRuntimeAnnotationInPublicClass");
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
    public void ignoresCompileAnnotationInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresCompileAnnotationInPublicClass");
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
    public void ignoresSourceAnnotationInPublicClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSourceAnnotationInPublicClass");
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
    public void ignoresRuntimeAnnotationInPackageClass() throws Exception
    {
        File basedir = resources.getBasedir("ignoresRuntimeAnnotationInPackageClass");
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
    public void exportedRuntimeAnnotationInPublicMethod() throws Exception
    {
        //TODO(pablo.kraan): what about private/protected/package methods?
        File basedir = resources.getBasedir("exportedRuntimeAnnotationInPublicMethod");
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
    public void missingRuntimeAnnotationInPublicMethod() throws Exception
    {
        File basedir = resources.getBasedir("missingRuntimeAnnotationInPublicMethod");
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
    public void ignoresCompileAnnotationInPublicMethod() throws Exception
    {
        File basedir = resources.getBasedir("ignoresCompileAnnotationInPublicMethod");
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
    public void ignoresSourceAnnotationInPublicMethod() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSourceAnnotationInPublicMethod");
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
    public void ignoresRuntimeAnnotationInPackageMethod() throws Exception
    {
        File basedir = resources.getBasedir("ignoresRuntimeAnnotationInPackageMethod");
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
    public void exportedRuntimeAnnotationInPublicField() throws Exception
    {
        File basedir = resources.getBasedir("exportedRuntimeAnnotationInPublicField");
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
    public void missingRuntimeAnnotationInPublicField() throws Exception
    {
        File basedir = resources.getBasedir("missingRuntimeAnnotationInPublicField");
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
    public void ignoresCompileAnnotationInPublicField() throws Exception
    {
        File basedir = resources.getBasedir("ignoresCompileAnnotationInPublicField");
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
    public void ignoresSourceAnnotationInPublicField() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSourceAnnotationInPublicField");
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
    public void ignoresRuntimeAnnotationInPackageField() throws Exception
    {
        File basedir = resources.getBasedir("ignoresRuntimeAnnotationInPackageMethod");
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
    public void exportedRuntimeAnnotationInPublicMethodParam() throws Exception
    {
        File basedir = resources.getBasedir("exportedRuntimeAnnotationInPublicMethodParam");
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
    public void missingRuntimeAnnotationInPublicMethodParam() throws Exception
    {
        File basedir = resources.getBasedir("missingRuntimeAnnotationInPublicMethodParam");
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
    public void ignoresCompileAnnotationInPublicMethodParam() throws Exception
    {
        File basedir = resources.getBasedir("ignoresCompileAnnotationInPublicMethodParam");
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
    public void ignoresSourceAnnotationInPublicMethodParam() throws Exception
    {
        File basedir = resources.getBasedir("ignoresSourceAnnotationInPublicMethodParam");
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
    public void ignoresRuntimeAnnotationInPackageMethodParam() throws Exception
    {
        File basedir = resources.getBasedir("ignoresRuntimeAnnotationInPackageMethodParam");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Visiting class: org/foo/A");
        result.assertLogText("Visiting class: org/bar/B");
        result.assertNoLogText("Packages that must be exported:");
    }
}
