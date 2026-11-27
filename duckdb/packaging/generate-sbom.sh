#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
LOCK=${1:-dependency-lock.json}
OUTPUT=${2:-sbom.spdx.json}
TREE_ROOT=${3:-$(CDPATH= cd -- "$(dirname -- "$LOCK")" && pwd -P)}
SHADED_JAR=${4:-}
if [ -n "$SHADED_JAR" ]; then
  python3 "$SCRIPT_DIR/generate_sbom.py" "$LOCK" "$OUTPUT" "$TREE_ROOT" "$SHADED_JAR"
else
  python3 "$SCRIPT_DIR/generate_sbom.py" "$LOCK" "$OUTPUT" "$TREE_ROOT"
fi
