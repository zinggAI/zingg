param(
  [string]$NativeImage = "native-image",
  [string]$Output = "dist-native",
  [string]$BinaryName = "zingg-duckdb-worker"
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root "runtime-worker\target\runtime-worker-0.1.0-SNAPSHOT.jar"
$configSource = Join-Path $root "runtime-worker\src\main\resources\META-INF\native-image\io.zingg.duckdb\runtime-worker"
if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) { throw "runtime worker jar is missing; run Maven package first" }
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { throw "Python 3 is required to prepare Native Image metadata" }
$nativeImageCommand = Get-Command $NativeImage -ErrorAction SilentlyContinue
if (-not $nativeImageCommand) {
  throw "GraalVM Native Image prerequisite is missing: '$NativeImage' was not found on PATH; install GraalVM for JDK 22+ or pass -NativeImage with an explicit executable path"
}
$javaCommand = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }
$javaVersionOutput = & $javaCommand -version 2>&1 | Out-String
if ($LASTEXITCODE -ne 0 -or $javaVersionOutput -notmatch 'version "(?<major>\d+)') {
  throw "JDK 22+ is required for Native Image; unable to determine version from '$javaCommand'"
}
$javaMajor = [int]$Matches.major
if ($javaMajor -lt 22) { throw "JDK 22+ is required for Native Image; detected JDK $javaMajor from '$javaCommand'" }
New-Item -ItemType Directory -Force -Path $Output | Out-Null
$target = Join-Path (Resolve-Path -LiteralPath $Output).Path $BinaryName
$tempRoot = Join-Path ([IO.Path]::GetTempPath()) ("zingg-native-image-config-" + [guid]::NewGuid().ToString("N"))
$config = Join-Path $tempRoot "config"
try {
  & $python.Source "$PSScriptRoot\prepare_native_image_config.py" $configSource $config
  if ($LASTEXITCODE -ne 0) { throw "Native Image metadata preparation failed: $LASTEXITCODE" }
& $NativeImage `
  --no-fallback `
  --enable-url-protocols=http,https `
  --initialize-at-run-time=org.duckdb,java.sql.SQLException,java.sql.DriverManager `
  -H:+UnlockExperimentalVMOptions `
  "-H:ConfigurationFileDirectories=$config" `
  -jar $jar $target
if ($LASTEXITCODE -ne 0) { throw "native-image failed: $LASTEXITCODE" }
if (-not (Test-Path -LiteralPath $target -PathType Leaf)) { throw "native worker was not produced: $target" }
Get-FileHash -Algorithm SHA256 -LiteralPath $target | ConvertTo-Json | Set-Content (Join-Path $Output "$BinaryName.sha256.json") -Encoding utf8
Write-Output $target
} finally {
  if (Test-Path -LiteralPath $tempRoot) { Remove-Item -LiteralPath $tempRoot -Recurse -Force }
}
