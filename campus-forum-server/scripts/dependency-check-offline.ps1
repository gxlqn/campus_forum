param(
  [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
  [switch]$SkipRetireJs = $true
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectRoot

$args = @(
  "org.owasp:dependency-check-maven:check",
  "-Dformats=JSON,HTML",
  "-DskipTests",
  "-DautoUpdate=false",
  "-DfailOnError=false"
)

if ($SkipRetireJs.IsPresent) {
  $args += "-DretireJsAnalyzerEnabled=false"
}

Write-Host "Running offline dependency-check..."
Write-Host ".\\mvnw.cmd $($args -join ' ')"

& .\mvnw.cmd @args
if ($LASTEXITCODE -ne 0) {
  throw "Offline dependency-check failed with exit code $LASTEXITCODE"
}

Write-Host ""
Write-Host "Offline scan completed. Reports:"
Write-Host "- target/dependency-check-report.json"
Write-Host "- target/dependency-check-report.html"
