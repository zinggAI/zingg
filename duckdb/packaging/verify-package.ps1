[CmdletBinding()]
param([Parameter(Mandatory=$true)][string]$Bundle,[switch]$RequireJre,[switch]$RequireSignature)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $Bundle).Path
python (Join-Path $PSScriptRoot 'verify_bundle_inventory.py') $root
if ($LASTEXITCODE -ne 0) { throw "bundle inventory verification failed: $LASTEXITCODE" }
$structureArgs = @($root)
if ($RequireJre) { $structureArgs += '--require-jre' }
$sourceArchive = Join-Path (Split-Path -Parent $root) 'zingg-duckdb-source.zip'
if (Test-Path -LiteralPath $sourceArchive -PathType Leaf) { $structureArgs += @('--source-archive', $sourceArchive) }
python (Join-Path $PSScriptRoot 'verify_package.py') @structureArgs
if ($LASTEXITCODE -ne 0) { throw "package structure verification failed: $LASTEXITCODE" }
python (Join-Path $PSScriptRoot 'verify_spdx.py') (Join-Path $root 'sbom.spdx.json')
if ($LASTEXITCODE -ne 0) { throw "SBOM verification failed: $LASTEXITCODE" }
python (Join-Path $PSScriptRoot 'dependency_licenses.py') verify (Join-Path $root 'licenses/third-party')
if ($LASTEXITCODE -ne 0) { throw "dependency notice verification failed: $LASTEXITCODE" }
if ($RequireJre) { python (Join-Path $PSScriptRoot 'jdk_legal.py') verify (Join-Path $root 'runtime/java'); if ($LASTEXITCODE -ne 0) { throw "JDK legal verification failed: $LASTEXITCODE" } }
if ($RequireSignature) { $signature = Get-AuthenticodeSignature -FilePath (Join-Path $root 'SHA256SUMS'); if ($signature.Status -ne 'Valid') { throw "SHA256SUMS signature is not valid: $($signature.Status)" } }
Write-Output 'PACKAGE_VERIFY_SUCCESS'
