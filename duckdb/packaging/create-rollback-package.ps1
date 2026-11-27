[CmdletBinding()]
param([Parameter(Mandatory=$true)][string]$Bundle,[Parameter(Mandatory=$true)][string]$Output)
$ErrorActionPreference = 'Stop'
$bundlePath = (Resolve-Path -LiteralPath $Bundle).Path
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { throw 'Python 3 is required to create the rollback package' }
& $python.Source (Join-Path $PSScriptRoot 'rollback_package.py') create $bundlePath ([IO.Path]::GetFullPath($Output))
if ($LASTEXITCODE -ne 0) { throw "rollback package generation failed: $LASTEXITCODE" }
