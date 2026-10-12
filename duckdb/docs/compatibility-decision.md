# Compatibility boundary decision

Status: provisional pending maintainer confirmation
Date: 2026-09-23

## Current decision

This repository is treated as a standalone DuckDB backend and compatibility capsule. The pure `engine-*` modules remain Spark-free. Zingg v0.7 compatibility is isolated behind the adapter/importer boundary and is not claimed for the pure engine until exact upstream contracts and differential fixtures are available.

The pinned implementation baseline is:

- Zingg compatibility target: `v0.7.0`
- Active DuckDB JDBC profile: `1.5.6.0`
- Retained released DuckDB JDBC profile: `1.5.5.1` (immutable)
- Java: `21`
- Python control-plane baseline: `3.10+`
- Default runtime mode: offline, with extension auto-installation disabled
- Supported locally proven scope: DuckDB relational execution, resource/lifecycle controls, model envelopes, structured worker protocol, compatibility-function subset, graph output, and isolated legacy import scaffolding
- Not yet proven: exact Zingg generic contracts, real v0.7 classifier parity, complete phase orchestration, and Spark differential equivalence

## Required maintainer confirmation

Before strict compatibility or release claims are enabled, confirm:

1. Whether the implementation must be moved into the main Zingg `home/duckdb` module.
2. Whether legacy Spark importers may require an externally installed Spark distribution.
3. Whether v0.7 MATCH-only operation is an acceptable intermediate release boundary while FIND_TRAINING_DATA, TRAIN, and LINK parity are completed.
4. Which exact Zingg v0.7 generic-contract artifacts and real model fixtures are authoritative.

Until these decisions are confirmed, CI must keep strict adapter, real-artifact, and differential gates visible as pending rather than silently treating the standalone backend as full Zingg parity.
