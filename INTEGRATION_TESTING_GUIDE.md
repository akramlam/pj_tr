# Integration Testing Guide

This document provides a comprehensive guide to the integration tests for the Binome Matcher application, covering both the API backend and Android mobile client.

## Overview

Our integration testing strategy covers:
- **API Integration Tests**: Full API workflows with real database using TestContainers
- **Mobile Integration Tests**: End-to-end Android UI flows with mocked API responses
- **Cross-platform Integration**: Complete user journeys from mobile app to API

## API Integration Tests

### Test Structure

All API integration tests are located in `api/src/test/java/com/example/api/integration/` and extend `ApiIntegrationTestBase`.

#### Key Features:
- **TestContainers**: Real PostgreSQL database for each test
- **Isolated Environment**: Each test runs with a fresh database
- **Full HTTP Testing**: Tests actual HTTP requests/responses
- **JWT Authentication**: Real token generation and validation

### Test Classes

#### 1. AuthIntegrationTest
Tests authentication and authorization flows:
- ✅ User registration with validation
- ✅ Login with valid/invalid credentials
- ✅ JWT token generation and validation
- ✅ Protected endpoint access control
- ✅ Email and password validation
- ✅ Error handling for duplicate users

#### 2. MatchingIntegrationTest
Tests the core matching functionality:
- ✅ Profile creation with different formations and skills
- ✅ Finding potential matches based on compatibility
- ✅ Matching algorithm accuracy (same formation prioritized)
- ✅ Like/Pass functionality
- ✅ Cross-user visibility validation
- ✅ Error handling for invalid requests

#### 3. EndToEndIntegrationTest
Tests complete user journeys:
- ✅ Registration → Profile Creation → Matching → Messaging
- ✅ Multiple user interactions and visibility
- ✅ Compatibility scoring validation
- ✅ Complete workflow validation
- ✅ Error handling throughout the journey

### Running API Integration Tests

#### Linux/macOS:
```bash
# Run all API integration tests
cd api
./gradlew test

# Run specific test class
./gradlew test --tests AuthIntegrationTest

# Run the complete integration test suite
./gradlew test --tests IntegrationTestSuite

# Run tests with detailed output
./gradlew test --info

# Run tests and generate coverage report
./gradlew test jacocoTestReport
```

#### Windows:
```powershell
# Run all API integration tests
cd api
.\gradlew.bat test

# Run specific test class
.\gradlew.bat test --tests AuthIntegrationTest

# Run the complete integration test suite
.\gradlew.bat test --tests IntegrationTestSuite

# Run tests with detailed output
.\gradlew.bat test --info

# Run tests and generate coverage report
.\gradlew.bat test jacocoTestReport
```

### API Test Coverage

The integration tests cover:
- **Authentication**: Registration, Login, JWT validation
- **Profile Management**: Creation, Retrieval, Updates
- **Matching Algorithm**: Finding matches, Compatibility scoring
- **Match Requests**: Like/Pass functionality, Mutual matching
- **Error Handling**: Validation, Network errors, Invalid data
- **Security**: Protected endpoints, Token validation

## Mobile Integration Tests

### Test Structure

All Android integration tests are located in `client/app/src/androidTest/java/com/example/client/integration/` and extend `BaseIntegrationTest`.

#### Key Features:
- **Espresso UI Testing**: Real UI interactions and validations
- **MockWebServer**: Controlled API response mocking
- **Intent Testing**: Navigation and activity transitions
- **Network Simulation**: Various network conditions and errors

### Test Classes

#### 1. LoginFlowIntegrationTest
Tests authentication UI flows:
- ✅ Login form validation and submission
- ✅ Registration form with email/password validation
- ✅ Success/failure flow handling
- ✅ Network error handling
- ✅ Loading states and button behaviors
- ✅ Navigation to main app after successful auth

#### 2. MainAppIntegrationTest
Tests main application functionality:
- ✅ Navigation drawer functionality
- ✅ Profile creation and editing
- ✅ Potential matches display and interaction
- ✅ Swipe gestures (Like/Pass)
- ✅ Messaging interface
- ✅ Pull-to-refresh functionality
- ✅ Offline mode handling
- ✅ Settings and logout flows

### Running Mobile Integration Tests

#### Linux/macOS:
```bash
# Run all Android integration tests
cd client
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.client.integration.LoginFlowIntegrationTest

# Run tests on specific device
./gradlew connectedDebugAndroidTest

# Generate test report
./gradlew connectedAndroidTest --continue
```

#### Windows:
```powershell
# Run all Android integration tests
cd client
.\gradlew.bat connectedAndroidTest

# Run specific test class
.\gradlew.bat connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.client.integration.LoginFlowIntegrationTest

# Run tests on specific device
.\gradlew.bat connectedDebugAndroidTest

# Generate test report
.\gradlew.bat connectedAndroidTest --continue
```

**Note**: Android integration tests require:
- Connected Android device or running emulator
- API level 21+ (as specified in build.gradle.kts)

### Mobile Test Coverage

The integration tests cover:
- **Authentication UI**: Login/Registration forms, validation, error handling
- **Navigation**: Drawer navigation, fragment transitions
- **Profile Management**: Form inputs, data validation, API integration
- **Matching Interface**: Card display, swipe gestures, API calls
- **Messaging**: Conversation list, message sending, real-time updates
- **Error Handling**: Network errors, offline mode, validation errors
- **User Experience**: Loading states, refresh functionality, navigation flow

## Test Data and Scenarios

### API Test Scenarios

```java
// Example test data used in integration tests
User alice = {
    username: "alice",
    email: "alice@university.com", 
    formation: "Computer Science",
    skills: ["Java", "Python", "Machine Learning"]
}

User bob = {
    username: "bob",
    email: "bob@university.com",
    formation: "Computer Science", 
    skills: ["Java", "React", "NodeJS"]
}

User charlie = {
    username: "charlie",
    email: "charlie@business.com",
    formation: "Business Administration",
    skills: ["Marketing", "Finance"]
}
```

### Expected Test Outcomes

1. **Alice and Bob** should have high compatibility (same formation, common skill: Java)
2. **Alice and Charlie** should have lower compatibility (different formations)
3. **Matching algorithm** should prioritize formation similarity
4. **All users** should be able to see each other as potential matches
5. **Like/Pass actions** should be recorded and processed correctly

## Continuous Integration

### Running Tests in CI/CD

```yaml
# Example GitHub Actions workflow
name: Integration Tests
on: [push, pull_request]

jobs:
  api-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run API Integration Tests
        run: |
          cd api
          ./gradlew test --tests IntegrationTestSuite

  android-tests:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run Android Integration Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: |
            cd client
            ./gradlew connectedAndroidTest
```

## Performance Considerations

### API Test Performance
- **TestContainers startup**: ~10-15 seconds per test class
- **Database isolation**: Each test gets fresh database
- **Parallel execution**: Tests can run in parallel with different containers

### Mobile Test Performance
- **Emulator startup**: ~30-60 seconds
- **UI interactions**: Real UI rendering and interaction
- **Network mocking**: Fast response times with MockWebServer

## Troubleshooting

### Common API Test Issues

1. **TestContainers not starting**:
   
   **Linux/macOS:**
   ```bash
   # Ensure Docker is running
   docker --version
   docker ps
   ```
   
   **Windows:**
   ```powershell
   # Ensure Docker Desktop is running
   docker --version
   docker ps
   ```

2. **Port conflicts**:
   
   **Linux/macOS:**
   ```bash
   # Tests use random ports, but check for conflicts
   netstat -tulpn | grep :5432
   ```
   
   **Windows:**
   ```powershell
   # Check for port conflicts
   netstat -an | findstr :5432
   ```

3. **Database connection issues**:
   
   **Linux/macOS:**
   ```bash
   # Check database logs in test output
   ./gradlew test --info --stacktrace
   ```
   
   **Windows:**
   ```powershell
   # Check database logs in test output
   .\gradlew.bat test --info --stacktrace
   ```

### Common Mobile Test Issues

1. **Emulator not responding**:
   ```bash
   # Restart emulator (same on all platforms)
   adb kill-server
   adb start-server
   ```

2. **Test timeouts**:
   ```kotlin
   // Increase timeouts in test configuration
   android {
       testOptions {
           animationsDisabled = true
       }
   }
   ```

3. **View not found errors**:
   
   **Linux/macOS:**
   ```bash
   # Enable layout inspection
   ./gradlew connectedAndroidTest --info
   ```
   
   **Windows:**
   ```powershell
   # Enable layout inspection
   .\gradlew.bat connectedAndroidTest --info
   ```

## Test Reporting

### API Test Reports
- **Location**: `api/build/reports/tests/test/index.html`
- **Coverage**: `api/build/reports/jacoco/test/html/index.html`

### Mobile Test Reports
- **Location**: `client/app/build/reports/androidTests/connected/index.html`
- **Screenshots**: `client/app/build/outputs/androidTest-results/connected/`

## Best Practices

### API Integration Testing
1. **Use real database**: TestContainers provides realistic environment
2. **Test complete workflows**: Don't just test individual endpoints
3. **Validate business logic**: Ensure matching algorithms work correctly
4. **Test error scenarios**: Network failures, invalid data, etc.
5. **Isolate tests**: Each test should be independent

### Mobile Integration Testing
1. **Mock external dependencies**: Use MockWebServer for API calls
2. **Test user journeys**: Complete flows from start to finish
3. **Validate UI states**: Loading, error, success states
4. **Test gestures and interactions**: Swipes, clicks, navigation
5. **Handle timing issues**: Use appropriate waits and timeouts

## Future Enhancements

### Planned Test Improvements
1. **Performance testing**: Load testing for API endpoints
2. **Accessibility testing**: UI accessibility validation
3. **Device testing**: Multiple device sizes and Android versions
4. **Network testing**: Various network conditions simulation
5. **Security testing**: Penetration testing and vulnerability scanning

### Test Automation
1. **Automated test execution**: CI/CD pipeline integration
2. **Test data management**: Automated test data generation
3. **Visual regression testing**: UI screenshot comparison
4. **Cross-platform testing**: iOS integration tests
5. **End-to-end testing**: Full system integration tests

---

## Quick Start

### For Linux/macOS:
```bash
# 1. Make script executable and run all tests
chmod +x run_integration_tests.sh
./run_integration_tests.sh

# Or use specific options:
./run_integration_tests.sh --api-only        # API tests only
./run_integration_tests.sh --mobile-only     # Mobile tests only  
./run_integration_tests.sh --open-reports    # Run all + open reports

# Manual execution
cd api && ./gradlew test --tests IntegrationTestSuite
cd client && ./gradlew connectedAndroidTest
```

### For Windows (PowerShell):
```powershell
# Run all tests
.\run_integration_tests.ps1

# Or use specific options:
.\run_integration_tests.ps1 -ApiOnly        # API tests only
.\run_integration_tests.ps1 -MobileOnly     # Mobile tests only  
.\run_integration_tests.ps1 -OpenReports    # Run all + open reports

# Manual execution
cd api; .\gradlew.bat test --tests IntegrationTestSuite
cd client; .\gradlew.bat connectedAndroidTest
```

### View reports:
```powershell
# Windows - reports will auto-open with -OpenReports flag, or manually:
start api\build\reports\tests\test\index.html
start client\app\build\reports\androidTests\connected\index.html
```

This comprehensive integration testing suite ensures that both the API and mobile components work correctly together and independently, providing confidence in the application's reliability and user experience on all platforms! 🎯