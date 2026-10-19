#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
ARCHIVE=${1:?rollback archive required}
python3 "$SCRIPT_DIR/rollback_package.py" verify "$ARCHIVE"
