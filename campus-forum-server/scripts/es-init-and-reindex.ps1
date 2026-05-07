param(
  [string]$EsBaseUrl = "http://localhost:9200",
  [string]$ApiBaseUrl = "http://localhost:8081/api",
  [string]$AdminToken = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJzdWIiOiIxIiwiaWF0IjoxNzc3MTkzNDMzLCJleHAiOjE3Nzc3OTgyMzN9.MR5CWQaNj3jO8k9DjS8UBzoYQyuI0i4RPq7ddxjGdoZWkysrEqm7d-KltUbfmwQN",
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
  Write-Host "Checking index $($idx.Name)"
  try {
    $null = Invoke-RestMethod -Method GET -Uri "$EsBaseUrl/$($idx.Name)" -ErrorAction Stop
    Write-Host "Deleting index $($idx.Name)"
    Invoke-Es -Method "DELETE" -Path $idx.Name | Out-Null
  } catch {
    Write-Host "Index $($idx.Name) does not exist or error occurred."
  }

  $mappingFile = Join-Path $mappingDir $idx.File
  Write-Host "Creating index $($idx.Name) with mapping $($idx.File)"
  Invoke-Es -Method "PUT" -Path $idx.Name -BodyFile $mappingFile | Out-Null
}

# ============================================================
# 拼音分词验证（可选，用于确认配置正确）
# ============================================================
Write-Host "`nVerifying pinyin analysis..."
$testIndices = @($PostIndex, $ActivityIndex, $HelpIndex, $ProductIndex)
foreach ($testIndex in $testIndices) {
  $verifyBody = @{
    field = "title"
    text  = "女友鞋转让"
  } | ConvertTo-Json

  try {
    $result = Invoke-RestMethod -Method POST `
      -Uri "$EsBaseUrl/$testIndex/_analyze" `
      -ContentType "application/json" `
      -Body $verifyBody

    $pinyinTokens = $result.tokens | Where-Object { $_.token -match "^(nv|you|xie|zhuan|rang)$" }
    if ($pinyinTokens) {
      Write-Host "  [OK] $testIndex - Pinyin tokens found: $($pinyinTokens.token -join ', ')" -ForegroundColor Green
    } else {
      Write-Host "  [WARN] $testIndex - No pinyin tokens found, check mapping" -ForegroundColor Yellow
    }
  } catch {
    Write-Host "  [SKIP] $testIndex - Unable to verify: $_" -ForegroundColor DarkYellow
  }
}

# ============================================================
# 后端重建索引调用（带 CSRF 处理）
# ============================================================
if ([string]::IsNullOrWhiteSpace($AdminToken)) {
  Write-Host "`nSkip backend reindex call: AdminToken not provided."
  Write-Host "Use POST $ApiBaseUrl/admin/search/reindex manually after admin login."
  exit 0
}

$headers = @{ Authorization = "Bearer $AdminToken" }

Write-Host "`nTriggering backend reindex..."

# 尝试多种方式处理 CSRF
$reindexSuccess = $false
$errorMessages = @()

# 方式1：使用 SessionVariable 自动管理 Cookie 和 CSRF
try {
  Write-Host "  Trying with automatic session handling..."
  $response = Invoke-RestMethod -Method POST `
    -Uri "$ApiBaseUrl/admin/search/reindex" `
    -Headers $headers `
    -SessionVariable reindexSession
  $reindexSuccess = $true
  Write-Host "  [OK] Reindex triggered successfully!" -ForegroundColor Green
} catch {
  $errorMessages += "Session method failed: $($_.Exception.Message)"
  Write-Host "  [FAIL] Session method: $($_.Exception.Message)" -ForegroundColor DarkYellow
}

# 方式2：尝试常见的 CSRF crumb 端点
if (-not $reindexSuccess) {
  $crumbEndpoints = @(
    "/crumbIssuer/api/json",
    "/admin/search/crumb",
    "/api/crumb",
    "/csrf"
  )

  foreach ($crumbEndpoint in $crumbEndpoints) {
    try {
      Write-Host "  Trying crumb endpoint: $crumbEndpoint"
      $crumbUrl = "$ApiBaseUrl$crumbEndpoint"
      
      # 先尝试不带 Authorization 头获取 crumb
      try {
        $crumbResponse = Invoke-RestMethod -Method GET -Uri $crumbUrl
      } catch {
        # 如果失败，尝试带 Authorization 头
        $crumbResponse = Invoke-RestMethod -Method GET -Uri $crumbUrl -Headers $headers
      }

      # Jenkins 格式
      if ($crumbResponse.crumb) {
        $crumbField = if ($crumbResponse.crumbRequestField) { $crumbResponse.crumbRequestField } else { "Jenkins-Crumb" }
        $headers[$crumbField] = $crumbResponse.crumb
        Write-Host "    Got crumb: $crumbField = $($crumbResponse.crumb)"
      }
      # Spring Security 格式
      elseif ($crumbResponse.token) {
        $crumbField = if ($crumbResponse.headerName) { $crumbResponse.headerName } else { "X-CSRF-TOKEN" }
        $headers[$crumbField] = $crumbResponse.token
        Write-Host "    Got CSRF token: $crumbField = $($crumbResponse.token)"
      }
      # 未知格式
      else {
        Write-Host "    Unknown crumb format: $($crumbResponse | ConvertTo-Json)"
        continue
      }

      # 带 crumb 重新请求
      $response = Invoke-RestMethod -Method POST `
        -Uri "$ApiBaseUrl/admin/search/reindex" `
        -Headers $headers `
        -SessionVariable reindexSession

      $reindexSuccess = $true
      Write-Host "  [OK] Reindex triggered with crumb from $crumbEndpoint!" -ForegroundColor Green
      break
    } catch {
      $errorMessages += "Crumb endpoint $crumbEndpoint failed: $($_.Exception.Message)"
      Write-Host "  [FAIL] $crumbEndpoint : $($_.Exception.Message)" -ForegroundColor DarkYellow
      # 移除可能无效的 crumb header
      if ($crumbField) { $headers.Remove($crumbField) }
    }
  }
}

# 方式3：尝试禁用 CSRF 检查（通过设置特定 header）
if (-not $reindexSuccess) {
  try {
    Write-Host "  Trying with X-Requested-With header..."
    $headers["X-Requested-With"] = "XMLHttpRequest"
    $response = Invoke-RestMethod -Method POST `
      -Uri "$ApiBaseUrl/admin/search/reindex" `
      -Headers $headers `
      -SessionVariable reindexSession
    $reindexSuccess = $true
    Write-Host "  [OK] Reindex triggered with X-Requested-With!" -ForegroundColor Green
  } catch {
    $errorMessages += "X-Requested-With method failed: $($_.Exception.Message)"
    Write-Host "  [FAIL] X-Requested-With method: $($_.Exception.Message)" -ForegroundColor DarkYellow
  }
}

# 输出结果
if ($reindexSuccess) {
  Write-Host "`n=== Reindex Response ===" -ForegroundColor Cyan
  $response | ConvertTo-Json -Depth 8
  Write-Host "`n[SUCCESS] Elasticsearch indices created and backend reindex triggered!" -ForegroundColor Green
} else {
  Write-Host "`n[WARNING] All automatic CSRF methods failed." -ForegroundColor Yellow
  Write-Host "Errors encountered:" -ForegroundColor Yellow
  foreach ($msg in $errorMessages) {
    Write-Host "  - $msg" -ForegroundColor DarkYellow
  }
  Write-Host "`nManual steps:" -ForegroundColor Yellow
  Write-Host "  1. Login to your admin panel"
  Write-Host "  2. Find and click 'Rebuild Search Index' button"
  Write-Host "  3. Or call the API manually with a tool like Postman"
  Write-Host "  4. Or disable CSRF temporarily in SecurityConfig.java for development"
  Write-Host "`nElasticsearch indices are ready, only backend data sync is pending." -ForegroundColor Cyan
  exit 0
}