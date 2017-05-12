/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Project dependencies analysis result.
 */
public class ProjectDependencyAnalysis {

  private final Map<String, Set<String>> undeclaredPackageDeps;
  private final Set<String> packagesToExport;

  public ProjectDependencyAnalysis() {
    this(new HashMap<String, Set<String>>(), new HashSet<String>());
  }


  public ProjectDependencyAnalysis(Map<String, Set<String>> undeclaredPackageDeps, Set<String> packagesToExport) {

    this.undeclaredPackageDeps = undeclaredPackageDeps;
    this.packagesToExport = packagesToExport;
  }

  /**
   * Exported classes that are not declared
   */
  public Map<String, Set<String>> getUndeclaredPackageDeps() {
    return undeclaredPackageDeps;
  }

  public Set<String> getPackagesToExport() {
    return packagesToExport;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int hashCode = getUndeclaredPackageDeps().hashCode();
    hashCode = (hashCode * 37) + getPackagesToExport().hashCode();

    return hashCode;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object object) {
    if (object instanceof ProjectDependencyAnalysis) {
      ProjectDependencyAnalysis analysis = (ProjectDependencyAnalysis) object;

      return getUndeclaredPackageDeps().equals(analysis.getUndeclaredPackageDeps())
          && getPackagesToExport().equals(analysis.getPackagesToExport());
    }

    return false;
  }

  /*
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuilder buffer = new StringBuilder();

    if (!getUndeclaredPackageDeps().isEmpty()) {
      buffer.append("undeclaredPackagesDeps=").append(getUndeclaredPackageDeps());
    }

    if (!getPackagesToExport().isEmpty()) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }

      buffer.append("packagesToExport=").append(getPackagesToExport());
    }

    buffer.insert(0, "[");
    buffer.insert(0, getClass().getName());

    buffer.append("]");

    return buffer.toString();
  }

  private Set<String> safeCopy(Set<String> set) {
    return (set == null) ? Collections.<String>emptySet()
        : Collections.unmodifiableSet(new LinkedHashSet<String>(set));
  }
}
