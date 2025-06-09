# Test Debug API Script
# This script tests the debug logging API endpoints

$baseUrl = "http://localhost:8080/api/debug"

Write-Host "🔍 Testing Binome Matcher Debug API" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Test 1: Check API Status
Write-Host "`n1. Testing API Status..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/ping" -Method GET -TimeoutSec 10
    Write-Host "✅ API Status: $response" -ForegroundColor Green
} catch {
    Write-Host "❌ API Status Failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Make sure the API is running on port 8080" -ForegroundColor Yellow
    exit 1
}

# Test 2: Send Sample Debug Logs
Write-Host "`n2. Sending Sample Debug Logs..." -ForegroundColor Yellow

$sampleLogs = @(
    @{
        userId = "test_user_1"
        action = "SCREEN_VIEW"
        screen = "LOGIN"
        details = "User navigated to login screen"
        deviceInfo = "PowerShell Test Script"
        appVersion = "1.0.0-test"
    },
    @{
        userId = "test_user_1"
        action = "BUTTON_CLICK"
        screen = "LOGIN"
        details = "Clicked login button"
        deviceInfo = "PowerShell Test Script"
        appVersion = "1.0.0-test"
    },
    @{
        userId = "test_user_1"
        action = "API_CALL"
        screen = "LOGIN"
        details = "POST /api/auth/login"
        deviceInfo = "PowerShell Test Script"
        appVersion = "1.0.0-test"
    },
    @{
        userId = "test_user_1"
        action = "API_SUCCESS"
        screen = "LOGIN"
        details = "Login successful"
        deviceInfo = "PowerShell Test Script"
        appVersion = "1.0.0-test"
    },
    @{
        userId = "test_user_1"
        action = "SCREEN_VIEW"
        screen = "PROFILE"
        details = "User navigated to profile screen"
        deviceInfo = "PowerShell Test Script"
        appVersion = "1.0.0-test"
    },
    @{
        userId = "test_user_1"
        action = "API_ERROR"
        screen = "PROFILE"
        details = "Failed to load profile"
        errorMessage = "Network timeout after 5 seconds"
        deviceInfo = "PowerShell Test Script"
        appVersion = "1.0.0-test"
    }
)

foreach ($log in $sampleLogs) {
    try {
        $jsonBody = $log | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$baseUrl/log" -Method POST -Body $jsonBody -ContentType "application/json" -TimeoutSec 10
        Write-Host "✅ Sent: $($log.action) - $($log.screen)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to send log: $($_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 500
}

# Test 3: Retrieve Recent Logs
Write-Host "`n3. Retrieving Recent Logs..." -ForegroundColor Yellow
try {
    $logs = Invoke-RestMethod -Uri "$baseUrl/logs/recent?hours=1" -Method GET -TimeoutSec 10
    Write-Host "✅ Retrieved $($logs.Count) recent logs" -ForegroundColor Green
    
    if ($logs.Count -gt 0) {
        Write-Host "`nSample logs:" -ForegroundColor Cyan
        $logs | Select-Object -First 3 | ForEach-Object {
            $timestamp = [DateTime]::Parse($_.timestamp).ToString("HH:mm:ss")
            Write-Host "  [$timestamp] $($_.action) - $($_.screen): $($_.details)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ Failed to retrieve logs: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Retrieve Error Logs
Write-Host "`n4. Retrieving Error Logs..." -ForegroundColor Yellow
try {
    $errorLogs = Invoke-RestMethod -Uri "$baseUrl/logs/errors" -Method GET -TimeoutSec 10
    Write-Host "✅ Retrieved $($errorLogs.Count) error logs" -ForegroundColor Green
    
    if ($errorLogs.Count -gt 0) {
        Write-Host "`nError logs:" -ForegroundColor Red
        $errorLogs | Select-Object -First 2 | ForEach-Object {
            $timestamp = [DateTime]::Parse($_.timestamp).ToString("HH:mm:ss")
            Write-Host "  [$timestamp] $($_.action) - $($_.screen): $($_.errorMessage)" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "❌ Failed to retrieve error logs: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Retrieve Logs by User
Write-Host "`n5. Retrieving Logs by User..." -ForegroundColor Yellow
try {
    $userLogs = Invoke-RestMethod -Uri "$baseUrl/logs/user/test_user_1?hours=1" -Method GET -TimeoutSec 10
    Write-Host "✅ Retrieved $($userLogs.Count) logs for test_user_1" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to retrieve user logs: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Debug API Testing Complete!" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host "You can now:" -ForegroundColor Yellow
Write-Host "- Open debug_viewer.html in your browser to view logs" -ForegroundColor White
Write-Host "- Run your Android app to see real-time debug logs" -ForegroundColor White
Write-Host "- Check Docker logs: docker logs pj_tr-api-1" -ForegroundColor White 