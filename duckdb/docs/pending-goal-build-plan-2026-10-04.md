# Build and test plan: Zingg DuckDB pending gates

Date: 2026-10-04

## Scope

This plan covers only the three explicit pending items for `zingg-duckdb`:

1. exact `SecondString/Jaccard` behavior used by Zingg v0.7;
2. hosted qualification on Linux amd64, Linux ARM64, macOS x86_64, macOS ARM64, and Windows x86_64;
3. promotion of DuckDB JDBC `1.5.6.0` as the active package profile while retaining the released `1.5.5.1` profile unchanged.

Other audit items remain outside this change.

## Acceptance gates

### Similarity parity

- Pin the upstream Zingg v0.7 `secondstring.jar` at commit `21f54e9b4693d49d6f26b2851853b140951a7502`.
- Verify SHA-256 `5267ae94cb2feff6b75bf112c1d22976b7835e04bad14b369cbd5525f20b520` before using it as an oracle.
- Compare the local implementation against `com.wcohen.ss.Jaccard` through the same null, empty, equality, and `NaN -> 0` wrapper used by Zingg.
- Exercise locale-sensitive, Unicode, punctuation-only, whitespace-only, repeated-token, and mixed letter/digit inputs.

### Profile promotion

- Keep `zingg-0.7.0-duckdb-1.5.5.1` registered and immutable.
- Make `zingg-0.7.0-duckdb-1.5.6.0` the default and package profile.
- Record `secondstring-exact` semantics in the profile, manifest, capsule, locks, provenance, verifier, and notices.
- Run the complete Java, Python, packaging, package-verifier, and packaged-worker checks against `1.5.6.0`.

### Hosted qualification

- Add a required native-validation matrix for Linux amd64/ARM64, macOS x86_64/ARM64, and Windows x86_64.
- Run the full Maven and Python suites on every matrix target.
- Download and checksum the pinned SecondString oracle in CI and run the differential test on each target.
- Update the mandatory DuckDB canary and Linux validation classpaths to `1.5.6.0`.
- Treat a new GitHub Actions run on the published commit as the evidence needed to close this gate; local Docker execution is not a substitute for hosted/macOS/ARM execution.

## Execution record

- SecondString differential test in Docker: passed, 1 differential test covering all 36 corpus values under English and Turkish locales; 0 failures.
- Full Maven reactor in Docker with DuckDB `1.5.6.0`: passed, 311 tests reported / 309 executed, 0 failures/errors, 2 pre-existing opt-in skips.
- Python suite in Docker: passed, 25 tests, 5 packaged-worker skips because no package was mounted in that isolated run.
- Packaging suite in Docker: passed, 103 tests, 1 platform skip.
- Hosted matrix: workflow implemented and statically validated; a post-change hosted run is still required after publication.

## Implementation map

- Similarity implementation and registry: `engine-duckdb/src/main/java/io/zingg/duckdb/engine/SimilarityFunctions.java`, `SimilarityRegistry.java`.
- Profile selection: `compat-zingg07-runtime/src/main/java/io/zingg/duckdb/compat/ProfileRegistry.java`, `CompatibilityRuntime.java`.
- Oracle test: `engine-duckdb/src/secondstring-test/java/io/zingg/duckdb/engine/SecondStringJaccardDifferentialTest.java`.
- Hosted matrix: `.github/workflows/build.yml` (`native-validation`).
- Package contract: `packaging/compatibility-profile.json`, `runtime-manifest.json`, `compatibility-capsule.json`, and `verify_package.py`.
