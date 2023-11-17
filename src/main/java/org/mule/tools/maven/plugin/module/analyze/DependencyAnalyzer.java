/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import org.mule.tools.maven.plugin.module.common.ModuleLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Gets the set of classes referenced by a library given either as a jar file or an exploded directory.
 */
public interface DependencyAnalyzer {

  String ROLE = DependencyAnalyzer.class.getName();

  Map<String, Set<String>> analyze(URL url, ModuleLogger analyzerLogger) throws IOException;
}
