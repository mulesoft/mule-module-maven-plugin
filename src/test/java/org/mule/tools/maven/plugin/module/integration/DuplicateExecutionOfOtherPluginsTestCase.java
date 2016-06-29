/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.integration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Ignore;
import org.junit.Test;

public class DuplicateExecutionOfOtherPluginsTestCase extends AbstractExportTestCase
{

    public DuplicateExecutionOfOtherPluginsTestCase(MavenRuntime.MavenRuntimeBuilder builder) throws Exception
    {
        super(builder);
    }

    @Ignore
    @Test
    public void duplicateExecutionOfOtherPlugins() throws Exception
    {
        File basedir = resources.getBasedir("duplicateExecutionOfOtherPlugins");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("verify");

        // I expect that the plugins that aren't involve in the mule-module-maven-plugin tasks just are executed once.
        assertEquals("1", obtainNumberOfAppearancesOfTextInLog(result, "process-sources exec"));
    }


    private int obtainNumberOfAppearancesOfTextInLog(MavenExecutionResult result, String text) throws IOException
    {

        List<String> logUnmodifiable = obtainLogFromFile(result);

        Iterator var3 = logUnmodifiable.iterator();

        int count = 0;

        while(var3.hasNext()) {
            String line = (String)var3.next();
            if(line.contains(text)) {
                count++;
            }
        }

        return count;
    }

    private List<String> obtainLogFromFile(MavenExecutionResult result) throws IOException
    {
        File basedir = result.getBasedir();
        List<String> log = new ArrayList();
        File logFile = new File(basedir.getPath() + "/log.txt");
        if(logFile.canRead()) {
            Iterator var5 = Files.readAllLines(logFile.toPath(), Charset.defaultCharset()).iterator();

            while(var5.hasNext()) {
                String line = (String)var5.next();
                log.add(line);
            }
        }

        return Collections.unmodifiableList(log);
    }

}
