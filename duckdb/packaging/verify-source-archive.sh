#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
ARCHIVE=${1:-dist/zingg-duckdb-source.zip}
[ -f "$ARCHIVE" ] || { echo "source archive is missing: $ARCHIVE" >&2; exit 2; }
python3 "$SCRIPT_DIR/verify_source_archive.py" "$ARCHIVE"
