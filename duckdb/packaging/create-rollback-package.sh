#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
BUNDLE=${1:?bundle directory required}
OUTPUT=${2:?rollback archive output required}
python3 "$SCRIPT_DIR/rollback_package.py" create "$BUNDLE" "$OUTPUT"
