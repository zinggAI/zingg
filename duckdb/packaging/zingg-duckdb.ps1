[CmdletBinding()]
param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Arguments)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$java = Join-Path $root 'runtime/java/bin/java.exe'
$jar = Join-Path $root 'worker/runtime-worker-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $java -PathType Leaf)) { throw "Bundled Java 21 runtime is missing: $java" }
if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) { throw "Runtime worker is missing: $jar" }
& $java '--add-opens=java.base/java.nio=ALL-UNNAMED' '--add-opens=java.base/sun.nio.ch=ALL-UNNAMED' -jar $jar @Arguments
exit $LASTEXITCODE
