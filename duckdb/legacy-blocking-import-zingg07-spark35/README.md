# Legacy blocking importer

This isolated process boundary is reserved for importing Zingg v0.7.0 Spark 3.5.5 blocking-tree artifacts. It must never be loaded by the normal DuckDB worker. The implementation contract is: allowlisted classes only, bounded depth/references/array sizes, isolated temporary directory, wall-clock timeout, no network, checksum/provenance output, and conversion to `model-format`.
