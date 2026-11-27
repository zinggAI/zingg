# Upstream Zingg fixture candidate

Audit date: 2026-09-25

The pinned upstream commit `48cb157b4f35fcfa733e2e7f9699ea988e018738` in
`https://github.com/zinggai/zingg.git` contains a test model under
`common/core/src/test/resources/models/100/model`. The blocking artifact is
not evidence of a v0.7-compatible serialization: its exact bytes already occur
in the upstream v0.3.4 test resource at
`core/src/test/resources/models/100/model/block/zingg.block` (tag commit
`297c7906e73afe7d8d86ee46681b152d212699f6`, dated 2022-08-04). The same
historical bytes were carried into the pinned tree. This establishes the
artifact's repository history, but does not establish which release generated
it, whether maintainers intend it as a v0.7 compatibility fixture, or that it
can be read by the v0.7 classes.

The candidate includes:

- `model/block/zingg.block` (blocking-tree artifact)
- `model/classifier/best.model/bestModel` (Spark ML pipeline)
- `VectorAssembler` metadata with 18 ordered input columns (`z_sim0` through `z_sim17`)
- `PolynomialExpansion` metadata with degree `3`
- `LogisticRegressionModel` metadata with threshold `0.4`, intercept enabled, and
  feature column `z_feature`

The classifier metadata identifies Spark `3.0.1`; this must be treated as an
upstream fixture compatibility fact, not silently relabeled as Spark `3.5.5`.

## Blocking artifact compatibility finding (2026-09-28)

The binary stream header in `block/zingg.block` names serialized classes
`zingg.block.Tree`, `zingg.block.Canopy`, `zingg.client.FieldDefinition`,
`zingg.client.MatchType`, and `zingg.hash.*`. The pinned source at
`48cb157b4f35fcfa733e2e7f9699ea988e018738` instead defines the corresponding
tree/model classes under `zingg.common.core.block.*`, `zingg.common.client.*`,
and `zingg.common.core.hash.*`. The isolated importer intentionally allows the
newer pinned namespaces and therefore rejects this binary before model
publication. A focused regression reproduces that fail-closed result on
Windows and WSL. The bytes are integrity-verified, but they are not yet proven
to be a directly importable artifact for the pinned source/API contract; do
not describe blocking-tree import as complete until maintainers confirm the
legacy class-layout contract or provide an approved compatible fixture.

## Provenance and intake status

The commit is available locally through the upstream repository clone. The
fixture directory is copied under
`compat-zingg07-runtime/src/test/resources/external/zingg-v07-model-48cb157`.
It is test-only data, is not a runtime dependency, and is not visible to
`engine-*` modules.

The blocking artifact SHA-256 observed during intake was:

```text
204D67EC3FFEBC538102D20D860FFCF9237F9962AEC66E8A3C645D0AEFCA526F  model/block/zingg.block
```

The classifier metadata manifest blob observed during intake was:

```text
A56DE4649E42FDBE7F29B2105428F617862D2345A563E73C7DC766BF75E0596D  model/classifier/best.model/bestModel/metadata/part-00000
```

The complete model directory contains **45 files** at this commit, including
Spark marker/CRC files, JSON metadata, and the Parquet coefficient data file.
The sorted per-file SHA-256 manifest is checked in as
`compat-zingg07-runtime/src/test/resources/external/zingg-v07-model-48cb157/SHA256SUMS`.
`Zingg07FixtureIntakeTest` now hashes all 45 files from the source tree (including
hidden `.crc` sidecars), rejects unsafe/duplicate paths and symlinks, and requires
the manifest file set to exactly match the `block/` and `classifier/` trees. Key
additional hashes are:

```text
E68E42FDE22E2BF4FF81AA9FF63212D6D0C15CEB25EABCBB6678D4930E1939EC  bestModel/stages/2_logreg_a0b6b01da133/data/part-00000-1868afb6-4fe2-43d9-bdfe-fea9fa59bd7e-c000.snappy.parquet
71A415F5BD30BA1B6BAEC1AF00C19C94CF2725879F3BEA15C227429D424DFC3F  bestModel/stages/0_vecAssembler_ca0ffaafe2a1/metadata/part-00000
3EC3EE71606492E1F08F91FFDC9F30C426AC634BB36ECAA755C568E211326E84  bestModel/stages/1_poly_83edb6284120/metadata/part-00000
A5D79A81FD1EC08100EE06694887C675315CA25580DBD3A3E6D8BA35E95EB93A  bestModel/stages/2_logreg_a0b6b01da133/metadata/part-00000
```

The source license, binary license, producer-version provenance, and maintainer
approval must still be recorded before promotion to an authoritative fixture.

### Historical fixture provenance (2026-09-29)

Git object inspection of upstream tag `v0.3.4` confirmed the blocking artifact
has blob `68b9faeae91e31ce979aee5a51d1ea35ba691331`; its SHA-256 is identical to
the locally checked-in candidate (`204d67ec3ffebc538102d20d860ffcf9237f9962aec66e8a3c645d0aefca526f`).
The pinned v0.7 tree also contains these same historical bytes. The v0.3.4
source declares `zingg.block.Tree`, `zingg.block.Canopy`,
`zingg.client.FieldDefinition`, and `zingg.client.MatchType`, matching the
stream's legacy namespaces. The pinned v0.7 source uses `zingg.common.*`; the
serialized Tree UID differs from the v0.7 Tree UID, so aliasing class names is
insufficient. Do not infer that all 45 fixture files were generated by v0.3.4
from this one-file identity, or that copying this fixture into the v0.7 source
tree makes it a compatible v0.7 model.

### Serialization UID finding (2026-09-28)

A local descriptor inspection reports `zingg.block.Tree` with
`serialVersionUID=-7879348438713840442`; the pinned 0.7.0
`zingg.common.core.block.Tree` computes `serialVersionUID=69916775799935088`
under JDK 21 (`serialver`). A simple `ObjectInputStream.resolveClass` alias
would still fail Java's serialization compatibility check. Do not weaken the
class filter or replace stream descriptors without a reviewed migration
contract. The safe next step is an approved artifact generated with the pinned
layout, or a versioned field-by-field migration based on historical class
definitions and validated against approved source records and neutral-tree
goldens.

## Required next steps

1. [x] Generate and commit a sorted per-file SHA-256 manifest, including Parquet model data; executable intake verification covers every manifest entry.
2. Record and review the upstream license and fixture-generation provenance.
3. Confirm with the maintainer that this fixture is authoritative for the Zingg v0.7 compatibility target.
4. Obtain an authoritative blocking-tree artifact/serialization contract compatible with the pinned API (or approved historical class definitions and a reviewed field migration), then complete import tests and compare neutral tree contents with approved upstream goldens. Package-name aliasing alone is disproven by the `Tree` serial UID mismatch. Classifier import, fixture integrity, and local Spark 3.5.5 score comparisons are implemented; they still need authoritative Spark 3.0.1/v0.7 outputs and a supported-format boundary review.

The blocker is now precisely: “historical candidate identified and integrity
verified; its authority and compatible v0.7 blocking serialization are
unproven; approved source records/goldens and differential validation remain
pending.”
