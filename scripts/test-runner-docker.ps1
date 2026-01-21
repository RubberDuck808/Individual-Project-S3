# PowerShell script to test GitLab runner Docker socket access
# This simulates what the CI pipeline does

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "GitLab Runner Docker Socket Test" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if Docker is installed
Write-Host "1. Checking Docker installation..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "   ✓ Docker is installed: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Docker is NOT installed or not in PATH" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Check Docker daemon access
Write-Host "2. Testing Docker daemon connection..." -ForegroundColor Yellow
try {
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Docker daemon is accessible" -ForegroundColor Green
        $dockerInfo | Select-Object -First 5 | ForEach-Object { Write-Host "   $_" }
    } else {
        Write-Host "   ✗ Cannot connect to Docker daemon" -ForegroundColor Red
        Write-Host "   Error: $dockerInfo" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   ✗ Docker daemon connection failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 3: Check Docker Compose
Write-Host "3. Testing Docker Compose..." -ForegroundColor Yellow
try {
    $composeVersion = docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Docker Compose available: $composeVersion" -ForegroundColor Green
    } else {
        throw "Docker Compose not available"
    }
} catch {
    Write-Host "   ⚠ Docker Compose not available (would be installed in CI)" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Test basic Docker operations
Write-Host "4. Testing basic Docker operations..." -ForegroundColor Yellow
try {
    $testOutput = docker run --rm docker:24 echo "Container execution works" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Container execution works" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Container execution failed: $testOutput" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ Container execution failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: Test docker-compose config validation
Write-Host "5. Testing docker-compose with e2e config..." -ForegroundColor Yellow
$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$composeFile = Join-Path $projectRoot "docker-compose.e2e.yml"
if (Test-Path $composeFile) {
    try {
        Push-Location $projectRoot
        $configOutput = docker compose -f docker-compose.e2e.yml config 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✓ docker-compose.e2e.yml is valid" -ForegroundColor Green
        } else {
            Write-Host "   ✗ docker-compose.e2e.yml has errors" -ForegroundColor Red
            Write-Host "   $configOutput" -ForegroundColor Red
        }
    } catch {
        Write-Host "   ⚠ Could not validate docker-compose.e2e.yml: $_" -ForegroundColor Yellow
    } finally {
        Pop-Location
    }
} else {
    Write-Host "   ⚠ docker-compose.e2e.yml not found (expected if not in project root)" -ForegroundColor Yellow
}
Write-Host ""

# Test 6: Check environment variables (CI simulation)
Write-Host "6. Simulating CI environment..." -ForegroundColor Yellow
Write-Host "   Note: Unix socket paths don't apply on Windows" -ForegroundColor Yellow
Write-Host "   On Linux runner, DOCKER_HOST would be: unix:///var/run/docker.sock" -ForegroundColor Yellow
Write-Host ""

# Test 7: Test network creation
Write-Host "7. Testing Docker network operations..." -ForegroundColor Yellow
$networkName = "test-runner-network-$(Get-Date -Format 'yyyyMMddHHmmss')"
try {
    docker network create $networkName 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Can create Docker networks" -ForegroundColor Green
        docker network rm $networkName 2>&1 | Out-Null
        Write-Host "   ✓ Can remove Docker networks" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Cannot create Docker networks" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ Network operations failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 8: Check user context
Write-Host "8. Checking user context..." -ForegroundColor Yellow
Write-Host "   Current user: $env:USERNAME"
Write-Host "   User domain: $env:USERDOMAIN"
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "If all tests passed, your runner should work with the CI pipeline." -ForegroundColor Green
Write-Host "If any test failed, check the error messages above." -ForegroundColor Yellow
Write-Host ""
