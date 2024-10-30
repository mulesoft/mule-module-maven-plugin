/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze;

import java.util.Map;
import java.util.Set;

/**
 * Project dependencies analysis result.
 */
public class ApiAnalysisResult {

  private final Map<String, Set<String>> undeclaredPackageDeps;
  private final Set<String> packagesToExport;
  private final Set<String> notAnalyzedPackages;
  private final Set<String> duplicatedPackages;
  private final Set<String> exportedPackageClosure;

  public ApiAnalysisResult(Map<String, Set<String>> undeclaredPackageDeps, Set<String> packagesToExport,
                           Set<String> notAnalyzedPackages, Set<String> duplicatedPackages,
                           Set<String> exportedPackageClosure) {

    this.undeclaredPackageDeps = undeclaredPackageDeps;
    this.packagesToExport = packagesToExport;
    this.notAnalyzedPackages = notAnalyzedPackages;
    this.duplicatedPackages = duplicatedPackages;
    this.exportedPackageClosure = exportedPackageClosure;
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

  public Set<String> getNotAnalyzedPackages() {
    return notAnalyzedPackages;
  }

  public Set<String> getDuplicatedPackages() {
    return duplicatedPackages;
  }

  public Set<String> getExportedPackageClosure() {
    return exportedPackageClosure;
  }

  @Override
  public int hashCode() {
    int hashCode = getUndeclaredPackageDeps().hashCode();
    hashCode = (hashCode * 37) + getPackagesToExport().hashCode();
    hashCode = (hashCode * 37) + getNotAnalyzedPackages().hashCode();

    return hashCode;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof ApiAnalysisResult) {
      ApiAnalysisResult analysis = (ApiAnalysisResult) object;

      return getUndeclaredPackageDeps().equals(analysis.getUndeclaredPackageDeps())
          && getPackagesToExport().equals(analysis.getPackagesToExport())
          && getNotAnalyzedPackages().equals(analysis.getNotAnalyzedPackages());
    }

    return false;
  }

  @Override
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

    if (!getNotAnalyzedPackages().isEmpty()) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }

      buffer.append("notAnalyzedPackages=").append(getNotAnalyzedPackages());
    }

    buffer.insert(0, "[");
    buffer.insert(0, getClass().getName());

    buffer.append("]");

    return buffer.toString();
  }
}
