# Benchmarks

`BenchmarkMain` performs one excluded warmup and then measures three independently reported workloads:

- runtime startup and shutdown;
- a DuckDB `range` aggregation;
- graph transitive closure on a chain, including the expected O(k²) output cardinality.
- offline Arrow IPC egress and file/stream ingress, including cardinality validation.

For a focused Arrow file-ingress comparison that excludes startup, graph work, row-ID assignment, and the pipe union, pass `--arrow-only`. It performs one excluded warmup, writes one Arrow IPC file per run, times both `ArrowFileSupport.read` (prepared JDBC row insertion) and `readWithAppender` (DuckDB's native JDBC Appender API), alternates which importer runs first, and verifies both imported cardinalities. By default there is no concurrent resource sampler, keeping timing evidence separate from resource diagnostics. Add `--resource-sampling` for a separate diagnostic run that polls process RSS and isolated DuckDB temp-directory bytes every 50 ms during each import; these best-effort maxima are not guaranteed peaks, and RSS remains process-wide rather than method-isolated. JSON includes raw samples, medians/statistics, file bytes, throughput, and Appender speedup. The Appender is a row-wise native append API—not Arrow C Data, zero-copy, or vectorized ingestion. Arrow requires the same Java module opens as Surefire. Pipe ingestion now uses the Appender route by default; retain the prepared-statement path as a comparison/fallback implementation until broader Arrow parity and reliability qualification is complete. Example on Windows:

```powershell
./mvnw.cmd -pl engine-duckdb -am -DskipTests package
./mvnw.cmd -q -pl engine-duckdb dependency:build-classpath '-Dmdep.outputFile=target/benchmark-classpath.txt'
$cp = (Get-Content engine-duckdb/target/benchmark-classpath.txt -Raw).Trim()
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED `
  -cp "engine-duckdb/target/classes;engine-api/target/classes;$cp" `
  io.zingg.duckdb.engine.BenchmarkMain --arrow-only --size 10000 --repetitions 5
```

Equivalent WSL/Linux invocation (first install reactor artifacts in that OS's local Maven repository, e.g. `./mvnw -pl engine-duckdb -am -DskipTests install`):

```bash
./mvnw -q -pl engine-duckdb dependency:build-classpath -Dmdep.outputFile=target/benchmark-classpath.txt
CP=$(cat engine-duckdb/target/benchmark-classpath.txt)
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  -cp "engine-duckdb/target/classes:engine-api/target/classes:$CP" \
  io.zingg.duckdb.engine.BenchmarkMain --arrow-only --size 10000 --repetitions 5
```

Build and run a smoke benchmark from the repository root:

```powershell
./mvnw.cmd -pl engine-duckdb -am -DskipTests package
$duck = (Get-ChildItem "$env:USERPROFILE\.m2\repository\org\duckdb\duckdb_jdbc\1.5.5.1\duckdb_jdbc-1.5.5.1.jar" | Select-Object -First 1 -ExpandProperty FullName)
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED `
  -cp "engine-duckdb\target\classes;engine-api\target\classes;$duck" `
  io.zingg.duckdb.engine.BenchmarkMain --size 1000 --repetitions 5
```

Use increasing `--size` values for capacity observations. The 2026-09-28 Windows/WSL three-repetition direct-ingress baselines are in `arrow-ingress-2026-09-28-windows.json` and `arrow-ingress-2026-09-28-wsl.json`. The five-repetition Appender comparison from that date is recorded in the corresponding `arrow-ingress-appender-2026-09-28-*.json` files. These are single-machine diagnostics, not a comparison with Arrow C Data/CTAS vectorized ingestion, and include no RSS/spill accounting or release threshold. Results are machine- and JVM-dependent; store the JSON output with JDK, DuckDB version, OS, CPU, memory, and thread configuration before comparing runs. These benchmarks are not correctness tests and do not establish release thresholds by themselves. The module opens shown in the command are required for Arrow IPC under Java module encapsulation. Graph closure now rejects before result allocation if the implied-pair count exceeds `GraphOutput.DEFAULT_MAX_TRANSITIVE_PAIRS` (1,000,000); use smaller fixtures or an explicit budget overload for larger controlled benchmarks.

### 2026-09-30 graph-cap smoke benchmark

The documented general smoke workload (`size=1000`, five measured repetitions after one warmup) was run after the transitive-pair cap was added. This workload supplies 999 edges and materializes 499,500 closure pairs, within the one-million-pair default. It passed on Windows 11 and Ubuntu 24.04/WSL2 under Microsoft OpenJDK 21.0.12.1 and Ubuntu OpenJDK 21.0.12.1, respectively. Median graph closure times were 126.813 ms (Windows) and 57.111 ms (WSL2); the full smoke JSON reported startup, SQL, Arrow write, and Arrow read samples too. These timing samples are noisy single-host diagnostics (WSL shares the laptop/storage), with no RSS/spill sampling or acceptance threshold; they do not establish capacity or a performance regression budget.

### Current Arrow-ingress diagnostic (2026-09-30)

On the same 13th Gen Intel Core i7-1355U Windows host and WSL2 environment, `BenchmarkMain --arrow-only --size 10000 --repetitions 7` (one warmup excluded) reported. Machine-readable summary: [`arrow-ingress-2026-09-30-cross-platform.json`](arrow-ingress-2026-09-30-cross-platform.json).

| Environment | Java | JDBC median | Appender median | Appender/JDBC ratio |
| --- | --- | ---: | ---: | ---: |
| Windows host | Microsoft OpenJDK 21.0.12.1 | 2.617 s | 20.051 ms | 130.5× |
| Ubuntu 24.04 on WSL2 | Ubuntu OpenJDK 21.0.12.1 | 4.159 s | 17.084 ms | 243.4× |

The IPC fixture was 294,888 bytes; both implementations validated 10,000 imported rows on each measured run. Raw seven-run timing samples and min/max/mean/population-standard-deviation summaries are preserved in the linked JSON report. A background sampler polls process RSS and the isolated DuckDB temp directory every 50 ms during each JDBC and Appender import, then records maximum observed values and sample counts per run. The report also captures process RSS immediately before and after the full benchmark and bytes remaining in the isolated temp directory before and after. Sampled maxima are not guaranteed OS-level peaks: short operations can finish between polls, and sampling overhead can influence timing (especially on Windows, where RSS uses a system process query). These remain local diagnostics, not independent-runner or release evidence: the two environments share one laptop, WSL uses the Windows-mounted checkout/storage, and the benchmark excludes CPU counters, confidence intervals, 100k/capacity-sized inputs, and vectorized Arrow C Data/CTAS alternatives. Both harnesses exclude one warmup and emit raw measured samples plus descriptive summaries. Linux CI validates warmup metadata, sample counts, positive timings/RSS, and summary consistency for the general and Arrow harnesses; it still does not enforce regression budgets.

Correction for the 2026-09-30 report above: the table's timing medians are from runs with resource sampling disabled; a separate `--resource-sampling` run produced the per-method RSS/spill samples in the JSON report. Resource sampling is a separate diagnostic because its background work can perturb wall-clock timings. RSS is shared-process and cumulative; method order alternates to reduce ordering bias, but this is not process-isolated memory attribution. Appender phases often complete between 50 ms polls, yielding only boundary observations. Treat the earlier paragraph's claim that timings were sampled concurrently as superseded. Linux CI now checks that Arrow report medians, descriptive statistics, throughput and Appender/JDBC ratio are internally consistent with raw samples and fixture metadata; this detects malformed evidence, not performance regressions or unfair workload selection.

### Docker/WSL2 Arrow-ingress scaling diagnostic (2026-10-02)

The existing `--arrow-only` benchmark was built and run inside `maven:3.9.9-eclipse-temurin-21` (Ubuntu 24.04.2, Temurin 21.0.7, DuckDB JDBC 1.5.5.1) for 1k, 10k and 100k rows, with one excluded warmup and three measured repetitions per size. Every run validated imported row count. Raw samples and environment notes are in [`arrow-ingress-2026-10-02-docker-wsl.json`](arrow-ingress-2026-10-02-docker-wsl.json).

| Rows | IPC bytes | JDBC median | Appender median | Appender/JDBC ratio |
| ---: | ---: | ---: | ---: | ---: |
| 1,000 | 29,688 | 586.8 ms | 4.72 ms | 124.2× |
| 10,000 | 294,888 | 4.201 s | 15.94 ms | 263.6× |
| 100,000 | 2,946,392 | 41.025 s | 113.24 ms | 362.3× |

This is a single Docker-on-WSL2 diagnostic with only three repeats; it lacks per-path peak RSS/spill, CPU counters, independent-runner reproduction and any vectorized Arrow C Data/CTAS candidate. The process-wide RSS rose from about 45 MB before the benchmark to 361 MB afterward at 100k; it cannot be attributed to a specific import path. These results do not define acceptance thresholds or independently qualify Appender as the final path. The required C Data/CTAS comparison and reproducible multi-runner resource evidence remain open.

### Graph aggregation versus explicit pair expansion

Run the dedicated comparator in Docker after building/installing the engine modules in the same container. It excludes one warmup, alternates operation order, times computation separately from CSV serialization, verifies output row counts, and records actual serialized CSV bytes. Run clean timing and resource sampling as separate profiles; RSS is process-wide and the 50 ms sampler can miss short-lived peaks. Example:

```bash
mvn -pl engine-duckdb -am -DskipTests install
mvn -pl engine-duckdb exec:java \
  -Dexec.mainClass=io.zingg.duckdb.engine.BenchmarkMain \
  -Dexec.args="--graph-compare --shape path --size 500 --repetitions 3"
```

`--shape` accepts `path` (default), `star`, `dense`, or `disconnected`; `size` is the vertex count. Path, star, and dense each have a single connected component and therefore produce `V*(V-1)/2` closure rows. The disconnected fixture has components of size two (and one size-three component for odd vertex counts). Pair count is checked against the default one-million-pair cap before allocating graph edges. Add `--resource-sampling` for best-effort RSS/spill samples; its 50 ms process-wide sampler is not guaranteed to capture a peak.

The first path-only Docker profile is at [`graph-compare-2026-10-02-docker.json`](graph-compare-2026-10-02-docker.json). An eight-case path/star/dense/disconnected × 100/500 matrix is summarized in [`graph-shape-matrix-2026-10-02-docker.json`](graph-shape-matrix-2026-10-02-docker.json). Separate sampled diagnostics for a 1,200-vertex path and a 1,000-vertex dense graph are in [`graph-resource-sampling-2026-10-02-docker.json`](graph-resource-sampling-2026-10-02-docker.json); RSS is process-wide and the sampler can miss peaks, so this is not capacity or release evidence. All results are single Docker-on-WSL2 exploratory measurements. Dense input also grows quadratically. Independent runners, representative capacity sizes, stronger peak-resource measurement, spill observations, and agreed thresholds remain open.

### Native classifier-training scaling diagnostic

The compatibility module includes `ClassifierTrainingBenchmarkMain`, a deterministic benchmark of the DuckDB-native full-batch logistic trainer. It defaults to 10k/100k rows and 2/8 numeric features, excludes one warmup, alternates no implementations (each configuration uses a fresh runtime/database), and runs three measured repetitions. The label is deliberately correlated with feature 0, and each run computes an independent SQL log-loss after training; the runner fails if training does not beat the random-classifier `ln(2)` baseline. It verifies input-row cardinality and non-empty artifact output.

Run in a Linux Docker container after compiling the reactor and generating the compatibility module's runtime classpath:

```bash
mvn -pl compat-zingg07-runtime -am -DskipTests package dependency:build-classpath \
  -Dmdep.outputFile=target/benchmark-classpath.txt
CP="compat-zingg07-runtime/target/classes:engine-api/target/classes:engine-duckdb/target/classes:model-format/target/classes:$(cat compat-zingg07-runtime/target/benchmark-classpath.txt)"
java --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  -cp "$CP" io.zingg.duckdb.compat.ClassifierTrainingBenchmarkMain \
  --rows 10000,100000,1000000 --widths 2,8,32 --iterations 20 --repetitions 3 \
  --output benchmarks/classifier-training-local.json
python3 packaging/verify-benchmark-report.py benchmarks/classifier-training-local.json --classifier-training
```

The timing interval covers `trainClassifier` only; runtime startup, generated input construction, and the post-training validation query are outside it. Reports include raw elapsed samples, rows/s, feature width, deterministic input/configuration digests, artifact bytes, warmup/measured loss, process RSS immediately before/after the training call, and isolated spill-directory bytes immediately before/after. RSS endpoints are not peak-memory attribution, and post-call spill can understate transient spill. This is diagnostic—not Spark/Zingg parity, not a DuckDB plan capture, no numerical tolerance oracle, no cancellation-latency measurement, and no CI performance threshold. The 1M×32 case can consume substantial CPU/time; select sizes and widths explicitly for constrained containers. Store reports with immutable image/commit provenance before comparing machines.

Docker/WSL2 exploratory run (2026-10-02), `maven:3.9.9-eclipse-temurin-21`, JDK 21.0.7, DuckDB JDBC 1.5.5.1, 8 reported processors, 20 iterations, three measured repetitions. Full six-case raw report (schema v2): [`classifier-training-plan-scaling-docker-2026-10-02.json`](classifier-training-plan-scaling-docker-2026-10-02.json). Median training-only times were 170 ms (10k×2), 282 ms (10k×8), 176 ms (100k×2), 527 ms (100k×8), 577 ms (1M×2), and 1.350 s (1M×8). The initial gradient physical plan is captured only on the excluded warmup, so measured timings do not include EXPLAIN. All measured losses were below synthetic random-classifier baseline `ln(2)` and every run trained the requested full row count. Each case includes the complete physical plan and full SHA-256. The v1 report is superseded because `DuckFrame.explain()` returned the EXPLAIN column label rather than its value; this was corrected and a regression now requires an aggregate node. No spill bytes remained at the post-call sample; those point samples can miss transient spill. This was generated from revision `182a3f1ebcea8381fc7a1e4d9a86b87db49c57bb` with uncommitted local changes present, so it is not reproducible from that commit alone. Single shared Docker-on-WSL2 host; diagnostic only, no release threshold.

## Experimental GraalVM Native Image comparison

On Linux x64 with GraalVM for Java 21 and the required C toolchain installed, build and compare the actual worker against the JVM implementation:

```bash
./mvnw -DskipTests package
sh packaging/build-native-image.sh native-image dist-native zingg-duckdb-worker-linux-x64
GRAALVM_VERSION="Oracle GraalVM 21.0.12+7.1" \
python3 packaging/compare_native_image.py dist-native/zingg-duckdb-worker-linux-x64 \
  --warmups 1 --repetitions 5 --match-sizes 64 128 256 \
  --report benchmarks/native-image-local.json
```

The comparator performs fresh-process paired runs, discards configurable warmups, alternates which runtime launches first, and verifies ping, SQL row counts, classifier training/reload, and full MATCH rows at each increasing input size. It records latency, input/output rows per second, output bytes/hash, post-MATCH RSS/temp-directory bytes, artifact sizes, raw samples, and median/range/population-deviation summaries. A pair fails if either complete output rows or worker resource/configuration settings differ. Reports include Java and required Native Image toolchain versions, OS release/architecture, CPU count, launch flags, and artifact/JAR SHA-256. The WSL2 schema-v3 startup smoke is retained at `native-image-repeated-2026-09-29-wsl-v3.json`; the schema-v4 scaling run with sizes 64/128/256 is at `native-image-scaling-2026-09-29-wsl.json` after generation. These WSL measurements are still development diagnostics, not representative capacity limits, hosted/native Linux qualification, or release approval. Native Image remains experimental.
