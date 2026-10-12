# Semantic inventory

Compatibility profiles: released `zingg-0.7.0-duckdb-1.5.5.1` (immutable) and active `zingg-0.7.0-duckdb-1.5.6.0`.

This inventory is the implementation-facing contract. New functions or operations must be added here before they are exposed through a public compatibility profile.

## Match and scoring types

The pinned capsule's `zingg.common.client.MatchTypes` exposes 11 generic field-feature identifiers. They are value objects, not Java enum constants. An opt-in reflection probe checks the exact pinned capsule inventory. **The current native DuckDB trainer/matcher does not implement these per-field feature generators**; accepting their names must not be confused with semantic support.

| Upstream identifier | Local v0.7 feature implementation | Required parity fixture/status |
|---|---|---|
| `FUZZY` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `EXACT` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `PINCODE` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `EMAIL` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `TEXT` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `NUMERIC` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `NUMERIC_WITH_UNITS` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `NULL_OR_BLANK` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `ONLY_ALPHABETS_EXACT` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `ONLY_ALPHABETS_FUZZY` | Not implemented in native feature generation | blocked on approved v0.7 training feature vectors |
| `DONT_USE` | Not interpreted as a per-field declaration by the native trainer | blocked on approved v0.7 output/schema fixtures |

Other native model families are separate: `BLOCKING_TREE` currently means a deterministic frequency-based experimental artifact (not upstream blocking parity); `CLASSIFIER` covers native linear/logistic models and the constrained Spark ML importer; `predicate` is the explicit DuckDB matcher path. The imported Spark envelope requires exactly three ordered pipeline stages (VectorAssembler → degree-3 PolynomialExpansion → binary LogisticRegressionModel) with verified feature-column wiring; extra/reordered stages and disconnected columns fail before publication. The checked-in candidate's authority/provenance is still unapproved. These are implementation/profile concepts, not upstream `MatchType` identifiers.

## Hash and normalization functions

| Name | Null behavior | Input/coercion | Notes |
|---|---|---|
| `java_hash` | integer `0` | Java `String.hashCode`; UTF-16 code units; null maps to 0. |
| `lower` | `null` | String; trim then lowercase with `Locale.ROOT`. |
| `trim` | `null` | String; Java `String.trim` semantics. |
| `sha256` | `null` | UTF-8 bytes; lowercase hexadecimal. |
| `md5` | `null` | UTF-8 bytes; lowercase hexadecimal; legacy only. |
| `length` | `null` | Java string length (UTF-16 code units). |
| `java_round` | `null` | Numeric string parsed as `double`, then `Math.round`; malformed numbers reject with `DuckException`. |

## Similarity functions

| Name | Domain | Null/empty behavior | Normalization/quirks |
|---|---|---|---|
| `exact` | `[0,1]` | Any null operand scores `0`, including null/null | Case- and punctuation-sensitive exact string equality. |
| `jaccard` | `[0,1]` | Active profile follows Zingg v0.7's wrapper: null/empty or equal strings score `1`; different empty-token inputs score `0` | SecondString `SimpleTokenizer` semantics: default-locale lowercase, whitespace/letter/digit token runs, punctuation ignored; compares token sets, so repeats do not add weight. The released 1.5.5.1 profile retains its historical blank-safe score of `1`. |
| `normalized_levenshtein` | `[0,1]` | Any null operand scores `0`; two empty strings score `1` | `1 - editDistance / max(UTF-16 lengths)`; exact string, case and punctuation are retained. |
| `jaro` | `[0,1]` | Any null operand scores `0`; equal strings (including empty) score `1` | Character-based Jaro, case-sensitive; no Winkler prefix boost. |
| `jaro_winkler` | `[0,1]` | Same as `jaro` | Released v0.7 quirk: delegates to plain Jaro; it is not a Winkler-scored implementation. |

`SimilarityRegistry` rejects unknown names and any non-finite or out-of-range result. Tests pin the full hash/function registry name sets; the profile's exact feature semantics still require v0.7-approved fixtures where indicated.

## ZFrame operation matrix

The exact, hash-pinned 93-signature upstream interface and per-signature routing disposition are maintained in [`zframe-v07-adapter-matrix.json`](zframe-v07-adapter-matrix.json), with semantic differential status in [`zframe-v07-coverage.md`](zframe-v07-coverage.md). Routing is not semantic parity. The table below is a semantic index of operation families, not a claim that every overload/edge case is already validated.

| Operation | Ordering | Null/type behavior | Duplicate behavior |
|---|---|---|---|
| Family (upstream methods in matrix) | Ordering contract | Null/type/coercion contract | Duplicate/multiplicity contract |
|---|---|---|---|
| Projection/access (`select`, `selectExpr`, `columns`, `fields`, `fieldNames`, `fieldIndex`, `head`, `get*`, `toDF`, `as`) | Projection preserves requested ordinal order; accessors follow schema/row position | Explicit expressions use DuckDB types; source schema and accessor casts require per-overload differential proof | Projection can repeat columns; duplicate output labels are permitted only where source overload permits and ambiguous name lookup must not silently choose |
| Predicates/expressions (`filter*`, `and`, `or`, `not`, `equalTo`, `gt`, `notEqual`, `isNotNull`, `substr`) | Relational order is not a guarantee unless explicitly sorted | SQL three-valued logic and DuckDB casts; predicate helpers have operation-specific NULL semantics | Filtering preserves row multiplicity |
| Joins (`join`, `joinOnCol`, `joinRight`) | Backend-dependent unless followed by `sort` | Join type and key/expression overloads must match their individual contracts | Many-to-many matches retained; outer unmatched rows retained on requested side |
| Set/union (`union`, `unionAll`, `unionByName`, `concat`, `except`, `intersect`, `distinct`, `dropDuplicates`) | No stable row order absent sort; union preserves input side/position semantics only where specified | Positional/name resolution, missing columns, and coercions vary by overload | `union` modes preserve or remove duplicates according to overload; set ops/dedup remove according to their distinctness key |
| Aggregation (`aggSum`, `countDistinct`, `groupByCount`, `groupByMinMaxScore`, `getMaxVal`) | Group order unspecified | Aggregate-specific NULL rules; `groupByCount` two-key source semantics count first key | One result per group; duplicate source rows affect aggregates |
| Ordering/window (`sortAscending`, `sortDescending`, `orderBy`, `limit`, `coalesce`, `repartition`) | Only explicit sort defines row order; partition topology is not ordering | Null placement and partition count/argument validation are overload-specific | No deduplication unless operation explicitly states so |
| Sampling (`sample`) | Random order/identity is not stable unless source contract and seed guarantee it | Fraction/bounds, replacement Poisson semantics, and empty input are per-overload contracts | Replacement may repeat source IDs; no-replacement may not |
| Reshape/branch (`explode`, `split`, `filterInCond`, `filterNullCond`, `filterNotNullCond`) | Explode follows source array element order where supported; branches have no general order promise | Null/empty arrays, unknown predicates and regex rules require fixture-specific proof | Explode repeats parent rows; split overlap depends on predicate contract |
| Lifecycle (`cache`, `isEmpty`, `count`, `collectAsList`, `collectFirstColumn`, `show`, `showSchema`, `df`) | Collection order follows the frame only if the query carries ordering | Ownership and materialization scope are enforced; presentation methods are not semantic comparisons | Cache preserves logical multiplicities; read methods must not mutate/deduplicate |

## Required invariants

1. `z_zid` is assigned after source tagging and union, not before.
2. Match operations use by-name union; training operations use positional union only in the documented native profile (upstream compatibility is still unproven).
3. Cross-job or cross-connection frame use is rejected.
4. Any output requiring reproducible comparison must specify an explicit sort.
5. Unsupported functions and model stages fail with their registered name/class, not a generic SQL error.
