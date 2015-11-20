/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.javascript.unittest.jstestdriver;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.javascript.JavaScriptLanguage;
import org.sonar.plugins.javascript.JavaScriptPlugin;
import org.sonar.test.TestUtils;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsTestDriverSensorTest {

  private static final File PROJECT_BASE_DIR = TestUtils.getResource("org/sonar/plugins/javascript/unittest/jstestdriver/sensortests");

  private JsTestDriverSensor sensor;
  private SensorContext context;
  private Settings settings;
  private final Project project = new Project("project");

  @Before
  public void init() {
    DefaultFileSystem fileSystem = new DefaultFileSystem();
    fileSystem.setBaseDir(PROJECT_BASE_DIR);
    fileSystem.add(newTestInputFile("test/AnotherPersonTest.js"));
    fileSystem.add(newTestInputFile("test/PersonTest.js"));

    settings = new Settings();
    sensor = new JsTestDriverSensor(fileSystem, settings);
    context = mock(SensorContext.class);

  }

  @Test
  public void test_shouldExecuteOnProject() {
    DefaultFileSystem localFS = new DefaultFileSystem();
    JsTestDriverSensor localSensor = sensor = new JsTestDriverSensor(localFS, settings);
    context = mock(SensorContext.class);

    // Not a JavaScript project
    assertThat(localSensor.shouldExecuteOnProject(project)).isFalse();

    // No report path provided
    assertThat(localSensor.shouldExecuteOnProject(project)).isFalse();

    settings.setProperty(JavaScriptPlugin.JSTESTDRIVER_REPORTS_PATH, "jstestdriver");
    localFS.add(new DefaultInputFile("File.jsp").setLanguage(JavaScriptLanguage.KEY).setType(InputFile.Type.MAIN));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void testAnalyseUnitTests() {
    settings.setProperty(JavaScriptPlugin.JSTESTDRIVER_REPORTS_PATH, "reports/jstestdriver");
    when(context.getResource(any(InputFile.class))).thenReturn(org.sonar.api.resources.File.create("test/PersonTest.js"));

    sensor.analyse(project, context);

    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.TESTS), eq(2.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.SKIPPED_TESTS), eq(0.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.TEST_ERRORS), eq(0.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.TEST_FAILURES), eq(0.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.TEST_EXECUTION_TIME), eq(700.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.TEST_SUCCESS_DENSITY), eq(100.0));
  }

  @Test
  public void wrong_file_name_in_report() {
    settings.setProperty(JavaScriptPlugin.JSTESTDRIVER_REPORTS_PATH, "reports/wrong-jstestdriver-report");

    sensor.analyse(project, context);

    verify(context, never()).saveMeasure(any(Resource.class), any(Metric.class), anyDouble());
  }

  @Test
  public void testGetUnitTestFileName() {
    String fileSeparator = File.separator;

    assertEquals("com" + fileSeparator + "company" + fileSeparator + "PersonTest.js", sensor.getUnitTestFileName("Chrome_16091263_Windows.com.company.PersonTest"));
    assertEquals("PersonTest.js", sensor.getUnitTestFileName("Chrome_16091263_Windows.PersonTest"));
  }

  @Test
  public void get_testfile_with_common_suffix_filename() {
    InputFile inputFile1 = sensor.getTestFileRelativePathToBaseDir("PersonTest.js");
    assertNotNull(inputFile1);
    assertEquals("test/PersonTest.js", inputFile1.relativePath());

    InputFile inputFile2 = sensor.getTestFileRelativePathToBaseDir("AnotherPersonTest.js");
    assertNotNull(inputFile2);
    assertEquals("test/AnotherPersonTest.js", inputFile2.relativePath());
  }

  @Test
  public void test_toString() {
    assertThat(sensor.toString()).isEqualTo("JsTestDriverSensor");
  }

  public DefaultInputFile newTestInputFile(String relativePath) {
    return new DefaultInputFile(relativePath)
      .setAbsolutePath("absolute/path/" + relativePath)
      .setType(InputFile.Type.TEST)
      .setLanguage(JavaScriptLanguage.KEY);
  }


}
