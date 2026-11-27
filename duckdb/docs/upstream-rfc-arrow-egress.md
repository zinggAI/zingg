# Upstream compatibility RFC: offline Arrow egress

## Problem

The pinned DuckDB JDBC runtime (`1.5.5.1`) rejects the default SQL form
`COPY (<query>) TO '<path>' (FORMAT ARROW)`: the Arrow copy function is not
available in the offline runtime. Reproduction is covered by
`DuckFrameMetadataTest` and produced `Catalog Error: Copy Function with name
arrow does not exist!`.

This makes extension-dependent Arrow egress unsuitable as the default data
plane for a self-contained Zingg DuckDB package.

## Current project workaround

The project now reads JDBC results and writes Arrow IPC directly with Apache
Arrow 19.0.0. The writer uses atomic `.partial` publication and explicit Java
21 module opens in the supported launchers. Primitive, null, multi-batch,
date, timestamp, binary, and decimal cases are covered by tests. Unsupported
`LIST`/`STRUCT` JDBC values are rejected instead of silently coerced.

## Requested upstream clarification

Please document one of the following supported contracts:

1. an offline, extension-free Arrow COPY function available through JDBC; or
2. a stable JDBC result-to-Arrow interoperability contract that guarantees
   type metadata and value access for primitive and nested DuckDB types.

The second contract should specify `LIST`, `STRUCT`, `MAP`, `DECIMAL`,
timestamp units/time zones, binary values, nulls, empty results, and error
behavior. It should also state whether Arrow IPC stream or file framing is
the supported interchange format.

## Acceptance criteria

- The contract works without installing or loading an extension.
- It is available on Linux, Windows, and macOS with the pinned JDBC artifact.
- Primitive and nested values have deterministic Arrow schemas.
- Empty, malformed, cancelled, and partially written operations have defined
  behavior.
- A compatibility test can run offline in CI against the published JDBC jar.

## Scope boundary

This RFC does not request a change to Zingg semantics or model formats. It is
limited to the backend data-plane contract demonstrated by the failure above.
