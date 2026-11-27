#!/usr/bin/env sh
set -eu

BUNDLE=${1:?bundle directory required}
LAUNCHER="$BUNDLE/bin/zingg-duckdb.sh"
[ -x "$LAUNCHER" ] || { echo "bundled launcher is missing or not executable: $LAUNCHER" >&2; exit 1; }
[ -x "$BUNDLE/runtime/java/bin/java" ] || { echo "bundled Java runtime is missing" >&2; exit 1; }

# Keep only the POSIX utilities needed by the launcher. Do not create symlinks:
# macOS runners commonly disallow them, and the check itself should not need
# Java, Python, or a writable system directory on PATH.
TMP_BIN=$(mktemp -d "${TMPDIR:-/tmp}/zingg-no-java.XXXXXX")
trap 'rm -rf "$TMP_BIN"' EXIT HUP INT TERM
for tool in dirname sh rm; do
  tool_path=$(command -v "$tool") || { echo "$tool is required to run this verifier" >&2; exit 2; }
  cp "$tool_path" "$TMP_BIN/$tool"
  chmod 700 "$TMP_BIN/$tool"
done

# Deliberately remove normal tool locations and JAVA_HOME. The launcher must
# resolve Java through the package-relative runtime, not the system runtime.
PATH="$TMP_BIN"; export PATH
unset JAVA_HOME JAVA_BIN
export ZINGG_NO_SYSTEM_JAVA=1
printf 'smoke\tping\t\nsmoke\tshutdown\t\n' | "$LAUNCHER"

echo NO_SYSTEM_JAVA_SMOKE_SUCCESS
