#!/usr/bin/env sh
set -eu
ROOT=${1:-.}
OUTPUT=${2:-provenance.json}
ROOT=$(CDPATH= cd -- "$ROOT" && pwd)
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
python3 "$SCRIPT_DIR/generate_provenance.py" "$ROOT" "$OUTPUT"
