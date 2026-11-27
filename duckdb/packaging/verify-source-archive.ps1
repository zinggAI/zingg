param([Parameter(Mandatory=$true)][string]$Archive)
$ErrorActionPreference = 'Stop'
$archivePath = (Resolve-Path -LiteralPath $Archive).Path
$script = Join-Path $PSScriptRoot 'verify_source_archive.py'
python $script $archivePath
if ($LASTEXITCODE -ne 0) { throw "source archive verification failed: $LASTEXITCODE" }
