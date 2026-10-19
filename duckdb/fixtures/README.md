# Development fixtures

This directory contains small, deterministic data inputs used by local development and future compatibility probes. Fixtures are data artifacts, not test sources.

The canonical sample uses stable UTF-8 CSV columns `id`, `name`, `email`, and `city`. Input readers must preserve source order, nulls, Unicode, and duplicate rows. Larger or Spark-generated reference fixtures belong in the separate reference environment and are not committed here.
