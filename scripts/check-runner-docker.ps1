# GitLab Runner Docker Access Diagnostic Script
# Run this to check Docker configuration that would be needed in CI

Write-Host "=== GitLab Runner Docker Diagnostic ===" -ForegroundColor Cyan
Write-Host ""

# Check Docker installation
Write-Host "1. Checking Docker installation..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "   ✓ Docker installed: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Docker not found" -ForegroundColor Red
    exit 1
}

# Check Docker Compose
Write-Host ""
Write-Host "2. Checking Docker Compose..." -ForegroundColor Yellow
try {
    $composeVersion = docker compose version
    Write-Host "   ✓ Docker Compose available: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Docker Compose not found" -ForegroundColor Red
}

# Check Docker daemon connection
Write-Host ""
Write-Host "3. Checking Docker daemon connection..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "   ✓ Can connect to Docker daemon" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Cannot connect to Docker daemon" -ForegroundColor Red
    Write-Host "   This is the issue you're seeing in CI!" -ForegroundColor Red
}

# Check docker-compose.e2e.yml file
Write-Host ""
Write-Host "4. Checking docker-compose.e2e.yml file..." -ForegroundColor Yellow
if (Test-Path "docker-compose.e2e.yml") {
    Write-Host "   ✓ File exists" -ForegroundColor Green
    
    # Validate compose file
    try {
        docker compose -f docker-compose.e2e.yml config --quiet 2>&1 | Out-Null
        Write-Host "   ✓ Compose file is valid" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Compose file has errors" -ForegroundColor Red
    }
} else {
    Write-Host "   ✗ File not found!" -ForegroundColor Red
    Write-Host "   This was the original issue - now fixed in CI" -ForegroundColor Yellow
}

# Check for Docker socket (Linux/WSL only)
Write-Host ""
Write-Host "5. Checking Docker socket access..." -ForegroundColor Yellow
if ($IsLinux -or (Get-Command wsl -ErrorAction SilentlyContinue)) {
    try {
        $socketPath = "/var/run/docker.sock"
        if (Test-Path $socketPath) {
            Write-Host "   ✓ Docker socket found at $socketPath" -ForegroundColor Green
        } else {
            Write-Host "   ⚠ Docker socket not at standard location" -ForegroundColor Yellow
            Write-Host "   On Windows with Docker Desktop, socket is via named pipe" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ⚠ Cannot check socket (Windows environment)" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⚠ Windows environment - socket check skipped" -ForegroundColor Yellow
    Write-Host "   Docker Desktop uses named pipes on Windows" -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "For GitLab CI to work without DIND, the runner needs:" -ForegroundColor White
Write-Host "  1. Docker executor configured" -ForegroundColor White
Write-Host "  2. privileged = true in runner config" -ForegroundColor White
Write-Host "  3. volumes = ['/var/run/docker.sock:/var/run/docker.sock']" -ForegroundColor White
Write-Host ""
Write-Host "Check runner config at: /etc/gitlab-runner/config.toml (on runner server)" -ForegroundColor Yellow
Write-Host "Or via GitLab UI: Settings → CI/CD → Runners" -ForegroundColor Yellow
