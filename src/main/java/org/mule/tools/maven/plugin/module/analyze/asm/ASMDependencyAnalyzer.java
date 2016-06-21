/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze.asm;

import org.mule.tools.maven.plugin.module.analyze.ClassFileVisitorUtils;
import org.mule.tools.maven.plugin.module.analyze.DependencyAnalyzer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = DependencyAnalyzer.class)
public class ASMDependencyAnalyzer
        implements DependencyAnalyzer
{

    /*
     * @see org.mule.tools.maven.plugin.module.analyze.DependencyAnalyzer#analyze(java.net.URL)
     */
    public Map<String, Set<String>> analyze(URL url)
            throws IOException
    {
        DependencyClassFileVisitor visitor = new DependencyClassFileVisitor();

        ClassFileVisitorUtils.accept(url, visitor);

        return visitor.getPackageDeps();
    }
}
