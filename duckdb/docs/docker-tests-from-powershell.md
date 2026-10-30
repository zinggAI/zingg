# Run the project test suites from PowerShell with Docker

This guide runs Java, Python, reference, packaging, and packaged-worker checks in Linux containers launched directly from Windows PowerShell. Maven build output is created in a container-local copy of the checkout, not in the Windows working tree. Only an explicitly selected package-output directory is writable from the host.

The commands below assume the current repository state is the source under test. They do not test hosted GitHub Actions, native Windows/macOS execution, or ARM hardware; a Linux container cannot substitute for those gates.

## 1. PowerShell setup

Open PowerShell at the repository root. Set and validate the paths and the pinned contract capsule:

```powershell
$ErrorActionPreference = 'Stop'
$Repo = (Resolve-Path '.').Path
$MavenImage = 'maven:3.9.9-eclipse-temurin-21'
$PythonImage = 'python:3.12-slim'
$ContractJar = Join-Path $Repo 'dist\zingg07-capsule\zingg-common-client-0.7.0.jar'

docker version
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop/Engine is unavailable' }
docker volume create zingg-m2-cache
if ($LASTEXITCODE -ne 0) { throw 'Could not create Maven cache volume' }

if (-not (Test-Path -LiteralPath $ContractJar -PathType Leaf)) {
  throw "Pinned Zingg 0.7 contract capsule is missing: $ContractJar"
}
$ContractHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $ContractJar).Hash.ToLowerInvariant()
if ($ContractHash -ne 'a289903fae2ed9ff3a0a3360fed3c80232d70fce848df27e2c6a6ee8406c4b5d') {
  throw "Unexpected Zingg contract capsule SHA-256: $ContractHash"
}
"Pinned contract capsule SHA-256: $ContractHash"
```

The capsule is only needed for the pinned Zingg contract and Spark differential suites. If it is missing, bootstrap it once using the repository's PowerShell script (this prepares a pinned test dependency; it does not run the tests):

```powershell
./packaging/prepare-zingg07-capsule.ps1 -ManifestPath (Join-Path $Repo 'dist\zingg07-capsule\zingg07-capsule-manifest.json')
if ($LASTEXITCODE -ne 0) { throw 'Pinned Zingg capsule bootstrap failed' }
```

Pull the images if you prefer to separate image downloads from the test run:

```powershell
docker pull $MavenImage
if ($LASTEXITCODE -ne 0) { throw 'Could not pull Maven/JDK image' }
docker pull $PythonImage
if ($LASTEXITCODE -ne 0) { throw 'Could not pull Python image' }
```

## 2. Complete default Maven reactor

Runs all nine default Maven modules and their tests. Two Zingg 0.7 external-contract probes are opt-in and are covered separately in section 4.

```powershell
$MavenTests = @'
set -eu
mkdir /tmp/src
tar -C /host \
  --exclude=.git --exclude='*/target' --exclude='dist*' --exclude=build \
  --exclude=.pytest_cache --exclude=snapshot_failures \
  --exclude=zingg-jre-audit-win-20260929 -cf - . | tar -C /tmp/src -xf -
cd /tmp/src
mvn -B -ntp -Dsurefire.timeout=300 test
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $MavenTests
if ($LASTEXITCODE -ne 0) { throw 'Full Maven test reactor failed' }
```

Expected shape of a successful result: `BUILD SUCCESS`, zero failures/errors. Skipped opt-in probes are not passing evidence; run section 4 for those contract/reference gates.

## 3. Python client suite and syntax checks

Runs the entire Python client suite with warnings treated as errors. The source is copied into the disposable container so `py_compile` can create bytecode without writing into the checkout. Tests that require a built Java worker or compiled Java test fixtures need those inputs; section 6 runs the packaged-worker integration suite with the actual bundled runtime.

```powershell
$PythonTests = @'
set -eu
mkdir /tmp/src
tar -C /host --exclude=.git --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache --exclude=__pycache__ \
  --exclude=snapshot_failures -cf - . | tar -C /tmp/src -xf -
cd /tmp/src
python -m py_compile python/zingg_duckdb/client.py python/zingg_duckdb/worker.py
PYTHONPATH=python PYTHONWARNINGS=error python -m unittest discover -s python/tests -v
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --workdir /tmp $PythonImage sh -lc $PythonTests
if ($LASTEXITCODE -ne 0) { throw 'Python client tests or syntax checks failed' }
```

## 4. Pinned Zingg 0.7 contract and Spark 3.5.5 differential tests

These checks compile against the capsule whose hash was verified in section 1. They run in an isolated source copy, use the shared Maven cache, and mount the capsule read-only.

### 4.1 Exact contract probes

```powershell
$ContractTests = @'
set -eu
mkdir /tmp/src
tar -C /host --exclude=.git --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache --exclude=snapshot_failures -cf - . |
  tar -C /tmp/src -xf -
cd /tmp/src
apt-get update
apt-get install -y --no-install-recommends python3
python3 tools/verify-zframe-inventory.py /contract/zingg-common-client-0.7.0.jar
python3 tools/generate-zframe-adapter-matrix.py --check
mvn -B -ntp -pl compat-zingg07-runtime -am \
  -Dzingg07.contract.profile=true \
  -Dzingg07.contract.jar=/contract/zingg-common-client-0.7.0.jar \
  -Dtest=Zingg07ExactCompileChecks,Zingg07ContractProbeTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dsurefire.timeout=300 test
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount "type=bind,source=$ContractJar,target=/contract/zingg-common-client-0.7.0.jar,readonly" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $ContractTests
if ($LASTEXITCODE -ne 0) { throw 'Pinned Zingg exact-contract checks failed' }
```

The two `Zingg07ContractProbeTest` cases may be skipped unless their external fixture inputs are supplied. Preserve and report those skips; do not count them as executed contract coverage.

### 4.2 Full Spark differential class and exact Javac overload resolver

```powershell
$SparkTests = @'
set -eu
mkdir /tmp/src
tar -C /host --exclude=.git --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache --exclude=snapshot_failures -cf - . |
  tar -C /tmp/src -xf -
cd /tmp/src
mvn -B -ntp -pl reference-spark35-tests -am \
  -Dzingg07.contract.profile=true \
  -Dzingg07.contract.jar=/contract/zingg-common-client-0.7.0.jar \
  -Dtest=ZFrameCoreDifferentialTest,ZFrameOverloadResolutionTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dsurefire.timeout=300 test ||
{
  find reference-spark35-tests/target/surefire-reports \
    -maxdepth 1 -name '*.txt' -print -exec cat {} \;
  exit 1
}
'@

docker run --rm --network bridge `
  --env SPARK_LOCAL_IP=127.0.0.1 `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount "type=bind,source=$ContractJar,target=/contract/zingg-common-client-0.7.0.jar,readonly" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $SparkTests
if ($LASTEXITCODE -ne 0) { throw 'Spark differential or exact-overload tests failed' }
```

This includes all currently isolated overload differentials and the resolver; it does not prove parity for overloads still marked selected-but-unproven or provide approved Zingg phase goldens.

## 5. Packaging and repository-workflow unit tests

Runs all tests under `packaging/tests`, including CI workflow contracts, package/SBOM/source-archive checks, benchmark-report validation, and ZFrame evidence-generator tests.

```powershell
$PackagingTests = @'
set -eu
python -m unittest discover -s /workspace/packaging/tests -p 'test_*.py' -v
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/workspace,readonly" `
  --workdir /workspace $PythonImage sh -lc $PackagingTests
if ($LASTEXITCODE -ne 0) { throw 'Packaging/repository-workflow tests failed' }
```

Verify the generated ZFrame matrix separately (also run as part of the packaging tests):

```powershell
$MatrixCheck = @'
set -eu
python tools/generate-zframe-adapter-matrix.py --check
'@
docker run --rm --network none `
  --mount "type=bind,source=$Repo,target=/workspace,readonly" `
  --workdir /workspace $PythonImage sh -lc $MatrixCheck
if ($LASTEXITCODE -ne 0) { throw 'ZFrame adapter evidence matrix is stale or invalid' }
```

## 6. Build a fresh Linux package and run packaged-worker checks

This section is longer because it builds a disposable Linux bundle with its own Java 21 runtime, then tests that exact bundle. The output goes into a unique `dist/docker-package-<timestamp>` folder. `package-native.sh` replaces the bundle with a fixed name inside its output folder, so use a new folder for each run.

```powershell
$PackageOut = Join-Path $Repo ("dist\docker-package-" + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Force -Path $PackageOut | Out-Null

$BuildPackage = @'
set -eu
mkdir /tmp/src
tar -C /host --exclude=.git --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache --exclude=snapshot_failures -cf - . |
  tar -C /tmp/src -xf -
cd /tmp/src
chmod +x mvnw
apt-get update
apt-get install -y --no-install-recommends python3
CREATE_JRE=true bash packaging/package-native.sh /out
mkdir -p /out/test-classes
cp -R runtime-worker/target/test-classes/. /out/test-classes/
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount "type=bind,source=$PackageOut,target=/out" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $BuildPackage
if ($LASTEXITCODE -ne 0) { throw 'Linux package build/verification failed' }

$Bundle = Join-Path $PackageOut 'zingg-duckdb-0.1.0'
if (-not (Test-Path (Join-Path $Bundle 'runtime\java\bin\java'))) {
  throw "Bundled Java runtime was not produced: $Bundle"
}
```

`package-native.sh` itself runs Maven `clean package` with unit-test execution skipped, then verifies the bundle, source archive, SBOM, JRE legal inventory, rollback archive, and manifests. The test suites above must still be run; packaging is not a substitute for tests.

Run the full Linux packaged-worker process suite (restart, concurrency, kill/race and failure cases) against the just-built bundle using its bundled Java:

```powershell
$PackagedWorkerTests = @'
set -eu
export PYTHONPATH=/repo/python
java=/opt/zingg/runtime/java/bin/java
jar=/opt/zingg/worker/runtime-worker-0.1.0-SNAPSHOT.jar
python /repo/tools/verify-worker-label-restart.py "$jar" "$java"
python /repo/tools/verify-worker-label-concurrency.py "$jar" "$java"
python /repo/tools/verify-worker-label-kill.py "$jar" "$java"
python /repo/tools/verify-worker-failure-matrix.py "$jar" "$java"
PYTHONWARNINGS=error ZINGG_TEST_JAVA="$java" \
  ZINGG_TEST_WORKER_JAR="$jar" \
  ZINGG_TEST_CLASSES=/opt/zingg-test-classes \
  python -m unittest discover -s /repo/python/tests -p test_worker_process_cancellation.py -v
'@

docker run --rm --network none --read-only `
  --tmpfs /tmp:rw,nosuid,nodev,exec,size=1g `
  --mount "type=bind,source=$Repo,target=/repo,readonly" `
  --mount "type=bind,source=$Bundle,target=/opt/zingg,readonly" `
  --mount "type=bind,source=$PackageOut\test-classes,target=/opt/zingg-test-classes,readonly" `
  --workdir /tmp $PythonImage sh -lc $PackagedWorkerTests
if ($LASTEXITCODE -ne 0) { throw 'Packaged-worker process/reliability tests failed' }
```

Run the package's offline, read-only-root, spill-tmpfs and resource-quota smoke directly in an isolated Ubuntu container. This is the same core Linux isolation contract as `packaging/verify-docker-package.sh`, without asking a container to invoke a nested Docker daemon. The quota probe deliberately triggers DuckDB's spill-limit error (which DuckDB labels `Out of Memory Error`); the command validates that rejection and cleanup, then prints only a short summary. Treat all container output as results, not commands to paste back into PowerShell:

```powershell
$DockerPackageSmoke = @'
set -eu
echo DOCKER_PACKAGE_SMOKE_SCRIPT_V2
test -x /opt/zingg/bin/zingg-duckdb.sh
test -x /opt/zingg/runtime/java/bin/java
test ! -e /usr/bin/java
actual=$(printf 'docker-smoke\tping\t\ndocker-smoke\tshutdown\t\n' | env PATH=/usr/bin:/bin /opt/zingg/bin/zingg-duckdb.sh)
expected=$(printf 'docker-smoke\tok\tpong\ndocker-smoke\tok\tstopping')
case "$actual" in
  "$expected") ;;
  *) printf 'unexpected packaged worker response:\n%s\n' "$actual" >&2; exit 1 ;;
esac
java=/opt/zingg/runtime/java/bin/java
jar=/opt/zingg/worker/runtime-worker-0.1.0-SNAPSHOT.jar
cp="$jar:/opt/zingg-test-classes"
offline=$($java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -cp "$cp" io.zingg.duckdb.worker.OfflineNetworkProbe /tmp/offline-probe)
case "$offline" in *OFFLINE_NETWORK_AND_DUCKDB_URL_REJECTED*) ;; *) echo "$offline" >&2; exit 1;; esac
status=$(printf 'spill-status\tstatus\t\nspill-stop\tshutdown\t\n' | env PATH=/usr/bin:/bin /opt/zingg/bin/zingg-duckdb.sh --temp-directory /zingg-spill --max-temp-bytes 1048576)
case "$status" in *'temp_directory=/zingg-spill;'*) ;; *) echo "$status" >&2; exit 1;; esac
quota=$($java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -cp "$cp" io.zingg.duckdb.worker.SpillQuotaProbe /zingg-spill)
case "$quota" in
  *SPILL_TMPFS_QUOTA_REJECTED_AND_CLEANED*MODEL_ARTIFACT_BUDGET_REJECTED_AND_CLEANED*CONCURRENT_SPILL_QUOTA_REJECTED_AND_CLEANED*)
    printf '%s\n' "${quota%% diagnostic=*}"
    ;;
  *) echo "$quota" >&2; exit 1 ;;
esac
echo DOCKER_PACKAGE_SMOKE_SUCCESS
'@

docker run --rm --network none --read-only `
  --tmpfs /tmp:rw,nosuid,nodev,exec,size=256m `
  --tmpfs /zingg-spill:rw,nosuid,nodev,size=16m `
  --mount "type=bind,source=$Bundle,target=/opt/zingg,readonly" `
  --mount "type=bind,source=$PackageOut\test-classes,target=/opt/zingg-test-classes,readonly" `
  ubuntu:24.04 /bin/sh -eu -c $DockerPackageSmoke
if ($LASTEXITCODE -ne 0) { throw 'Offline/quota Docker package smoke failed' }
```

The bundle/worker smoke verifies package-specific behavior in Linux Docker only. It does not validate the Windows launcher, macOS, hosted runners, or legal approval.

## 7. Optional performance and scaling diagnostics

These are measurements, not pass/fail correctness suites. Run them only after Maven package/install has built the modules. Performance reports are hardware/container-specific; do not compare numbers from unlike Docker hosts as if they were thresholds.

### 7.1 Classifier scaling benchmark

For the exact historical diagnostic matrix (10k/100k/1M rows × 2/8 features, 20 iterations, 3 measured repeats):

```powershell
$BenchOut = Join-Path $Repo ("benchmarks\docker-user-" + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Force -Path $BenchOut | Out-Null
$ClassifierBenchmark = @'
set -eu
echo '=== Install JSON verifier runtime ==='
apt-get update
apt-get install -y --no-install-recommends python3
mkdir /tmp/src
tar -C /host --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache -cf - . | tar -C /tmp/src -xf -
cd /tmp/src
chmod +x mvnw
echo '=== Build classifier benchmark and dependency classpath ==='
mvn -B -ntp -pl compat-zingg07-runtime -am -DskipTests install
mvn -B -ntp -pl compat-zingg07-runtime dependency:build-classpath \
  -Dmdep.outputFile=/tmp/compat-classpath.txt
dependency_classpath=''
IFS= read -r dependency_classpath < /tmp/compat-classpath.txt || :
test -n "$dependency_classpath"
cp="compat-zingg07-runtime/target/classes:engine-duckdb/target/classes:engine-api/target/classes:$dependency_classpath"
echo '=== Run classifier benchmark ==='
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  -cp "$cp" io.zingg.duckdb.compat.ClassifierTrainingBenchmarkMain \
  --rows 10000,100000,1000000 --widths 2,8 --iterations 20 --repetitions 3 \
  --output /reports/classifier-training.json
test -s /reports/classifier-training.json
echo '=== Verify classifier benchmark JSON report ==='
python3 packaging/verify-benchmark-report.py /reports/classifier-training.json --classifier-training
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount "type=bind,source=$BenchOut,target=/reports" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $ClassifierBenchmark
if ($LASTEXITCODE -ne 0) { throw 'Classifier benchmark or report verification failed' }
```

### 7.2 Arrow-ingress benchmark smoke

This reproduces the three CI benchmark modes at 1,000 rows (regular, Arrow-only clean timing, Arrow-only with resource sampling). To keep the Maven outputs Docker-local, build and run in the same container:

```powershell
$ArrowBenchmark = @'
set -eu
mkdir /tmp/src
tar -C /host --exclude=.git --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache -cf - . | tar -C /tmp/src -xf -
cd /tmp/src
mvn -B -ntp -pl engine-duckdb -am -DskipTests package
mvn -B -ntp -pl engine-duckdb dependency:build-classpath -Dmdep.outputFile=/tmp/engine-classpath.txt
cp="engine-duckdb/target/classes:engine-api/target/classes:$(cat /tmp/engine-classpath.txt)"
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -cp "$cp" io.zingg.duckdb.engine.BenchmarkMain --size 1000 --repetitions 3
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -cp "$cp" io.zingg.duckdb.engine.BenchmarkMain --arrow-only --size 1000 --repetitions 3
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -cp "$cp" io.zingg.duckdb.engine.BenchmarkMain --arrow-only --resource-sampling --size 1000 --repetitions 3
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $ArrowBenchmark
if ($LASTEXITCODE -ne 0) { throw 'Arrow/benchmark smoke failed' }
```

### 7.3 Graph-shape scaling matrix

Runs path, star, dense, and disconnected graphs at 100 and 500 vertices, three repeats each. Every JSON report is retained in the timestamped output folder. The 500-vertex connected cases emit 124,750 transitive pairs and stay below the configured one-million-pair limit.

```powershell
$GraphBenchmark = @'
set -eu
mkdir /tmp/src
tar -C /host --exclude=.git --exclude='*/target' --exclude='dist*' \
  --exclude=build --exclude=.pytest_cache -cf - . | tar -C /tmp/src -xf -
cd /tmp/src
mvn -B -ntp -pl engine-duckdb -am -DskipTests package
mvn -B -ntp -pl engine-duckdb dependency:build-classpath -Dmdep.outputFile=/tmp/engine-classpath.txt
cp="engine-duckdb/target/classes:engine-api/target/classes:$(cat /tmp/engine-classpath.txt)"
for shape in path star dense disconnected; do
  for size in 100 500; do
    java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
      -cp "$cp" io.zingg.duckdb.engine.BenchmarkMain \
      --graph-compare --shape "$shape" --size "$size" --repetitions 3 \
      > "/reports/graph-${shape}-${size}.json"
  done
done
'@

docker run --rm --network bridge `
  --mount "type=bind,source=$Repo,target=/host,readonly" `
  --mount "type=bind,source=$BenchOut,target=/reports" `
  --mount 'type=volume,source=zingg-m2-cache,target=/root/.m2' `
  --workdir /tmp $MavenImage bash -lc $GraphBenchmark
if ($LASTEXITCODE -ne 0) { throw 'Graph-shape benchmark matrix failed' }
```

For separate best-effort RSS/spill sampling, add `--resource-sampling` to the `BenchmarkMain` arguments and save it as a distinct profile; sampling perturbs timing and is not a true peak-memory guarantee.

For additional giant-component profiles or 100k/1M classifier widths beyond the command above, see [`benchmarks/README.md`](../benchmarks/README.md) and raise Docker CPU/memory limits deliberately; dense graph output grows quadratically.

## 8. Final hygiene and results

Run a whitespace check from a disposable Git container. This checks the host checkout without running Git or tests on the host:

```powershell
docker run --rm --network none `
  --mount "type=bind,source=$Repo,target=/workspace,readonly" `
  --workdir /workspace alpine/git:latest -c core.whitespace=cr-at-eol diff --check
if ($LASTEXITCODE -ne 0) { throw 'Whitespace check failed' }
```

Record for each suite: exact command, Docker image/tag (prefer also image digest), test counts, skips and their reasons, exit code, contract capsule hash where applicable, and date. A skipped test is not a pass. Keep external/hosted/platform gates open until they have their own authoritative runner evidence.
