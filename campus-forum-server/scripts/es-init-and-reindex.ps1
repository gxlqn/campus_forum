param(
  [string]$EsBaseUrl = "http://localhost:9200",
  [string]$ApiBaseUrl = "http://localhost:8080/api",
  [string]$AdminToken = "",
  [string]$PostIndex = "forum_post",
  [string]$ProductIndex = "service_product",
  [string]$ActivityIndex = "service_activity",
  [string]$HelpIndex = "service_help_request"
)

$ErrorActionPreference = "Stop"

function Invoke-Es {
  param(
    [string]$Method,
    [string]$Path,
    [string]$BodyFile
  )

  $url = "$EsBaseUrl/$Path"
  if ($BodyFile) {
    Invoke-RestMethod -Method $Method -Uri $url -ContentType "application/json" -InFile $BodyFile
  } else {
    Invoke-RestMethod -Method $Method -Uri $url
  }
}

Write-Host "Checking Elasticsearch..."
Invoke-Es -Method "GET" -Path "" | Out-Null

$mappingDir = Join-Path $PSScriptRoot "..\src\main\resources\elasticsearch\mappings"

$indices = @(
  @{ Name = $PostIndex; File = "forum_post.json" },
  @{ Name = $ProductIndex; File = "service_product.json" },
  @{ Name = $ActivityIndex; File = "service_activity.json" },
  @{ Name = $HelpIndex; File = "service_help_request.json" }
)

foreach ($idx in $indices) {
  $exists = Invoke-RestMethod -Method GET -Uri "$EsBaseUrl/$($idx.Name)" -SkipHttpErrorCheck
  if ($exists.StatusCode -eq 200) {
    Write-Host "Deleting index $($idx.Name)"
    Invoke-Es -Method "DELETE" -Path $idx.Name | Out-Null
  }

  $mappingFile = Join-Path $mappingDir $idx.File
  Write-Host "Creating index $($idx.Name) with mapping $($idx.File)"
  Invoke-Es -Method "PUT" -Path $idx.Name -BodyFile $mappingFile | Out-Null
}

if ([string]::IsNullOrWhiteSpace($AdminToken)) {
  Write-Host "Skip backend reindex call: AdminToken not provided."
  Write-Host "Use POST $ApiBaseUrl/admin/search/reindex manually after admin login."
  exit 0
}

$headers = @{ Authorization = "Bearer $AdminToken" }
Write-Host "Trigger backend reindex..."
$response = Invoke-RestMethod -Method POST -Uri "$ApiBaseUrl/admin/search/reindex" -Headers $headers
$response | ConvertTo-Json -Depth 8
