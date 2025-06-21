package com.example.api.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    AuthIntegrationTest.class,
    MatchingIntegrationTest.class,
    EndToEndIntegrationTest.class
})
public class IntegrationTestSuite {
    // This class runs all integration tests as a suite
    // Run with: ./gradlew test --tests IntegrationTestSuite
}