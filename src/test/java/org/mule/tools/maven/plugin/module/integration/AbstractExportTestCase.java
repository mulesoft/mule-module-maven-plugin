/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.integration;

import static java.io.File.separator;
import static java.lang.System.arraycopy;
import static java.lang.System.getProperty;
import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.DUPLICATED_EXPORTED_PACKAGES;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.DUPLICATED_PRIVILEGED_EXPORTED_PACKAGES;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.MODULE_API_PROBLEMS_FOUND;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.NOT_ANALYZED_PACKAGES_ERROR;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.NO_MODULE_API_PROBLEMS_FOUND;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.PACKAGES_TO_EXPORT_ERROR;
import static org.mule.tools.maven.plugin.module.analyze.AnalyzeMojo.PRIVILEGED_PACKAGES_TO_EXPORT_ERROR;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
public abstract class AbstractExportTestCase {

  protected static final String PATH_CLASS_A = "org/foo/A";
  protected static final String PATH_CLASS_B = "org/bar/B";
  protected static final String CLASS_PATH_C = "org/foo/A$C";
  protected static final String INFO_LOG_PREFIX = "[INFO] ";
  protected static final String BAR_PACKAGE = "org.bar";
  protected static final String[] ANALYZED_CLASSES_A_B = {PATH_CLASS_A, PATH_CLASS_B};
  protected static final String[] ANALYZED_CLASSES_A_B_C = {PATH_CLASS_A, PATH_CLASS_B, CLASS_PATH_C};
  protected static final String PROJECT_VERSION = System.getProperty("surefire.project.version");
  protected static final String FOO_MODULE_ID = "Foo Module ";
  protected static final String BAR_MODULE_ID = "Bar Module ";
  private static final Pattern moduleTitleStart = Pattern.compile("\\[INFO\\]\\s-+<.*>-+");
  private static final String ANALYZING_CLASS_PREFIX = INFO_LOG_PREFIX + "Analyzing class: ";
  private static final String MAVEN_BUILD_PREFIX = "[INFO] Building ";

  @Rule
  public final TestResources resources = new TestResources();
  public final MavenRuntime mavenRuntime;
  private final String folder;

  public AbstractExportTestCase(MavenRuntime.MavenRuntimeBuilder builder, String folder) throws Exception {
    this.mavenRuntime = builder.withCliOptions("-DmuleModule.analyze.verbose", "--batch-mode",
                                               "-Dmaven.repo.local=" + getProperty("maven.repo.local", ""))
        .build();
    this.folder = folder;
  }

  /**
   * Tests a successful validation scenario where {@value PATH_CLASS_A} and {@value PATH_CLASS_B} classes are exported
   *
   * @param projectName name of the folder containing the maven project to test.
   * @throws Exception
   */
  protected void doSuccessfulStandardValidationTest(String projectName) throws Exception {
    doSuccessfulStandardValidationTest(projectName, ANALYZED_CLASSES_A_B);
  }

  /**
   * Tests a successful validation scenario of a standard API
   *
   * @param projectName     name of the folder containing the maven project to test.
   * @param analyzedClasses full paths of classes that should be analyzed during the test.
   * @throws Exception
   */
  protected void doSuccessfulStandardValidationTest(String projectName, String... analyzedClasses) throws Exception {
    doSuccessfulValidationTest(projectName, PACKAGES_TO_EXPORT_ERROR, analyzedClasses);
  }

  /**
   * Tests a successful validation scenario of a privileged API
   *
   * @param projectName     name of the folder containing the maven project to test.
   * @param analyzedClasses full paths of classes that should be analyzed during the test.
   * @throws Exception
   */
  protected void doSuccessfulPrivilegedValidationTest(String projectName, String... analyzedClasses) throws Exception {
    doSuccessfulValidationTest(projectName, PRIVILEGED_PACKAGES_TO_EXPORT_ERROR, analyzedClasses);
  }

  private void doSuccessfulValidationTest(String projectName, String packagesToExportError, String[] analyzedClasses)
      throws Exception {
    MavenExecutionResult result = runMaven(projectName);

    result.assertLogText(NO_MODULE_API_PROBLEMS_FOUND);
    result.assertLogText("Found module:");
    result.assertNoLogText(packagesToExportError);
    assertAnalyzedClasses(getLogLines(result), analyzedClasses);
  }

  /**
   * Tests a failing validation scenario where {@value PATH_CLASS_A} and {@value PATH_CLASS_B} classes should be exported
   *
   * @param projectName name of the folder containing the maven project to test.
   * @throws Exception
   */
  protected void doMissingStandardExportTest(String projectName) throws Exception {
    doMissingStandardExportTest(projectName, ANALYZED_CLASSES_A_B);
  }

  /**
   * Tests a failing validation scenario
   *
   * @param projectName     name of the folder containing the maven project to test.
   * @param analyzedClasses full paths of classes that should be analyzed during the test.
   * @throws Exception
   */
  protected void doMissingStandardExportTest(String projectName, String... analyzedClasses) throws Exception {
    doMissingExportTest(projectName, PACKAGES_TO_EXPORT_ERROR, analyzedClasses);
  }

  /**
   * Tests a failing validation scenario
   *
   * @param projectName     name of the folder containing the maven project to test.
   * @param analyzedClasses full paths of classes that should be analyzed during the test.
   * @throws Exception
   */
  protected void doMissingPrivilegedExportTest(String projectName, String... analyzedClasses) throws Exception {
    doMissingExportTest(projectName, PRIVILEGED_PACKAGES_TO_EXPORT_ERROR, analyzedClasses);
  }

  private void doMissingExportTest(String projectName, String packagesToExportError, String[] analyzedClasses) throws Exception {
    MavenExecutionResult result = runMaven(projectName);

    result.assertLogText(MODULE_API_PROBLEMS_FOUND);
    result.assertLogText("Found module:");
    List<String> logLines = getLogLines(result);
    assertAnalyzedClasses(logLines, analyzedClasses);
    assertMissingExportedPackages(logLines, packagesToExportError, BAR_PACKAGE);
  }

  /**
   * Tests a failing validation scenario caused by duplicated exported packages
   *
   * @param projectName        name of the folder containing the maven project to test.
   * @param duplicatedPackages packages that expected to be duplicated.
   * @throws Exception
   */
  protected void doDuplicatedExportTest(String projectName, String... duplicatedPackages)
      throws Exception {
    doDuplicatedExportTest(projectName, duplicatedPackages, DUPLICATED_EXPORTED_PACKAGES);
  }

  /**
   * Tests a failing validation scenario caused by duplicated privileged exported packages
   *
   * @param projectName        name of the folder containing the maven project to test.
   * @param duplicatedPackages packages that expected to be duplicated.
   * @throws Exception
   */
  protected void doDuplicatedPrivilegedExportTest(String projectName, String... duplicatedPackages)
      throws Exception {
    doDuplicatedExportTest(projectName, duplicatedPackages, DUPLICATED_PRIVILEGED_EXPORTED_PACKAGES);
  }

  private void doDuplicatedExportTest(String projectName, String[] duplicatedPackages, String errorMessage)
      throws Exception {
    MavenExecutionResult result = runMaven(projectName);

    result.assertLogText(MODULE_API_PROBLEMS_FOUND);
    result.assertLogText("Found module:");
    List<String> logLines = getLogLines(result);
    assertDuplicatedExportedPackages(logLines, errorMessage, duplicatedPackages);
  }

  /**
   * Tests a validation scenario failing because a package not analyzed
   *
   * @param projectName name of the folder containing the maven project to test.
   * @throws Exception
   */
  protected void doMissingAnalyzedPackageTest(String projectName) throws Exception {
    MavenExecutionResult result = runMaven(projectName);

    result.assertLogText(MODULE_API_PROBLEMS_FOUND);
    result.assertLogText("Found module:");
    assertMultiLogLine(getLogLines(result), INFO_LOG_PREFIX + NOT_ANALYZED_PACKAGES_ERROR, BAR_PACKAGE);
  }

  /**
   * Builds a single module with Maven
   *
   * @param projectName name of the folder containing the Maven project to test
   * @return the full Maven's log of the build process
   * @throws Exception
   */
  protected List<String> buildSingleModule(String projectName) throws Exception {
    MavenExecutionResult result = runMaven(projectName);

    return getLogLines(result);
  }

  /**
   * Builds a multi module with Maven
   *
   * @param projectName name of the folder containing the Maven project to test
   * @return a {@link Map} containing the separated Maven's log of each single module build
   * @throws Exception
   */
  protected Map<String, List<String>> buildMultiModule(String projectName) throws Exception {
    MavenExecutionResult result = runMaven(projectName);

    List<String> logLines = getLogLines(result);

    return splitLog(logLines);
  }

  /**
   * Asserts that there are the expected log entries regarding missing exported packages
   *
   * @param log          log lines to check
   * @param errorMessage
   * @param packages     packages that are expected to be missing
   */
  protected void assertMissingExportedPackages(List<String> log, String errorMessage, String... packages) {
    String[] lines = new String[packages.length + 1];
    lines[0] = INFO_LOG_PREFIX + errorMessage;
    arraycopy(packages, 0, lines, 1, packages.length);

    assertMultiLogLine(log, lines);
  }

  private void assertDuplicatedExportedPackages(List<String> log, String errorMessage, String... packages) {
    String[] lines = new String[packages.length + 1];
    lines[0] = INFO_LOG_PREFIX + errorMessage;
    arraycopy(packages, 0, lines, 1, packages.length);

    assertMultiLogLine(log, lines);
  }

  /**
   * Asserts that there are the expected log entries regarding missing analyzed classes
   *
   * @param log        log lines to check
   * @param classNames names of the classes that are expected to be analyzed
   */
  protected void assertAnalyzedClasses(List<String> log, String... classNames) {
    List<String> analyzedClassLines = log.stream().filter(s -> s.contains(ANALYZING_CLASS_PREFIX))
        .map(s -> s.substring(ANALYZING_CLASS_PREFIX.length())).collect(Collectors.toList());

    assertThat(analyzedClassLines, containsInAnyOrder(classNames));
  }

  /**
   * Asserts that the API validation was successful
   *
   * @param log log lines to check
   */
  protected void assertValidModuleApi(List<String> log) {
    assertThat(log, hasItem(containsString(NO_MODULE_API_PROBLEMS_FOUND)));
  }

  private MavenExecutionResult runMaven(String projectName) throws Exception {
    File basedir = resources.getBasedir(folder + separator + projectName);
    return mavenRuntime.forProject(basedir).execute("compile", "mule-module:analyze");
  }

  private Map<String, List<String>> splitLog(List<String> logLines) {
    Map<String, List<String>> result = new HashMap<>();

    int i = 0;
    while (i < logLines.size()) {
      String currentLine = logLines.get(i);
      if (moduleTitleStart.matcher(currentLine).find()) {
        if (i + 1 < logLines.size() && logLines.get(i + 1).startsWith(MAVEN_BUILD_PREFIX)) {
          int moduleLogStart = i;
          String moduleName = logLines.get(i + 1).substring(MAVEN_BUILD_PREFIX.length()).split(PROJECT_VERSION)[0];
          i = i + 3;

          while (i < logLines.size() && !(moduleTitleStart.matcher(logLines.get(i)).find())) {
            i++;
          }

          result.put(moduleName, logLines.subList(moduleLogStart, i));
        } else {
          i++;
        }
      } else {
        i++;
      }
    }
    return result;
  }

  private static void assertMultiLogLine(List<String> log, String... lines) {
    if (lines.length < 2) {
      throw new IllegalArgumentException("There must be more than one line to assert. Use MavenExecutionResult#assertLogText instead");
    }

    for (int i = 0; i < log.size(); i++) {
      if (i + lines.length > log.size()) {
        break;
      }
      int j = 0;
      while (j < lines.length && log.get(i + j).equals(lines[j])) {
        j++;
      }

      if (j == lines.length) {
        return;
      }
    }

    StringBuilder builder = new StringBuilder("Expected multi-line log: ");
    stream(lines).forEach(s -> builder.append("\n").append(s));
    builder.append("\nbut was");
    log.stream().forEach(s -> builder.append(s));

    throw new AssertionError(builder.toString());
  }

  private List<String> getLogLines(MavenExecutionResult result) {
    List<String> log;
    try {
      Field logField = result.getClass().getDeclaredField("log");
      logField.setAccessible(true);
      log = (List<String>) logField.get(result);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
    return log;
  }
}
