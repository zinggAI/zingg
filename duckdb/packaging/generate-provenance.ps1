param([Parameter(Mandatory=$true)][string]$Root,[Parameter(Mandatory=$true)][string]$Output)
$ErrorActionPreference = 'Stop'
$rootPath = (Resolve-Path -LiteralPath $Root).Path
python (Join-Path $PSScriptRoot 'generate_provenance.py') $rootPath $Output
if ($LASTEXITCODE -ne 0) { throw "provenance generation failed: $LASTEXITCODE" }
