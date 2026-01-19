# PowerShell script to automatically run e2e tests
# This script starts services, runs tests, and optionally cleans up

param(
    [switch]$Cleanup = $true,
    [switch]$Headless = $true
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "E2E Test Automation Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if services are already running
$servicesRunning = docker-compose -f docker-compose.e2e.yml ps -q
if ($servicesRunning) {
    Write-Host "Services are already running. Using existing services." -ForegroundColor Yellow
} else {
    Write-Host "Starting e2e test services..." -ForegroundColor Green
    docker-compose -f docker-compose.e2e.yml up -d --build
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to start services!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
    
    # Wait for postgres
    $maxAttempts = 30
    $attempt = 0
    do {
        Start-Sleep -Seconds 2
        $pgReady = docker exec e2e-postgres pg_isready -U test -d testdb 2>$null
        $attempt++
        if ($attempt -ge $maxAttempts) {
            Write-Host "PostgreSQL failed to start!" -ForegroundColor Red
            exit 1
        }
    } while ($LASTEXITCODE -ne 0)
    Write-Host "PostgreSQL is ready" -ForegroundColor Green
    
    # Wait for backend
    $attempt = 0
    do {
        Start-Sleep -Seconds 2
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8081/api/users/login" -Method POST -Body '{"email":"test","password":"test"}' -ContentType "application/json" -ErrorAction SilentlyContinue
        } catch {
            # 401 is expected, means backend is responding
            if ($_.Exception.Response.StatusCode -eq 401) {
                $response = $null
            }
        }
        $attempt++
        if ($attempt -ge 60) {
            Write-Host "Backend failed to start!" -ForegroundColor Red
            docker-compose -f docker-compose.e2e.yml logs backend-e2e
            exit 1
        }
    } while ($null -eq $response -and $_.Exception.Response.StatusCode -ne 401)
    Write-Host "Backend is ready" -ForegroundColor Green
    
    # Wait for frontend
    $attempt = 0
    do {
        Start-Sleep -Seconds 2
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:5174" -ErrorAction SilentlyContinue
        } catch {
            # Any response means frontend is up
        }
        $attempt++
        if ($attempt -ge 30) {
            Write-Host "Frontend failed to start!" -ForegroundColor Red
            exit 1
        }
    } while ($null -eq $response)
    Write-Host "Frontend is ready" -ForegroundColor Green
    
    # Setup test data
    Write-Host "Setting up test data..." -ForegroundColor Yellow
    try {
        $body = @{
            username = "testuser"
            email = "testuser@example.com"
            password = "testpassword123"
            name = "Test User"
        } | ConvertTo-Json
        
        Invoke-RestMethod -Uri "http://localhost:8081/api/users/register" -Method POST -Body $body -ContentType "application/json" -ErrorAction SilentlyContinue
        Write-Host "Test user created (or already exists)" -ForegroundColor Green
    } catch {
        Write-Host "Test user may already exist (this is OK)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Running Cypress e2e tests..." -ForegroundColor Cyan
Write-Host ""

# Run tests
$env:CYPRESS_BASE_URL = "http://localhost:5174"
$env:CYPRESS_API_URL = "http://localhost:8081"

Push-Location frontend

if ($Headless) {
    npm run test:e2e
} else {
    npm run test:e2e:open
}

$testExitCode = $LASTEXITCODE

Pop-Location

Write-Host ""
if ($testExitCode -eq 0) {
    Write-Host "All tests passed!" -ForegroundColor Green
} else {
    Write-Host "Some tests failed. Check screenshots in frontend/cypress/screenshots/" -ForegroundColor Yellow
}

# Cleanup
if ($Cleanup) {
    Write-Host ""
    Write-Host "Cleaning up services..." -ForegroundColor Yellow
    docker-compose -f docker-compose.e2e.yml down -v
    Write-Host "Cleanup complete" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Services are still running. To stop them, run:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose.e2e.yml down -v" -ForegroundColor Cyan
}

exit $testExitCode
