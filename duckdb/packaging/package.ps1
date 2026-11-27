param(
  [string]$Maven = "mvn",
  [string]$Output = "dist",
  [switch]$CreateJre,
  [string]$CertificateThumbprint = ""
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try {
  $mavenCommand = $Maven
  if ($Maven -eq "mvn" -and (Test-Path "$root\mvnw.cmd")) { $mavenCommand = "$root\mvnw.cmd" }
  & $mavenCommand -DskipTests clean package
  if ($LASTEXITCODE -ne 0) { throw "Maven package failed: $LASTEXITCODE" }
  & "$PSScriptRoot\check-pure-runtime.ps1" -Root $root
  if ($LASTEXITCODE -ne 0) { throw "pure runtime dependency check failed: $LASTEXITCODE" }
  & "$PSScriptRoot\check-module-locks.ps1" -Root $root
  if ($LASTEXITCODE -ne 0) { throw "module dependency lock check failed: $LASTEXITCODE" }
  New-Item -ItemType Directory -Force -Path $Output | Out-Null
  $bundle = Join-Path $Output "zingg-duckdb-0.1.0"
  if (Test-Path $bundle) { Remove-Item -LiteralPath $bundle -Recurse -Force }
  New-Item -ItemType Directory -Force -Path "$bundle\worker", "$bundle\legacy", "$bundle\python", "$bundle\config" | Out-Null
  $workerJar = Join-Path $root "runtime-worker\target\runtime-worker-0.1.0-SNAPSHOT.jar"
  if (-not (Test-Path -LiteralPath $workerJar -PathType Leaf)) { throw "runtime worker jar was not produced: $workerJar" }
  Copy-Item -LiteralPath $workerJar -Destination "$bundle\worker\"
  $legacyBlockingJar = Join-Path $root "legacy-blocking-import-zingg07-spark35\target\legacy-blocking-import-zingg07-spark35-0.1.0-SNAPSHOT.jar"
  $legacyClassifierJar = Join-Path $root "legacy-classifier-import-zingg07-spark35\target\legacy-classifier-import-zingg07-spark35-0.1.0-SNAPSHOT.jar"
  if (-not (Test-Path -LiteralPath $legacyBlockingJar -PathType Leaf)) { throw "legacy blocking importer jar was not produced: $legacyBlockingJar" }
  if (-not (Test-Path -LiteralPath $legacyClassifierJar -PathType Leaf)) { throw "legacy classifier importer jar was not produced: $legacyClassifierJar" }
  Copy-Item -LiteralPath $legacyBlockingJar, $legacyClassifierJar -Destination "$bundle\legacy\"
  & python "packaging\dependency_licenses.py" collect "runtime-worker\target\runtime-dependencies" "$bundle\licenses\third-party"
  if ($LASTEXITCODE -ne 0) { throw "runtime dependency notice collection failed: $LASTEXITCODE" }
  Copy-Item "python\zingg_duckdb" "$bundle\python\" -Recurse
  New-Item -ItemType Directory -Force -Path "$bundle\packaging", "$bundle\bin" | Out-Null
  Copy-Item -LiteralPath "packaging\invoke-legacy-import.ps1", "packaging\sign-package.ps1", "packaging\zingg-duckdb.ps1", "packaging\zingg-duckdb.cmd", "packaging\zingg-duckdb.sh", "packaging\verify-no-system-java.sh", "packaging\verify-process-kill-linux.sh", "packaging\verify-process-kill-windows.ps1", "packaging\verify-package.ps1", "packaging\verify-package.sh", "packaging\verify_bundle_inventory.py", "packaging\verify_package.py", "packaging\verify_spdx.py", "packaging\dependency_licenses.py", "packaging\jdk_legal.py", "packaging\verify-source-archive.ps1", "packaging\verify_source_archive.py", "packaging\rollback_package.py", "packaging\create-rollback-package.ps1", "packaging\create-rollback-package.sh", "packaging\verify-rollback-package.ps1", "packaging\verify-rollback-package.sh" -Destination "$bundle\packaging\"
  Copy-Item -LiteralPath "packaging\zingg-duckdb.ps1", "packaging\zingg-duckdb.cmd", "packaging\zingg-duckdb.sh" -Destination "$bundle\bin\"
  if (-not $IsWindows) { & chmod +x "$bundle/bin/zingg-duckdb.sh" }
  Get-ChildItem -LiteralPath "$bundle\python" -Directory -Recurse -Filter "__pycache__" |
    Remove-Item -Recurse -Force
  Copy-Item "packaging\runtime-manifest.json", "packaging\compatibility-profile.json", "packaging\compatibility-capsule.json", "packaging\release-policy.json" "$bundle\config\"
  & "$PSScriptRoot\generate-provenance.ps1" -Root $root -Output "$bundle\config\source-provenance.json"
  if ($LASTEXITCODE -ne 0) { throw "provenance generation failed: $LASTEXITCODE" }
  & $mavenCommand -q '-DoutputType=json' '-DoutputFile=target/dependency-tree.json' dependency:tree
  if ($LASTEXITCODE -ne 0) { throw "Maven dependency inventory failed: $LASTEXITCODE" }
  $sourceArchive = Join-Path (Resolve-Path -LiteralPath $Output).Path "zingg-duckdb-source.zip"
  & "$PSScriptRoot\create-source-archive.ps1" -Root $root -Output $sourceArchive
  if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $sourceArchive -PathType Leaf)) { throw "source archive generation failed" }
  & "$PSScriptRoot\verify-source-archive.ps1" -Archive $sourceArchive
  if ($LASTEXITCODE -ne 0) { throw "source archive verification failed: $LASTEXITCODE" }
  Copy-Item "README.md", "LICENSE", "NOTICE.md" "$bundle\"
  & "$PSScriptRoot\generate-sbom.ps1" -LockFile "$root\dependency-lock.json" -Output "$bundle\sbom.spdx.json" -TreeRoot $root -ShadedJar "$root\runtime-worker\target\runtime-worker-0.1.0-SNAPSHOT.jar"
  if ($LASTEXITCODE -ne 0) { throw "SBOM generation failed: $LASTEXITCODE" }
  if ($CreateJre) {
    & "$PSScriptRoot\create-jre.ps1" -Output "$Output\zingg-duckdb-0.1.0\runtime\java"
    if ($LASTEXITCODE -ne 0) { throw "jlink runtime creation failed: $LASTEXITCODE" }
  }
  & python "packaging\write_bundle_manifest.py" $bundle
  if ($LASTEXITCODE -ne 0) { throw "bundle manifest generation failed: $LASTEXITCODE" }
  if (-not [string]::IsNullOrWhiteSpace($CertificateThumbprint)) {
    & "$PSScriptRoot\sign-package.ps1" -Bundle $bundle -CertificateThumbprint $CertificateThumbprint
    if ($LASTEXITCODE -ne 0) { throw "package signing failed: $LASTEXITCODE" }
  }
  if ($CreateJre) { & "$PSScriptRoot\verify-package.ps1" -Bundle $bundle -RequireJre } else { & "$PSScriptRoot\verify-package.ps1" -Bundle $bundle }
  if ($LASTEXITCODE -ne 0) { throw "package verification failed: $LASTEXITCODE" }
  & "$PSScriptRoot\create-rollback-package.ps1" -Bundle $bundle -Output (Join-Path (Resolve-Path -LiteralPath $Output).Path "zingg-duckdb-0.1.0-rollback.zip")
  if ($LASTEXITCODE -ne 0) { throw "rollback package generation failed: $LASTEXITCODE" }
  & "$PSScriptRoot\verify-rollback-package.ps1" -Archive (Join-Path (Resolve-Path -LiteralPath $Output).Path "zingg-duckdb-0.1.0-rollback.zip")
  if ($LASTEXITCODE -ne 0) { throw "rollback package verification failed: $LASTEXITCODE" }
} finally { Pop-Location }
