#!/usr/bin/env sh
set -eu

JAVA_HOME=${JAVA_HOME:-}
OUTPUT=${1:-dist/zingg-duckdb-0.1.0/runtime/java}

if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME must point to a Java 21 JDK" >&2
  exit 2
fi

JAVA_HOME=$(CDPATH= cd -- "$JAVA_HOME" && pwd)
JAVA=${JAVA_BIN:-$JAVA_HOME/bin/java}
JLINK=${JLINK_BIN:-$JAVA_HOME/bin/jlink}

if [ ! -x "$JAVA" ] || [ ! -x "$JLINK" ]; then
  echo "Java 21 JDK tools were not found under $JAVA_HOME" >&2
  exit 2
fi

VERSION=$($JAVA -version 2>&1 | awk -F'"' '/version "/ { print $2; exit }')
case "$VERSION" in
  21.*) ;;
  *) echo "Java 21 is required; found ${VERSION:-unknown}" >&2; exit 2 ;;
esac

case "$OUTPUT" in
  /*) DEST=$OUTPUT ;;
  *) DEST=$(pwd)/$OUTPUT ;;
esac

rm -rf "$DEST"
mkdir -p "$(dirname "$DEST")"
"$JLINK" \
  --add-modules java.base,java.logging,java.management,java.naming,java.sql,java.xml,jdk.crypto.ec,jdk.unsupported \
  --strip-debug --no-header-files --no-man-pages --compress=2 --output "$DEST"

"$JAVA" -version >"$DEST/JAVA-VERSION.txt" 2>&1
python3 "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)/jdk_legal.py" collect "$JAVA_HOME" "$DEST" "$(basename "$JAVA")"
echo "Created $DEST"
