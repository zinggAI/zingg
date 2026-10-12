param([string]$Root = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = "Stop"
$modules = @("engine-api", "engine-duckdb", "model-format", "protocol", "compat-zingg07-runtime", "runtime-worker")
$forbidden = @("org/apache/spark", "org/graphframes", "scala/", "py4j", "jpype", "com/zingg", "org/zingg")
foreach ($module in $modules) {
  $jar = Get-ChildItem -LiteralPath (Join-Path $Root "$module\target") -Filter "*.jar" -File | Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } | Select-Object -First 1
  if ($null -eq $jar) { throw "runtime jar missing for $module" }
  $listing = & jar tf $jar.FullName
  if ($LASTEXITCODE -ne 0) { throw "cannot inspect $($jar.FullName)" }
  foreach ($needle in $forbidden) { if ($listing -contains $needle -or ($listing | Select-String -SimpleMatch $needle)) { throw "forbidden dependency reference '$needle' in $($jar.Name)" } }
}
Write-Output "PURE_RUNTIME_DEPENDENCY_CHECK_SUCCESS"
