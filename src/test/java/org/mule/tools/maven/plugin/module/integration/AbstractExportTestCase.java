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
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
public abstract class AbstractExportTestCase
{

    @Rule
    public final TestResources resources = new TestResources();
    public final MavenRuntime mavenRuntime;

    public AbstractExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        this.mavenRuntime = builder.withCliOptions("-DmuleModule.analyze.verbose").build();
    }

    protected void doExportABTest(String projectName) throws Exception
    {
        //TODO(pablo.kraan): this test should be different when org.bar is exported in the module, otherwise there is no safety that the code is doing the real thing
        File basedir = resources.getBasedir(projectName);
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(NO_DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Analyzing class: org/foo/A");
        result.assertLogText("Analyzing class: org/bar/B");
        result.assertNoLogText("Packages that must be exported:");
    }

    protected void doExportAMissingBTest(String projectName) throws Exception
    {
        File basedir = resources.getBasedir(projectName);
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("mule-module:analyze");

        result.assertLogText(DEPENDENCY_PROBLEMS_FOUND);
        result.assertLogText("Found module:");
        result.assertLogText("Analyzing class: org/foo/A");
        result.assertLogText("Analyzing class: org/bar/B");
        //TODO(pablo.kraan): what about tests using C class?
        //result.assertLogText("Analyzing class: org/bar/A$C");
        result.assertLogText("Packages that must be exported:");
        result.assertLogText("org.bar");
    }
}
