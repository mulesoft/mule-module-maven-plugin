/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class AnnotationsExportTestCase extends AbstractExportTestCase
{

    public AnnotationsExportTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        super(builder, "annotation");
    }

    @Test
    public void exportedRuntimeAnnotationInPublicClass() throws Exception
    {
        doExportABTest("exportedRuntimeAnnotationInPublicClass");
    }

    @Test
    public void missingRuntimeAnnotationInPublicClass() throws Exception
    {
        final String missingRuntimeAnnotationInPublicClass = "missingRuntimeAnnotationInPublicClass";
        doExportAMissingBTest(missingRuntimeAnnotationInPublicClass);
    }

    @Test
    public void ignoresCompileAnnotationInPublicClass() throws Exception
    {
        doExportABTest("ignoresCompileAnnotationInPublicClass");
    }

    @Test
    public void ignoresSourceAnnotationInPublicClass() throws Exception
    {
        doExportABTest("ignoresSourceAnnotationInPublicClass");
    }

    @Test
    public void ignoresRuntimeAnnotationInPackageClass() throws Exception
    {
        doExportABTest("ignoresRuntimeAnnotationInPackageClass");
    }

    @Test
    public void exportedRuntimeAnnotationInPublicMethod() throws Exception
    {
        //TODO(pablo.kraan): what about private/protected/package methods?
        doExportABTest("exportedRuntimeAnnotationInPublicMethod");
    }

    @Test
    public void missingRuntimeAnnotationInPublicMethod() throws Exception
    {
        doExportAMissingBTest("missingRuntimeAnnotationInPublicMethod");
    }

    @Test
    public void ignoresCompileAnnotationInPublicMethod() throws Exception
    {
        doExportABTest("ignoresCompileAnnotationInPublicMethod");
    }

    @Test
    public void ignoresSourceAnnotationInPublicMethod() throws Exception
    {
        doExportABTest("ignoresSourceAnnotationInPublicMethod");
    }

    @Test
    public void ignoresRuntimeAnnotationInPackageMethod() throws Exception
    {
        doExportABTest("ignoresRuntimeAnnotationInPackageMethod");
    }

    @Test
    public void exportedRuntimeAnnotationInPublicField() throws Exception
    {
        doExportABTest("exportedRuntimeAnnotationInPublicField");
    }

    @Test
    public void missingRuntimeAnnotationInPublicField() throws Exception
    {
        doExportAMissingBTest("missingRuntimeAnnotationInPublicField");
    }

    @Test
    public void ignoresCompileAnnotationInPublicField() throws Exception
    {
        doExportABTest("ignoresCompileAnnotationInPublicField");
    }

    @Test
    public void ignoresSourceAnnotationInPublicField() throws Exception
    {
        doExportABTest("ignoresSourceAnnotationInPublicField");
    }

    @Test
    public void ignoresRuntimeAnnotationInPackageField() throws Exception
    {
        doExportABTest("ignoresRuntimeAnnotationInPackageMethod");
    }

    @Test
    public void exportedRuntimeAnnotationInPublicMethodParam() throws Exception
    {
        doExportABTest("exportedRuntimeAnnotationInPublicMethodParam");
    }

    @Test
    public void missingRuntimeAnnotationInPublicMethodParam() throws Exception
    {
        doExportAMissingBTest("missingRuntimeAnnotationInPublicMethodParam");
    }

    @Test
    public void ignoresCompileAnnotationInPublicMethodParam() throws Exception
    {
        doExportABTest("ignoresCompileAnnotationInPublicMethodParam");
    }

    @Test
    public void ignoresSourceAnnotationInPublicMethodParam() throws Exception
    {
        doExportABTest("ignoresSourceAnnotationInPublicMethodParam");
    }

    @Test
    public void ignoresRuntimeAnnotationInPackageMethodParam() throws Exception
    {
        doExportABTest("ignoresRuntimeAnnotationInPackageMethodParam");
    }
}
