#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
NATIVE_IMAGE=${1:-native-image}
OUTPUT=${2:-dist-native}
BINARY_NAME=${3:-zingg-duckdb-worker}
JAR=${RUNTIME_WORKER_JAR:-$ROOT/runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar}
CONFIG_SOURCE="$ROOT/runtime-worker/src/main/resources/META-INF/native-image/io.zingg.duckdb/runtime-worker"

case "$BINARY_NAME" in
  ''|.|..|*[!A-Za-z0-9._-]*) echo "binary name must contain only letters, digits, dot, underscore, or hyphen" >&2; exit 2 ;;
esac
[ -f "$JAR" ] || { echo "runtime worker jar is missing: $JAR; run Maven package first" >&2; exit 2; }
[ -d "$CONFIG_SOURCE" ] || { echo "Native Image configuration directory is missing: $CONFIG_SOURCE" >&2; exit 2; }
command -v "$NATIVE_IMAGE" >/dev/null 2>&1 || {
  echo "GraalVM Native Image prerequisite is missing: '$NATIVE_IMAGE' was not found; install GraalVM for JDK 22+ or pass its executable path" >&2
  exit 2
}
command -v python3 >/dev/null 2>&1 || { echo "python3 is required to prepare Native Image metadata" >&2; exit 2; }
JAVA_COMMAND=${JAVA_HOME:-}/bin/java
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_COMMAND" ]; then JAVA_COMMAND=java; fi
JAVA_VERSION=$({ "$JAVA_COMMAND" -version 2>&1 || true; } | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -n 1)
case "$JAVA_VERSION" in
  ''|*[!0-9]*) echo "JDK 22+ is required for Native Image; unable to determine version from '$JAVA_COMMAND'" >&2; exit 2 ;;
esac
if [ "$JAVA_VERSION" -lt 22 ]; then
  echo "JDK 22+ is required for Native Image; detected JDK $JAVA_VERSION from '$JAVA_COMMAND'" >&2
  exit 2
fi

mkdir -p "$OUTPUT"
case "$OUTPUT" in /*) OUTPUT_DIR=$OUTPUT ;; *) OUTPUT_DIR="$(pwd)/$OUTPUT" ;; esac
TARGET="$OUTPUT_DIR/$BINARY_NAME"
TEMP_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/zingg-native-image-config.XXXXXX")
case "$TEMP_ROOT" in "${TMPDIR:-/tmp}"/zingg-native-image-config.*) ;; *) echo "unsafe temporary metadata directory" >&2; exit 2 ;; esac
trap 'rm -rf -- "$TEMP_ROOT"' EXIT HUP INT TERM
CONFIG="$TEMP_ROOT/config"
python3 "$SCRIPT_DIR/prepare_native_image_config.py" "$CONFIG_SOURCE" "$CONFIG"
"$NATIVE_IMAGE" \
  --no-fallback \
  --enable-url-protocols=http,https \
  --initialize-at-run-time=org.duckdb,java.sql.SQLException,java.sql.DriverManager \
  -H:+UnlockExperimentalVMOptions \
  "-H:ConfigurationFileDirectories=$CONFIG" \
  -jar "$JAR" "$TARGET"
[ -f "$TARGET" ] || { echo "native worker was not produced: $TARGET" >&2; exit 1; }
python3 - "$TARGET" "$OUTPUT_DIR/$BINARY_NAME.sha256.json" <<'PY'
import hashlib, json, pathlib, sys
artifact = pathlib.Path(sys.argv[1])
digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
pathlib.Path(sys.argv[2]).write_text(json.dumps({"algorithm": "SHA-256", "path": artifact.name, "sha256": digest}, indent=2) + "\n", encoding="utf-8")
PY
printf 'NATIVE_IMAGE_BUILD_SUCCESS %s\n' "$TARGET"
