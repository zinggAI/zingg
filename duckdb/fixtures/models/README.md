# Golden native model artifacts

These directories are immutable format fixtures, not executable tests. Each contains `model.bin`, `manifest.json`, and `provenance.json`; the manifest and provenance SHA-256 values must equal the payload hash. They are used by model readers, migration tooling, and release review to detect accidental format drift.
