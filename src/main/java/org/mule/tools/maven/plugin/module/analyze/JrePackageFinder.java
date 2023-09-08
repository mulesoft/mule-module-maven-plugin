/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import static org.codehaus.plexus.util.PropertyUtils.loadProperties;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Lists all the packages defined in the JRE public API according to the version declared on the module being built.
 */
public class JrePackageFinder {

  private static final String JRE_PACKAGES_PROPERTIES = "jre-packages.properties";
  private static final String UNABLE_TO_DETERMINE_JRE_PACKAGES_ERROR = "Unable to determine packages exported by the JRE";

  private JrePackageFinder() {}

  /**
   ** @return the packages defined in the JRE API
   */
  public static Set<String> find() {
    try {
      final Properties properties = loadProperties(JrePackageFinder.class.getClassLoader().getResource(JRE_PACKAGES_PROPERTIES));

      // TODO(pablo.kraan): MULE-12497: Manage different JRE version on module maven plugin
      final String jreVersionProperty = "jre-1.8";
      if (!properties.keySet().contains(jreVersionProperty)) {
        throw new IllegalStateException(UNABLE_TO_DETERMINE_JRE_PACKAGES_ERROR);
      }

      final String packages = (String) properties.get(jreVersionProperty);
      final Set<String> result = new HashSet<>();
      for (String jrePackage : packages.split(",")) {
        jrePackage = jrePackage.trim();
        if (!jrePackage.isEmpty()) {
          result.add(jrePackage);
        }
      }

      return result;
    } catch (Exception e) {
      throw new IllegalStateException("Unable to determine JRE provided packages", e);
    }
  }

}
