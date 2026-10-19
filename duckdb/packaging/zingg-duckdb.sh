#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
JAVA="$ROOT/runtime/java/bin/java"
JAR="$ROOT/worker/runtime-worker-0.1.0-SNAPSHOT.jar"
if [ ! -x "$JAVA" ]; then
  echo "Bundled Java 21 runtime is missing or not executable: $JAVA" >&2
  echo "Install the official bundle with its runtime directory or use a supported package." >&2
  exit 2
fi
if [ ! -f "$JAR" ]; then
  echo "Runtime worker is missing: $JAR" >&2
  exit 2
fi
exec "$JAVA" --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -jar "$JAR" "$@"
