param([Parameter(Mandatory=$true)][string]$Root,[Parameter(Mandatory=$true)][string]$Output)
$ErrorActionPreference = 'Stop'
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$creator = Join-Path $PSScriptRoot 'create_source_archive.py'
python $creator $rootPath $Output
if ($LASTEXITCODE -ne 0) { throw "source archive creation failed: $LASTEXITCODE" }
