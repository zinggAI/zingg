[CmdletBinding()]
param([Parameter(Mandatory=$true)][string]$Archive)
$ErrorActionPreference = 'Stop'
$archivePath = (Resolve-Path -LiteralPath $Archive).Path
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { throw 'Python 3 is required to verify the rollback package' }
& $python.Source (Join-Path $PSScriptRoot 'rollback_package.py') verify $archivePath
if ($LASTEXITCODE -ne 0) { throw "rollback package verification failed: $LASTEXITCODE" }
