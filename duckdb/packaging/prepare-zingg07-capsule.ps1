[CmdletBinding()]
param(
  [string]$SourceDirectory = (Join-Path ([System.IO.Path]::GetTempPath()) ("zingg-upstream-07-" + [guid]::NewGuid().ToString("N"))),
  [string]$Commit = "48cb157b4f35fcfa733e2e7f9699ea988e018738",
  [string]$ManifestPath = "",
  [switch]$KeepSource
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$wrapper = Join-Path $repoRoot "mvnw.cmd"
if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
  $ManifestPath = Join-Path $repoRoot "dist\zingg07-capsule-manifest.json"
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
  throw "git is required to bootstrap the Zingg v0.7 compatibility capsule"
}
if (-not (Test-Path -LiteralPath $wrapper)) {
  throw "repository Maven wrapper not found: $wrapper"
}
if ($Commit -notmatch '^[0-9a-f]{40}$') {
  throw "Commit must be a full 40-character SHA-1"
}
if (Test-Path -LiteralPath $SourceDirectory) {
  throw "source directory already exists; choose a new path: $SourceDirectory"
}

$source = [System.IO.Path]::GetFullPath($SourceDirectory)
$remote = "https://github.com/zinggai/zingg.git"

try {
  & git clone --filter=blob:none --no-checkout $remote $source
  if ($LASTEXITCODE -ne 0) { throw "git clone failed with exit code $LASTEXITCODE" }
  & git -C $source fetch --depth 1 origin $Commit
  if ($LASTEXITCODE -ne 0) { throw "git fetch failed with exit code $LASTEXITCODE" }
  & git -C $source sparse-checkout init --no-cone
  if ($LASTEXITCODE -ne 0) { throw "git sparse-checkout initialization failed with exit code $LASTEXITCODE" }
  & git -C $source sparse-checkout set --no-cone pom.xml common/pom.xml common/client/** common/core/src/main/** thirdParty/lib/**
  if ($LASTEXITCODE -ne 0) { throw "git sparse-checkout configuration failed with exit code $LASTEXITCODE" }
  & git -C $source checkout --detach $Commit
  if ($LASTEXITCODE -ne 0) { throw "git checkout failed with exit code $LASTEXITCODE" }

  $actual = (& git -C $source rev-parse HEAD).Trim()
  if ($actual -ne $Commit) { throw "checked-out commit mismatch: expected $Commit, got $actual" }

  & $wrapper -f (Join-Path $source "pom.xml") -pl common/client -am -DskipTests "-Dzingg.version=0.7.0" package
  if ($LASTEXITCODE -ne 0) { throw "Zingg common contract build failed with exit code $LASTEXITCODE" }

  $artifact = Join-Path $source "common\client\target\zingg-common-client-0.7.0.jar"
  if (-not (Test-Path -LiteralPath $artifact)) { throw "expected contract artifact was not built: $artifact" }
  $manifestParent = Split-Path -Parent ([System.IO.Path]::GetFullPath($ManifestPath))
  New-Item -ItemType Directory -Force -Path $manifestParent | Out-Null
  $capsule = Join-Path $manifestParent "zingg-common-client-0.7.0.jar"
  & python (Join-Path $repoRoot "packaging\normalize-zingg07-jar.py") $artifact $capsule $ManifestPath
  if ($LASTEXITCODE -ne 0) { throw "canonical capsule normalization or hash verification failed: $LASTEXITCODE" }
  & python (Join-Path $repoRoot "tools\verify-zframe-inventory.py") $capsule
  if ($LASTEXITCODE -ne 0) { throw "upstream ZFrame inventory verification failed: $LASTEXITCODE" }
  $canonicalManifest = Get-Content -Raw -LiteralPath $ManifestPath | ConvertFrom-Json
  $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $capsule).Hash.ToLowerInvariant()
  $result = [ordered]@{
    upstreamRepository = $remote
    upstreamCommit = $actual
    sourceArchiveSha256 = $canonicalManifest.sourceArchiveSha256
    artifact = $capsule
    artifactSha256 = $hash
    normalizedJar = $true
    normalization = $canonicalManifest.normalization
    crossPlatformBaselineMatched = $canonicalManifest.pinnedHashMatched
    byteReproducible = $false
    buildCommand = "mvnw.cmd -f <checkout>\pom.xml -pl common/client -am -DskipTests -Dzingg.version=0.7.0 package"
    localOnly = $true
    releaseReady = $false
    note = "Canonical JAR hash matched the reviewed cross-platform baseline; independent hosted-runner evidence remains pending."
  }
  $result | ConvertTo-Json | Set-Content -LiteralPath $ManifestPath -Encoding UTF8
  $result | ConvertTo-Json | Write-Output
}
finally {
  if (-not $KeepSource -and (Test-Path -LiteralPath $source)) {
    Remove-Item -LiteralPath $source -Recurse -Force -ErrorAction SilentlyContinue
  }
}
