param(
  [ValidateSet("headless","headed","ui")]
  [string]$Mode = "headless",
  [switch]$Keep,
  [switch]$NoInstall,
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$PlaywrightArgs
)

$ErrorActionPreference = "Stop"

Write-Host "Starting E2E test environment..." -ForegroundColor Cyan

# IMPORTANT: The frontend is built into a static Nginx image; VITE_API_URL must be correct at build-time.
# Locally, the browser runs on your host and must reach the backend via localhost:8081.
if (-not $env:VITE_API_URL) { $env:VITE_API_URL = "http://localhost:8081" }
if (-not $env:VITE_MAPBOX_TOKEN) { $env:VITE_MAPBOX_TOKEN = "test" }
if (-not $env:PLAYWRIGHT_BASE_URL) { $env:PLAYWRIGHT_BASE_URL = "http://localhost:5174" }

docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml up -d --build | Out-Host

Write-Host "Waiting for services to be ready..." -ForegroundColor Cyan

function Wait-HttpOk([string]$Url, [int]$TimeoutSeconds) {
  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $resp = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
      if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 400) { return }
    } catch { }
    Start-Sleep -Seconds 2
  }
  throw "Timed out waiting for $Url"
}

Wait-HttpOk "http://localhost:8081/actuator/health" 120
Wait-HttpOk "http://localhost:5174" 60

Write-Host "Services are ready!" -ForegroundColor Green
Write-Host "Backend:  http://localhost:8081"
Write-Host "Frontend: http://localhost:5174"

Write-Host "Setting up test data..." -ForegroundColor Cyan
try {
  Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/users/register" -ContentType "application/json" -Body '{"username":"testuser","email":"testuser@example.com","password":"testpassword123","name":"Test User"}' | Out-Null
} catch {
  Write-Host "Test user may already exist (or API returned error): $($_.Exception.Message)" -ForegroundColor Yellow
}

Push-Location "frontend"
try {
  Write-Host "Running Playwright tests (Mode=$Mode, BaseURL=$env:PLAYWRIGHT_BASE_URL)..." -ForegroundColor Cyan

  if (-not $NoInstall) {
    npx playwright install chromium | Out-Host
  }

  switch ($Mode) {
    "headless" { npx playwright test @PlaywrightArgs | Out-Host }
    "headed"   { npx playwright test --headed @PlaywrightArgs | Out-Host }
    "ui"       { npx playwright test --ui @PlaywrightArgs | Out-Host }
  }
} finally {
  Pop-Location
}

if (-not $Keep) {
  $reply = Read-Host "Stop test services? (y/n)"
  if ($reply -match "^[Yy]") {
    docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml down -v | Out-Host
  }
} else {
  Write-Host "Keeping services running. To stop:" -ForegroundColor Yellow
  Write-Host "docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml down -v"
}

