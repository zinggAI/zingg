#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
ROOT=${1:-.}
OUTPUT=${2:-dist/zingg-duckdb-source.zip}
python3 "$SCRIPT_DIR/create_source_archive.py" "$ROOT" "$OUTPUT"
