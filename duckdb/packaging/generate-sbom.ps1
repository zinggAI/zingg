[CmdletBinding()]
param(
  [string]$LockFile = "dependency-lock.json",
  [string]$Output = "sbom.spdx.json",
  [string]$TreeRoot = "",
  [string]$ShadedJar = ""
)
$ErrorActionPreference = "Stop"
$lockPath = (Resolve-Path -LiteralPath $LockFile).Path
if ([string]::IsNullOrWhiteSpace($TreeRoot)) { $TreeRoot = Split-Path -Parent $lockPath }
$generator = Join-Path $PSScriptRoot "generate_sbom.py"
if ([string]::IsNullOrWhiteSpace($ShadedJar)) {
  & python $generator $lockPath $Output (Resolve-Path -LiteralPath $TreeRoot).Path
} else {
  & python $generator $lockPath $Output (Resolve-Path -LiteralPath $TreeRoot).Path (Resolve-Path -LiteralPath $ShadedJar).Path
}
if ($LASTEXITCODE -ne 0) { throw "SBOM generation failed: $LASTEXITCODE" }
