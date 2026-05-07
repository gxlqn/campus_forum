param(
  [string]$ApiBaseUrl = "http://localhost:8081/api",
  [int]$MaxWaitSeconds = 300,
  [int]$PollIntervalSeconds = 5
)

$ErrorActionPreference = "Continue"

Write-Host "Waiting for backend at $ApiBaseUrl ..."

$elapsed = 0
while ($elapsed -lt $MaxWaitSeconds) {
  try {
    $null = Invoke-RestMethod -Uri $ApiBaseUrl -TimeoutSec 3 -ErrorAction Stop
    Write-Host "Backend is UP!"
    break
  } catch {
    Write-Host "Waiting ... ($elapsed / $MaxWaitSeconds s)"
    Start-Sleep -Seconds $PollIntervalSeconds
    $elapsed += $PollIntervalSeconds
  }
}

if ($elapsed -ge $MaxWaitSeconds) {
  Write-Host "ERROR: Backend did not start within $MaxWaitSeconds seconds"
  exit 1
}

Write-Host ""
Write-Host "Getting admin token via login ..."

$loginBody = @{
  username = "admin"
  password = "123456"
} | ConvertTo-Json

try {
  $loginResp = Invoke-RestMethod -Method POST -Uri "$ApiBaseUrl/admin/login" -ContentType "application/json" -Body $loginBody -ErrorAction Stop
  $token = $loginResp.data.token
  if (-not $token) {
    Write-Host "ERROR: Could not extract token from login response"
    Write-Host ($loginResp | ConvertTo-Json -Depth 5)
    exit 1
  }
  Write-Host "Login successful, got token"
} catch {
  Write-Host "ERROR: Admin login failed: $_"
  Write-Host "Will try reindex without token..."
  $token = ""
}

Write-Host ""
Write-Host "Triggering reindex ..."

$headers = @{}
if ($token) {
  $headers["Authorization"] = "Bearer $token"
}

try {
  $reindexResp = Invoke-RestMethod -Method POST -Uri "$ApiBaseUrl/admin/search/reindex" -Headers $headers -ErrorAction Stop
  Write-Host "Reindex result:"
  $reindexResp | ConvertTo-Json -Depth 8
} catch {
  Write-Host "ERROR: Reindex failed: $_"
  exit 1
}

Write-Host ""
Write-Host "Done!"
