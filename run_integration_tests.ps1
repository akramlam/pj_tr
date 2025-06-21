# Integration Tests Runner for Binome Matcher (Windows PowerShell)
# This script runs all integration tests for both API and mobile components

param(
    [switch]$ApiOnly,
    [switch]$MobileOnly,
    [switch]$OpenReports,
    [switch]$Help
)

# Enable strict mode for better error handling
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Display help if requested
if ($Help) {
    Write-Host "Usage: .\run_integration_tests.ps1 [OPTIONS]" -ForegroundColor White
    Write-Host ""
    Write-Host "Options:" -ForegroundColor White
    Write-Host "  -ApiOnly       Run only API integration tests" -ForegroundColor Gray
    Write-Host "  -MobileOnly    Run only mobile integration tests" -ForegroundColor Gray
    Write-Host "  -OpenReports   Open test reports in browser after completion" -ForegroundColor Gray
    Write-Host "  -Help          Show this help message" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor White
    Write-Host "  .\run_integration_tests.ps1                  # Run all tests" -ForegroundColor Gray
    Write-Host "  .\run_integration_tests.ps1 -ApiOnly        # Run only API tests" -ForegroundColor Gray
    Write-Host "  .\run_integration_tests.ps1 -MobileOnly     # Run only mobile tests" -ForegroundColor Gray
    Write-Host "  .\run_integration_tests.ps1 -OpenReports    # Run all tests and open reports" -ForegroundColor Gray
    exit 0
}

Write-Host "🚀 Starting Binome Matcher Integration Tests" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# Functions for colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Function to check prerequisites
function Test-Prerequisites {
    Write-Status "Checking prerequisites..."
    
    # Check if Docker is running (for API tests)
    if (-not $MobileOnly) {
        try {
            $dockerInfo = docker info 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Docker is running"
            } else {
                throw "Docker command failed"
            }
        }
        catch {
            Write-Error "Docker is not running. Please start Docker Desktop for API integration tests."
            exit 1
        }
    }
    
    # Check if Java is installed
    try {
        $javaVersion = java -version 2>&1 | Select-Object -First 1
        Write-Success "Java is available: $javaVersion"
    }
    catch {
        Write-Error "Java is not installed or not in PATH"
        exit 1
    }
    
    # Check if Gradle wrapper exists
    if (-not $MobileOnly) {
        if (-not (Test-Path "gradlew.bat")) {
            Write-Error "gradlew.bat not found in root directory. Make sure you're running from the project root."
            exit 1
        }
        Write-Success "Gradle wrapper found"
    }
    
    # Check if Android SDK is available (for mobile tests)
    if (-not $ApiOnly) {
        try {
            $adbVersion = adb version 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Android SDK tools are available"
                return $true
            } else {
                Write-Warning "ADB not found. You can install Android SDK or run with -ApiOnly to skip mobile tests."
                return $false
            }
        }
        catch {
            Write-Warning "ADB not found. You can install Android SDK or run with -ApiOnly to skip mobile tests."
            return $false
        }
    }
    return $true
}

# Function to run API integration tests
function Invoke-ApiTests {
    Write-Status "Running API Integration Tests..."
    Write-Host "==================================" -ForegroundColor White
    
    try {
        # Clean previous test results
        Write-Status "Cleaning previous test results..."
        & ".\gradlew.bat" clean --project-dir api
        
        # Run integration tests
        Write-Status "Running integration test suite..."
        & ".\gradlew.bat" test --tests IntegrationTestSuite --project-dir api --info
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "API integration tests passed!"
            
            # Generate coverage report
            Write-Status "Generating coverage report..."
            & ".\gradlew.bat" jacocoTestReport --project-dir api
            
            # Display test results location
            Write-Host ""
            Write-Status "API Test Results:"
            $currentPath = Get-Location
            Write-Host "  📊 Test Report: $currentPath\api\build\reports\tests\test\index.html"
            Write-Host "  📈 Coverage Report: $currentPath\api\build\reports\jacoco\test\html\index.html"
            
            return $true
        } else {
            Write-Error "API integration tests failed!"
            Write-Host ""
            Write-Status "API Test Results:"
            $currentPath = Get-Location
            Write-Host "  📊 Test Report: $currentPath\api\build\reports\tests\test\index.html"
            Write-Host "  📋 Check the test report above for detailed failure information"
            return $false
        }
    }
    catch {
        Write-Error "Error running API tests: $_"
        return $false
    }
}

# Function to run mobile integration tests
function Invoke-MobileTests {
    Write-Status "Running Mobile Integration Tests..."
    Write-Host "====================================" -ForegroundColor White
    
    try {
        # Check for connected devices/emulators
        $devices = adb devices 2>$null | Select-String "device$"
        if (-not $devices) {
            Write-Error "No Android device or emulator found. Please connect a device or start an emulator."
            Write-Status "To start an emulator, run: emulator -avd <your_avd_name>"
            return $false
        }
        
        Write-Success "Android device/emulator detected"
        
        # Clean previous test results
        Write-Status "Cleaning previous test results..."
        & ".\gradlew.bat" clean --project-dir client
        
        # Run integration tests
        Write-Status "Running mobile integration tests..."
        & ".\gradlew.bat" connectedAndroidTest --project-dir client
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Mobile integration tests passed!"
            
            # Display test results location
            Write-Host ""
            Write-Status "Mobile Test Results:"
            $currentPath = Get-Location
            Write-Host "  📱 Test Report: $currentPath\client\app\build\reports\androidTests\connected\index.html"
            Write-Host "  📸 Screenshots: $currentPath\client\app\build\outputs\androidTest-results\connected\"
            
            return $true
        } else {
            Write-Error "Mobile integration tests failed!"
            return $false
        }
    }
    catch {
        Write-Error "Error running mobile tests: $_"
        return $false
    }
}

# Function to open test reports
function Open-TestReports {
    if ($OpenReports) {
        Write-Status "Opening test reports..."
        
        # API reports
        $apiReportPath = "api\build\reports\tests\test\index.html"
        if (Test-Path $apiReportPath) {
            Start-Process $apiReportPath
        }
        
        # Mobile reports
        $mobileReportPath = "client\app\build\reports\androidTests\connected\index.html"
        if (Test-Path $mobileReportPath) {
            Start-Process $mobileReportPath
        }
    }
}

# Function to display summary
function Show-Summary {
    param(
        [bool]$ApiTestsPassed,
        [bool]$MobileTestsPassed,
        [bool]$RunApiTests,
        [bool]$RunMobileTests
    )
    
    Write-Host ""
    Write-Host "🎯 Integration Test Summary" -ForegroundColor Cyan
    Write-Host "==========================" -ForegroundColor Cyan
    
    if ($RunApiTests) {
        if ($ApiTestsPassed) {
            Write-Success "✅ API Integration Tests: PASSED"
        } else {
            Write-Error "❌ API Integration Tests: FAILED"
        }
    }
    
    if ($RunMobileTests) {
        if ($MobileTestsPassed) {
            Write-Success "✅ Mobile Integration Tests: PASSED"
        } else {
            Write-Error "❌ Mobile Integration Tests: FAILED"
        }
    } else {
        Write-Warning "⏭️  Mobile Integration Tests: SKIPPED"
    }
    
    Write-Host ""
    Write-Status "📚 Documentation: INTEGRATION_TESTING_GUIDE.md"
    Write-Host ""
}

# Main execution
function Main {
    $startTime = Get-Date
    
    # Determine what to run based on parameters
    $runApiTests = -not $MobileOnly
    $runMobileTests = -not $ApiOnly
    
    # Check prerequisites and get ADB availability
    $adbAvailable = Test-Prerequisites
    
    # Skip mobile tests if ADB is not available and user didn't specifically request mobile-only
    if ($runMobileTests -and -not $adbAvailable -and -not $MobileOnly) {
        Write-Warning "Skipping mobile tests due to missing Android SDK. Use -MobileOnly to force mobile tests."
        $runMobileTests = $false
    }
    
    # Initialize test result flags
    $apiTestsPassed = $false
    $mobileTestsPassed = $false
    
    # Run API tests
    if ($runApiTests) {
        $apiTestsPassed = Invoke-ApiTests
    }
    
    # Run mobile tests
    if ($runMobileTests) {
        $mobileTestsPassed = Invoke-MobileTests
    }
    
    # Open reports if requested
    Open-TestReports
    
    # Display summary
    Show-Summary -ApiTestsPassed ([bool]$apiTestsPassed) -MobileTestsPassed ([bool]$mobileTestsPassed) -RunApiTests ([bool]$runApiTests) -RunMobileTests ([bool]$runMobileTests)
    
    # Calculate execution time
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    Write-Status "Total execution time: $([math]::Round($duration, 1)) seconds"
    
    # Exit with appropriate code
    if (($runApiTests -and -not $apiTestsPassed) -or ($runMobileTests -and -not $mobileTestsPassed)) {
        Write-Error "Some tests failed!"
        exit 1
    }
    
    Write-Success "🎉 All requested integration tests completed successfully!"
}

# Run main function
try {
    Main
}
catch {
    Write-Error "Script execution failed: $_"
    exit 1
} 