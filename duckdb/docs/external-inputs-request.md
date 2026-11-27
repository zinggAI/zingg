# External inputs required to complete strict compatibility

This checklist is the handoff needed before the strict Zingg compatibility phases can be implemented and tested. No substitute fixture or inferred contract may be promoted to authoritative evidence.

## Required inputs

| ID | Input | Minimum acceptance criteria | Owner/status |
|---|---|---|---|
| EXT-01 | Authority/provenance for exact Zingg v0.7 contracts | **Local candidate exists:** source commit `48cb157b4f35fcfa733e2e7f9699ea988e018738`, canonical `zingg-common-client-0.7.0.jar` SHA-256 `a289903fae2ed9ff3a0a3360fed3c80232d70fce848df27e2c6a6ee8406c4b5d`, reproducible local Windows/WSL bootstrap, and all 93 `ZFrame` signatures verified. Still required: maintainer confirmation this commit/API is the compatibility authority; reviewed source, transitive dependency, license and notice provenance; pinned signature inventories/compile contract for `Context`, `DSUtil`, `GraphUtil`, `ModelUtil`, `PipeUtilBase`, model and phase interfaces (the checked-in 93-method inventory covers `ZFrame` only). | Maintainer — partial; approval and broader contract inventory pending |
| EXT-02 | Blocking model | An authoritative artifact compatible with the pinned v0.7 `Tree<Canopy<Row>>` contract, plus approved source records and expected neutral tree nodes, field context, hashes, and edges | The integrity-verified 45-file candidate is present in pinned v0.7 test resources, but its blocking file is byte-identical to upstream v0.3.4's historical test resource (tag commit `297c7906e73afe7d8d86ee46681b152d212699f6`, blob `68b9faeae91e31ce979aee5a51d1ea35ba691331`; SHA-256 `204d67ec3ffebc538102d20d860ffcf9237f9962aec66e8a3c645d0aefca526f`). This proves it is historical content carried into the later tree, **not** that it is an authoritative or directly compatible v0.7 artifact. It serializes `zingg.block.*`/`zingg.client.*`/`zingg.hash.*`, whereas pinned source uses `zingg.common.*`; its Tree UID (`-7879348438713840442`) differs from pinned v0.7 (`69916775799935088`). Strict isolated importer rejects it on Windows/WSL; do not alias classes or relax the filter. Needed: maintainer confirms authority/version contract and supplies either a compatible approved artifact, or historical source plus an approved field-by-field migration contract; then provide source records/goldens and pass neutral-tree differential validation. |
| EXT-03 | Spark ML classifier | Real v0.7 classifier with VectorAssembler, PolynomialExpansion, LogisticRegression metadata, feature order, coefficients, intercept, threshold, and expected margins/probabilities | Candidate imported and locally compared to Spark 3.5.5; sorted 45-file manifest now verifies every file including hidden CRC sidecars. Approved score goldens, supported-format boundary, maintainer authority and full v0.7 differential validation pending |
| EXT-04 | Differential fixtures | Spark-produced outputs for FIND_TRAINING_DATA, labeling, TRAIN, MATCH, and LINK, including nulls, duplicates, empty inputs, ordering, IDs, pair shape, predictions, and clusters | Compatibility owner — pending |
| EXT-05 | Integration decision | Written confirmation of standalone repository versus `home/duckdb`, supported phase boundary, and external Spark prerequisite policy | Maintainer — pending |
| EXT-06 | Release environment | Hosted Linux amd64/arm64, macOS x86_64/arm64, Windows x86_64 runners with Java 21, Python matrix, signing identity, and security-scanning access | Release owner — pending |

## Intake procedure

1. Place received artifacts in a non-runtime fixture directory and record original filenames, source URLs, commit/tag, licenses, producer versions, and SHA-256 hashes.
2. Add a provenance manifest and review it into version control; never place Spark classes or proprietary artifacts in pure engine runtime dependencies.
3. Run import preflight before any implementation work: file-size limits, schema validation, checksum verification, model-version/profile validation, and malware/license review.
4. Generate a small sanitized golden fixture from each artifact and commit expected outputs separately from the original artifact.
5. Re-run the adapter compilation, model import, differential, restart, and full phase tests on a clean checkout.
6. Promote an input from “pending” only when its acceptance criteria and independent reviewer approval are recorded in `docs/validation-status.md`.

## Blocking rule

Until EXT-01 through EXT-05 are supplied, the repository may continue local backend, lifecycle, packaging, and safety work, but it must not claim exact Zingg v0.7 phase/model parity. EXT-06 is required before cross-platform release claims, not before local development.
