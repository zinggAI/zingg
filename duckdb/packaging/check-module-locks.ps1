param([string]$Root = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = "Stop"
$missing = @(
  Get-ChildItem -LiteralPath $Root -Directory |
    Where-Object { (Test-Path (Join-Path $_.FullName "pom.xml")) -and
                   (-not (Test-Path (Join-Path $_.FullName "dependency-lock.json"))) }
)
if ($missing.Count -gt 0) {
  throw "Missing dependency-lock.json: $($missing.Name -join ', ')"
}
Get-ChildItem -LiteralPath $Root -Directory |
  Where-Object { Test-Path (Join-Path $_.FullName "dependency-lock.json") } |
  ForEach-Object { Get-Content -LiteralPath (Join-Path $_.FullName "dependency-lock.json") -Raw | ConvertFrom-Json | Out-Null }
Write-Output "MODULE_LOCK_CHECK_SUCCESS"
