#!/bin/bash

# Integration Tests Runner for Binome Matcher
# This script runs all integration tests for both API and mobile components

set -e  # Exit on any error

echo "🚀 Starting Binome Matcher Integration Tests"
echo "============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if Docker is running (for API tests)
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker for API integration tests."
        exit 1
    fi
    print_success "Docker is running"
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    print_success "Java is available: $(java -version 2>&1 | head -n 1)"
    
    # Check if Android SDK is available (for mobile tests)
    if [[ "$RUN_MOBILE_TESTS" == "true" ]]; then
        if ! command -v adb &> /dev/null; then
            print_warning "ADB not found. Mobile tests may fail if no device/emulator is connected."
        else
            print_success "Android SDK tools are available"
        fi
    fi
}

# Function to run API integration tests
run_api_tests() {
    print_status "Running API Integration Tests..."
    echo "=================================="
    
    cd api
    
    # Clean previous test results
    ./gradlew clean
    
    # Run integration tests
    if ./gradlew test --tests IntegrationTestSuite --info; then
        print_success "API integration tests passed!"
        
        # Generate coverage report
        print_status "Generating coverage report..."
        ./gradlew jacocoTestReport
        
        # Display test results location
        echo ""
        print_status "API Test Results:"
        echo "  📊 Test Report: $(pwd)/build/reports/tests/test/index.html"
        echo "  📈 Coverage Report: $(pwd)/build/reports/jacoco/test/html/index.html"
        
    else
        print_error "API integration tests failed!"
        return 1
    fi
    
    cd ..
}

# Function to run mobile integration tests
run_mobile_tests() {
    print_status "Running Mobile Integration Tests..."
    echo "===================================="
    
    cd client
    
    # Check for connected devices/emulators
    if ! adb devices | grep -q "device$"; then
        print_error "No Android device or emulator found. Please connect a device or start an emulator."
        print_status "To start an emulator, run: emulator -avd <your_avd_name>"
        return 1
    fi
    
    print_success "Android device/emulator detected"
    
    # Clean previous test results
    ./gradlew clean
    
    # Run integration tests
    if ./gradlew connectedAndroidTest; then
        print_success "Mobile integration tests passed!"
        
        # Display test results location
        echo ""
        print_status "Mobile Test Results:"
        echo "  📱 Test Report: $(pwd)/app/build/reports/androidTests/connected/index.html"
        echo "  📸 Screenshots: $(pwd)/app/build/outputs/androidTest-results/connected/"
        
    else
        print_error "Mobile integration tests failed!"
        return 1
    fi
    
    cd ..
}

# Function to open test reports
open_reports() {
    if [[ "$OPEN_REPORTS" == "true" ]]; then
        print_status "Opening test reports..."
        
        # API reports
        if [[ -f "api/build/reports/tests/test/index.html" ]]; then
            if command -v xdg-open &> /dev/null; then
                xdg-open "api/build/reports/tests/test/index.html"
            elif command -v open &> /dev/null; then
                open "api/build/reports/tests/test/index.html"
            fi
        fi
        
        # Mobile reports
        if [[ -f "client/app/build/reports/androidTests/connected/index.html" ]]; then
            if command -v xdg-open &> /dev/null; then
                xdg-open "client/app/build/reports/androidTests/connected/index.html"
            elif command -v open &> /dev/null; then
                open "client/app/build/reports/androidTests/connected/index.html"
            fi
        fi
    fi
}

# Function to display summary
display_summary() {
    echo ""
    echo "🎯 Integration Test Summary"
    echo "=========================="
    
    if [[ "$API_TESTS_PASSED" == "true" ]]; then
        print_success "✅ API Integration Tests: PASSED"
    else
        print_error "❌ API Integration Tests: FAILED"
    fi
    
    if [[ "$RUN_MOBILE_TESTS" == "true" ]]; then
        if [[ "$MOBILE_TESTS_PASSED" == "true" ]]; then
            print_success "✅ Mobile Integration Tests: PASSED"
        else
            print_error "❌ Mobile Integration Tests: FAILED"
        fi
    else
        print_warning "⏭️  Mobile Integration Tests: SKIPPED"
    fi
    
    echo ""
    print_status "📚 Documentation: INTEGRATION_TESTING_GUIDE.md"
    echo ""
}

# Parse command line arguments
RUN_API_TESTS=true
RUN_MOBILE_TESTS=true
OPEN_REPORTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --api-only)
            RUN_MOBILE_TESTS=false
            shift
            ;;
        --mobile-only)
            RUN_API_TESTS=false
            shift
            ;;
        --open-reports)
            OPEN_REPORTS=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --api-only       Run only API integration tests"
            echo "  --mobile-only    Run only mobile integration tests"
            echo "  --open-reports   Open test reports in browser after completion"
            echo "  --help           Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                    # Run all tests"
            echo "  $0 --api-only        # Run only API tests"
            echo "  $0 --mobile-only     # Run only mobile tests"
            echo "  $0 --open-reports    # Run all tests and open reports"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Main execution
main() {
    local start_time=$(date +%s)
    
    # Check prerequisites
    check_prerequisites
    
    # Initialize test result flags
    API_TESTS_PASSED=false
    MOBILE_TESTS_PASSED=false
    
    # Run API tests
    if [[ "$RUN_API_TESTS" == "true" ]]; then
        if run_api_tests; then
            API_TESTS_PASSED=true
        fi
    fi
    
    # Run mobile tests
    if [[ "$RUN_MOBILE_TESTS" == "true" ]]; then
        if run_mobile_tests; then
            MOBILE_TESTS_PASSED=true
        fi
    fi
    
    # Open reports if requested
    open_reports
    
    # Display summary
    display_summary
    
    # Calculate execution time
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    print_status "Total execution time: ${duration} seconds"
    
    # Exit with appropriate code
    if [[ "$RUN_API_TESTS" == "true" && "$API_TESTS_PASSED" != "true" ]]; then
        exit 1
    fi
    
    if [[ "$RUN_MOBILE_TESTS" == "true" && "$MOBILE_TESTS_PASSED" != "true" ]]; then
        exit 1
    fi
    
    print_success "🎉 All requested integration tests completed successfully!"
}

# Run main function
main "$@"