# Native model format

New artifacts are directories containing `manifest.json`, `model.bin`, and `provenance.json`. The payload is opaque to the neutral store but is checksummed with SHA-256. The manifest pins the compatibility profile, Zingg version, format version, model type, features, and checksum. Java serialization is not used for new artifacts; legacy Spark artifacts cross the isolated importer boundary and are converted before entering the normal runtime.

The native TRAIN path emits a `native-blocking-tree` JSON payload for the blocking configuration, sampled-row count, and deterministic blocking-key frequency table. Null keys are represented by the reserved `__NULL__` bucket. The payload is intentionally Spark-free; the manifest and provenance remain the authoritative compatibility and audit metadata.

## Migration policy

`zingg-0.1-native` is the only readable native format in this release. The reader accepts only the `zingg-0.7.0` compatibility profile and rejects unknown format versions, Zingg versions, model types, profiles, missing provenance, and checksum mismatches explicitly. There is no implicit in-place migration: a future format must provide a separately versioned migration step that writes a new artifact and preserves the original directory unchanged.
