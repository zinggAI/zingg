#!/usr/bin/env sh
set -eu
BUNDLE=${1:?bundle directory required}
REQUIRE_JRE=${2:-false}
[ -f "$BUNDLE/SHA256SUMS" ] || { echo "SHA256SUMS is missing" >&2; exit 1; }
[ -f "$BUNDLE/sbom.spdx.json" ] || { echo "SBOM is missing" >&2; exit 1; }
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
python3 "$SCRIPT_DIR/verify_bundle_inventory.py" "$BUNDLE"
SOURCE_ARCHIVE=$(dirname -- "$BUNDLE")/zingg-duckdb-source.zip
if [ "$REQUIRE_JRE" = true ]; then
  if [ -f "$SOURCE_ARCHIVE" ]; then
    python3 "$SCRIPT_DIR/verify_package.py" "$BUNDLE" --require-jre --source-archive "$SOURCE_ARCHIVE"
  else
    python3 "$SCRIPT_DIR/verify_package.py" "$BUNDLE" --require-jre
  fi
else
  if [ -f "$SOURCE_ARCHIVE" ]; then
    python3 "$SCRIPT_DIR/verify_package.py" "$BUNDLE" --source-archive "$SOURCE_ARCHIVE"
  else
    python3 "$SCRIPT_DIR/verify_package.py" "$BUNDLE"
  fi
fi
python3 "$SCRIPT_DIR/verify_spdx.py" "$BUNDLE/sbom.spdx.json"
python3 "$SCRIPT_DIR/dependency_licenses.py" verify "$BUNDLE/licenses/third-party"
if [ "$REQUIRE_JRE" = true ]; then
  python3 "$SCRIPT_DIR/jdk_legal.py" verify "$BUNDLE/runtime/java"
fi
echo PACKAGE_VERIFY_SUCCESS
