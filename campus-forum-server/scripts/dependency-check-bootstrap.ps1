param(
  [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
  [string]$NvdApiKey = $env:NVD_API_KEY,
  [string]$RetireJsUrl = $env:DC_RETIREJS_URL,
  [string]$KevUrl = $env:DC_KEV_URL,
  [int]$RetryCount = 3,
  [switch]$PurgeCache
)

$ErrorActionPreference = "Stop"

function Invoke-Maven {
  param([string[]]$MavenArgs)

  Write-Host ""
  Write-Host "==> Running: .\\mvnw.cmd $($MavenArgs -join ' ')"
  & .\mvnw.cmd @MavenArgs
  return $LASTEXITCODE
}

Set-Location $ProjectRoot

$lockFile = Join-Path $env:USERPROFILE ".m2\repository\org\owasp\dependency-check-data\11.0\odc.update.lock"
if (Test-Path $lockFile) {
  Write-Host "Found stale lock file, removing: $lockFile"
  Remove-Item -Path $lockFile -Force -ErrorAction SilentlyContinue
}

if ($PurgeCache.IsPresent) {
  $purgeCode = Invoke-Maven -MavenArgs @("org.owasp:dependency-check-maven:purge")
  if ($purgeCode -ne 0) {
    throw "dependency-check purge failed with exit code $purgeCode"
  }
}

$baseArgs = @(
  "org.owasp:dependency-check-maven:check",
  "-Dformats=JSON,HTML",
  "-DskipTests",
  "-DfailOnError=false"
)

if ($NvdApiKey) {
  $baseArgs += "-DnvdApiKey=$NvdApiKey"
}
if ($RetireJsUrl) {
  $baseArgs += "-DretireJsUrl=$RetireJsUrl"
}
if ($KevUrl) {
  $baseArgs += "-DknownExploitedUrl=$KevUrl"
}

$attempt = 1
$success = $false
while ($attempt -le $RetryCount) {
  Write-Host ""
  Write-Host "[Attempt $attempt/$RetryCount] Online warm-up scan"

  $exitCode = Invoke-Maven -MavenArgs $baseArgs
  if ($exitCode -eq 0) {
    $success = $true
    break
  }

  Write-Host "Attempt $attempt failed with exit code $exitCode"
  $attempt++
}

if (-not $success) {
  Write-Host ""
  Write-Host "Online update failed after retries. Trying fallback without RetireJS analyzer..."
  $fallbackArgs = $baseArgs + @("-DretireJsAnalyzerEnabled=false")
  $fallbackCode = Invoke-Maven -MavenArgs $fallbackArgs
  if ($fallbackCode -ne 0) {
    throw "Fallback scan failed with exit code $fallbackCode"
  }
}

Write-Host ""
Write-Host "Done. Reports (if generated):"
Write-Host "- target/dependency-check-report.json"
Write-Host "- target/dependency-check-report.html"
