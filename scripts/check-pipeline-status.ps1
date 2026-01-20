# GitLab Pipeline Status Checker
# Usage: .\scripts\check-pipeline-status.ps1 [pipeline_id]
# Or set GITLAB_TOKEN environment variable for authentication

param(
    [string]$PipelineId = "",
    [string]$Branch = "development",
    [string]$ProjectId = "I548789/individual-project-s3"
)

$gitlabUrl = "https://git.fhict.nl"
$apiUrl = "$gitlabUrl/api/v4/projects/$($ProjectId -replace '/', '%2F')"

Write-Host "=== GitLab Pipeline Status Checker ===" -ForegroundColor Cyan
Write-Host ""

# Check for GitLab token
$token = $env:GITLAB_TOKEN
if (-not $token) {
    Write-Host "⚠️  GITLAB_TOKEN not set. Using unauthenticated access (limited)." -ForegroundColor Yellow
    Write-Host "   Set GITLAB_TOKEN environment variable for full access." -ForegroundColor Yellow
    Write-Host ""
}

# Get latest pipeline if not specified
if (-not $PipelineId) {
    Write-Host "Fetching latest pipeline for branch: $Branch" -ForegroundColor Yellow
    
    $headers = @{}
    if ($token) {
        $headers["PRIVATE-TOKEN"] = $token
    }
    
    try {
        $pipelines = Invoke-RestMethod -Uri "$apiUrl/pipelines?ref=$Branch&per_page=1" -Headers $headers -Method Get
        if ($pipelines.Count -eq 0) {
            Write-Host "❌ No pipelines found for branch: $Branch" -ForegroundColor Red
            exit 1
        }
        $PipelineId = $pipelines[0].id
        Write-Host "   Found pipeline ID: $PipelineId" -ForegroundColor Green
    } catch {
        Write-Host "❌ Error fetching pipelines: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "Manual check:" -ForegroundColor Yellow
        Write-Host "   Go to: $gitlabUrl/$ProjectId/-/pipelines" -ForegroundColor White
        exit 1
    }
}

Write-Host ""
Write-Host "Pipeline ID: $PipelineId" -ForegroundColor Cyan
Write-Host ""

# Get pipeline details
$headers = @{}
if ($token) {
    $headers["PRIVATE-TOKEN"] = $token
}

try {
    $pipeline = Invoke-RestMethod -Uri "$apiUrl/pipelines/$PipelineId" -Headers $headers -Method Get
    
    Write-Host "Status: " -NoNewline
    switch ($pipeline.status) {
        "success" { Write-Host "✅ SUCCESS" -ForegroundColor Green }
        "failed" { Write-Host "❌ FAILED" -ForegroundColor Red }
        "running" { Write-Host "🔄 RUNNING" -ForegroundColor Yellow }
        "pending" { Write-Host "⏳ PENDING" -ForegroundColor Yellow }
        "canceled" { Write-Host "🚫 CANCELED" -ForegroundColor Gray }
        default { Write-Host $pipeline.status -ForegroundColor White }
    }
    
    Write-Host "Branch: $($pipeline.ref)" -ForegroundColor White
    Write-Host "Commit: $($pipeline.sha.Substring(0, 8))" -ForegroundColor White
    Write-Host "URL: $gitlabUrl/$ProjectId/-/pipelines/$PipelineId" -ForegroundColor Cyan
    Write-Host ""
    
    # Get jobs
    Write-Host "=== Jobs ===" -ForegroundColor Cyan
    $jobs = Invoke-RestMethod -Uri "$apiUrl/pipelines/$PipelineId/jobs" -Headers $headers -Method Get
    
    $jobs | ForEach-Object {
        $statusIcon = switch ($_.status) {
            "success" { "✅" }
            "failed" { "❌" }
            "running" { "🔄" }
            "pending" { "⏳" }
            "canceled" { "🚫" }
            "skipped" { "⏭️ " }
            default { "❓" }
        }
        
        $statusColor = switch ($_.status) {
            "success" { "Green" }
            "failed" { "Red" }
            "running" { "Yellow" }
            "pending" { "Yellow" }
            default { "White" }
        }
        
        Write-Host "$statusIcon $($_.name)" -ForegroundColor $statusColor -NoNewline
        Write-Host " [$($_.stage)] - $($_.status)" -ForegroundColor Gray
        if ($_.web_url) {
            Write-Host "   → $($_.web_url)" -ForegroundColor DarkGray
        }
    }
    
    Write-Host ""
    
    # Summary
    $successCount = ($jobs | Where-Object { $_.status -eq "success" }).Count
    $failedCount = ($jobs | Where-Object { $_.status -eq "failed" }).Count
    $runningCount = ($jobs | Where-Object { $_.status -eq "running" }).Count
    $totalCount = $jobs.Count
    
    Write-Host "=== Summary ===" -ForegroundColor Cyan
    Write-Host "Total: $totalCount" -ForegroundColor White
    Write-Host "✅ Success: $successCount" -ForegroundColor Green
    if ($failedCount -gt 0) {
        Write-Host "❌ Failed: $failedCount" -ForegroundColor Red
    }
    if ($runningCount -gt 0) {
        Write-Host "🔄 Running: $runningCount" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Error fetching pipeline details: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Manual check:" -ForegroundColor Yellow
    Write-Host "   Go to: $gitlabUrl/$ProjectId/-/pipelines/$PipelineId" -ForegroundColor White
    exit 1
}
