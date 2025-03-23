package com.orangehrm.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;


/**
 * TestRunner class for executing Cucumber tests.
 * <p>
 * This class is configured to:
 * - Execute as a JUnit platform test suite
 * - Use only the Cucumber test engine
 * - Find feature files in the resources/features directory
 * - Generate console, HTML, and JSON reports
 * - Use step definitions and hooks from specified packages
 * - Filter scenarios to run only those tagged with @Sanity and not @Ignore
 * - Enable parallel test execution
 * - Use dynamic parallelism that automatically determines the optimal number
 *   of parallel threads based on available CPU cores
 */

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/report.html, json:target/cucumber-reports/report.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.orangehrm.stepdefinitions, com.orangehrm.hooks")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Sanity and not @Ignore")
@ConfigurationParameter(key = PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, value = "true")
@ConfigurationParameter(key = PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, value = "dynamic")
public class TestRunner {
}