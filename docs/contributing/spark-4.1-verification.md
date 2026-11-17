# Spark 4.1 / Scala 2.13 Build Verification

Zingg can be built against three Spark lines. Pick one with `-Dspark=<version>`. The default is 3.5.

| Profile | Command | Spark | Scala | JDK | Jackson | GraphFrames | Netty (test) | lz4 (test) |
|---|---|---|---|---|---|---|---|---|
| `spark-3.5` (default) | `mvn install` or `-Dspark=3.5` | 3.5.5 | 2.12.10 | 11 | 2.15.2 | `graphframes-spark3_2.12` 0.11.0 | 4.1.125.Final | `org.lz4` 1.8.1 (relocates to `at.yawk.lz4`) |
| `spark-4.1` (new) | `-Dspark=4.1` | 4.1.4 | 2.13.17 | 17 | 2.21.2 (annotations 2.21) | `graphframes-spark4_2.13` 0.12.3 | 4.2.7.Final | `org.lz4` 1.8.0 |
| `spark-4.2` | `-Dspark=4.2` | 4.2.0 | 2.13.18 | 17 | 2.21.2 (annotations 2.21) | `graphframes-spark4_2.13` 0.12.3 | 4.2.13.Final | `at.yawk.lz4` 1.11.0 |

The Scala, Netty, lz4 and Jackson versions match the ones each Spark release ships in its own parent POM.

## What changed

- **`pom.xml`**
  - New `spark-4.1` profile.
  - The JDK 17 `--add-opens` list is now one shared property, `spark4.javaModuleOptions`, used by both 4.x profiles. The 4.2 flags are unchanged.
  - Jackson versions are now set per profile through `jackson.version` and `jackson.annotations.version`. Spark 3.5 keeps 2.15.2. Spark 4.1 and 4.2 move to 2.21.2, which is what those Spark releases run with.
  - Surefire now tells the tests which Spark and Scala versions the profile expects.
- **New test `spark/core/src/test/java/zingg/spark/core/TestSparkRuntimeVersion.java`.** It fails if the tests are running on a different Spark or Scala binary version than the selected profile. If the expected versions aren't passed in (for example, when run from an IDE), it skips instead.
- **`.github/workflows/build.yml`.** CI now builds a matrix: 3.5 on JDK 11, 4.1 on JDK 17 and 4.2 on JDK 17.
- **No source changes were needed for Scala 2.13.** The 2.12 → 2.13 fixes came with the Spark 4.2 profile (#1414). The four `.scala` files in `spark/client` compile with 2.13.17 under the 4.1 profile.

## Results (run on 2026-11-17, macOS arm64, Maven 3.10.0)

"Baseline" is `main` at `9d1a7f49` before this change. Every run is `mvn clean install -Dmaven.test.failure.ignore=true`, so every module runs its tests even after a failure.

| Profile | JDK | Baseline: run / fail / error / skip | After: run / fail / error / skip | Delta |
|---|---|---|---|---|
| 3.5 | 11 (Temurin 11.0.33) | 450 / 0 / 0 / 2 | 452 / 0 / 0 / 2 | +2 new tests, both pass |
| 4.1 | 17 (Zulu 17.0.20) | n/a (no profile before) | 452 / 0 / 0 / 2 | same per-test results as 4.2 |
| 4.2 | 17 (Zulu 17.0.20) | 450 / 0 / 0 / 2 | 452 / 0 / 0 / 2 | +2 new tests, both pass |
| 3.5 | 17 (no `--add-opens`) | 450 / 1 / 86 / 2 | 452 / 1 / 87 / 2 | +1 error, see below; pre-existing |
| 3.5 | 17 + `--add-opens` | 450 / 4 / 55 / 2 | 452 / 4 / 55 / 2 | +2 new tests pass; pre-existing |

The 4.1 log shows `Running Spark version 4.1.4`. The 4.1 build produces `assembly/target/zingg-0.7.0-spark-4.1.4.tar.gz`. The full 4.1 dependency tree contains no `_2.12` artifacts.

### Skipped tests (all profiles, unchanged)
- `TestSparkFrame#testSelectWithColumnArray`: `@Disabled` in `TestZFrameBase`
- `TestSparkFrame#testSelectWithColumnList`: `@Disabled` in `TestZFrameBase`

### Failures

**No regressions.** In every profile and JDK combination, comparing each test before and after shows no test that went from passing to failing.

**Pre-existing: Spark 3.5 on JDK 17. Not caused by this change. CI runs 3.5 on JDK 11, where all tests pass.**
- Without `--add-opens`, SparkSession creation fails. `SparkSessionProviderBase` catches the error and logs it, so 86 tests then fail with `NullPointerException: ... sparkSession is null`. `TestSparkStopWordsRecommender#testWithNegativeCutoff` fails with an `AssertionFailedError` because it gets an NPE instead of the expected `ZinggClientException`.
  - This also affects the new `TestSparkRuntimeVersion#testSparkVersionMatchesProfile` in this setup only: `NullPointerException: Cannot invoke "SparkSession.version()" because "this.sparkSession" is null`. That accounts for the +1 error.
- With `--add-opens`, 59 tests still fail with `java.io.InvalidObjectException: ReflectiveOperationException during deserialization`. Scala 2.12.10 predates JDK 17 support; lambda deserialization needs 2.12.15 or later. The affected classes are `TestSparkFrame`, `TestSparkVerticalDisplayUtility`, `TestSparkStopWordsRecommender`, `TestSparkTrainer`, the preprocess tests, `TestGetAs`, `TestSparkDSUtil`, `TestSparkModelDocumenter`, `TestSparkBlockingTreeUtil`, `TestSparkModel`, `TestSparkExecutors*` and `TestSparkBlock`.

## Risks

| # | Risk | Status |
|---|---|---|
| R1 | Spark 4.1 and 4.2 now run their tests on Jackson 2.21.2 instead of 2.15.2. | **Changes 4.2's test setup.** All 4.2 tests still pass. The assembly jar leaves Jackson out, so clusters use their own copy. |
| R2 | Spark 4.1 behaviour could differ from 3.5 and 4.2. | Not seen. 4.1 matches 4.2 test for test. |
| R3 | 4.1 uses `org.lz4:1.8.0`; 4.2 uses `at.yawk.lz4:1.11.0`. | Confirmed with `dependency:tree`. |
| R4 | Netty is pinned at test scope (4.2.7 for 4.1). | Confirmed with `dependency:tree`. Tests pass. |
| R5 | GraphFrames 0.12.3 is built against Spark 4.1.2; this profile uses 4.1.4. | Graph and clustering tests pass. A patch-level difference only. |
| R6 | Spark 3.5 on JDK 17 fails locally. | **Pre-existing** (see Failures). 3.5 is only supported on JDK 11. |
| R7 | Homebrew Maven defaults to JDK 27. | Set `JAVA_HOME` explicitly for every run. |
| R8 | CI now runs three builds per push. | Takes about 3× the runner time. `fail-fast: false`, and each job is named by profile. **NOT VERIFIED on GitHub Actions** until pushed. |
| R9 | Only local-mode unit tests were run. | **NOT VERIFIED on a real Spark 4.1 cluster or Databricks.** See the manual smoke test below. |
| R10 | `TestSparkRuntimeVersion` skips when the expected versions aren't passed in. | It ran (not skipped) in every Maven run above. |

## How to verify manually

Use JDK 17 for 4.x and JDK 11 for 3.5.

```bash
# 1. Build and test each profile
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean install -Dspark=4.1          # expect BUILD SUCCESS, Tests run: 452, Failures: 0, Errors: 0, Skipped: 2
mvn clean install -Dspark=4.2          # same totals
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
mvn clean install                      # default 3.5; same totals

# 2. Confirm the runtime-version guard ran (not skipped) in each profile
grep -h testcase spark/core/target/surefire-reports/TEST-zingg.spark.core.TestSparkRuntimeVersion.xml

# 3. Confirm the Spark version actually used and the artifact name
mvn clean install -Dspark=4.1 2>&1 | grep -m1 "Running Spark version"   # Running Spark version 4.1.4
ls assembly/target/zingg-0.7.0-spark-4.1.4.tar.gz

# 4. Check resolved versions (run from the repo root; -am is required)
mvn -Dspark=4.1 -pl spark/core -am dependency:tree \
  "-Dincludes=org.apache.spark:spark-core_*,org.scala-lang:scala-library,io.graphframes,org.lz4,at.yawk.lz4,io.netty:netty-handler,com.fasterxml.jackson.core,com.fasterxml.jackson.module"
# expect spark-core_2.13:4.1.4, scala-library:2.13.17, graphframes-spark4_2.13:0.12.3,
#        org.lz4:lz4-java:1.8.0, netty-handler:4.2.7.Final, jackson 2.21.2 / annotations 2.21
mvn -Dspark=4.1 dependency:tree | grep -c "_2\.12"                       # expect 0
```

### Cluster smoke test (not yet done)

On a Spark 4.1.x cluster (Scala 2.13, JDK 17), unpack `zingg-0.7.0-spark-4.1.4.tar.gz`. Point `SPARK_HOME` at the cluster's Spark and run the Febrl example:

```bash
./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json
./scripts/zingg.sh --phase label            --conf examples/febrl/config.json
./scripts/zingg.sh --phase train            --conf examples/febrl/config.json
./scripts/zingg.sh --phase match            --conf examples/febrl/config.json
```

Look for:
- `NoSuchMethodError` or `ClassNotFoundException` mentioning `scala.` or `com.fasterxml.jackson`. These would point to Scala or Jackson classpath problems.
- Jackson's `Scala module ... requires Jackson Databind version` message.
