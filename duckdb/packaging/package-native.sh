#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
OUTPUT=${1:-dist}
CREATE_JRE=${CREATE_JRE:-false}
case "$OUTPUT" in /*) DIST=$OUTPUT ;; *) DIST="$ROOT/$OUTPUT" ;; esac
BUNDLE="$DIST/zingg-duckdb-0.1.0"
[ -x "$ROOT/mvnw" ] || { echo 'executable mvnw is required' >&2; exit 2; }
for tool in python3; do command -v "$tool" >/dev/null 2>&1 || { echo "$tool is required" >&2; exit 2; }; done
JAVA_CMD=${JAVA_BIN:-java}
JAVAC_CMD=${MAVEN_COMPILER_EXECUTABLE:-javac}
JAVA_VERSION=$($JAVA_CMD -version 2>&1 | awk -F'"' '/version / {print $2; exit}')
JAVAC_VERSION=$($JAVAC_CMD -version 2>&1 | awk '{print $2; exit}')
case "$JAVA_VERSION" in 21.*) ;; *) echo "Java 21 is required; found ${JAVA_VERSION:-unknown}" >&2; exit 2 ;; esac
case "$JAVAC_VERSION" in 21.*) ;; *) echo "javac 21 is required; found ${JAVAC_VERSION:-unknown}" >&2; exit 2 ;; esac
if [ "$CREATE_JRE" = true ]; then
  JLINK_CMD=${JLINK_BIN:-jlink}
  JLINK_VERSION=$($JLINK_CMD --version 2>&1 | head -n 1)
  case "$JLINK_VERSION" in 21.*) ;; *) echo "jlink 21 is required for CREATE_JRE=true; found ${JLINK_VERSION:-unknown}" >&2; exit 2 ;; esac
fi
cd "$ROOT"
MAVEN_EXTRA=""
if [ -n "${MAVEN_COMPILER_EXECUTABLE:-}" ]; then MAVEN_EXTRA="-Dmaven.compiler.fork=true -Dmaven.compiler.executable=$MAVEN_COMPILER_EXECUTABLE"; fi
"$ROOT/mvnw" $MAVEN_EXTRA -DskipTests clean package
rm -rf "$BUNDLE"
mkdir -p "$BUNDLE/worker" "$BUNDLE/legacy" "$BUNDLE/python" "$BUNDLE/config" "$BUNDLE/packaging" "$BUNDLE/bin"
cp runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar "$BUNDLE/worker/"
cp legacy-blocking-import-zingg07-spark35/target/*.jar legacy-classifier-import-zingg07-spark35/target/*.jar "$BUNDLE/legacy/"
python3 "$SCRIPT_DIR/dependency_licenses.py" collect "$ROOT/runtime-worker/target/runtime-dependencies" "$BUNDLE/licenses/third-party"
cp -R python/zingg_duckdb "$BUNDLE/python/"
cp packaging/invoke-legacy-import.ps1 packaging/sign-package.ps1 packaging/zingg-duckdb.sh packaging/package-native.sh packaging/verify-package.sh packaging/verify-package.ps1 packaging/verify_bundle_inventory.py packaging/verify_package.py packaging/verify_spdx.py packaging/dependency_licenses.py packaging/jdk_legal.py packaging/verify-source-archive.sh packaging/verify-source-archive.ps1 packaging/verify_source_archive.py packaging/verify-no-system-java.sh packaging/verify-rss-linux.sh packaging/verify-process-kill-linux.sh packaging/verify-process-kill-windows.ps1 packaging/rollback_package.py packaging/create-rollback-package.sh packaging/verify-rollback-package.sh "$BUNDLE/packaging/"
cp packaging/zingg-duckdb.sh "$BUNDLE/bin/"
cp packaging/zingg-duckdb.ps1 packaging/zingg-duckdb.cmd "$BUNDLE/packaging/"; cp packaging/zingg-duckdb.ps1 packaging/zingg-duckdb.cmd "$BUNDLE/bin/"
cp packaging/runtime-manifest.json packaging/compatibility-profile.json packaging/compatibility-capsule.json packaging/release-policy.json "$BUNDLE/config/"
cp README.md LICENSE NOTICE.md "$BUNDLE/"
find "$BUNDLE" -type d -name __pycache__ -prune -exec rm -rf {} +
"$SCRIPT_DIR/generate-provenance.sh" "$ROOT" "$BUNDLE/config/source-provenance.json"
"$ROOT/mvnw" -q -DoutputType=json -DoutputFile=target/dependency-tree.json dependency:tree
"$SCRIPT_DIR/generate-sbom.sh" "$ROOT/dependency-lock.json" "$BUNDLE/sbom.spdx.json" "$ROOT" "$ROOT/runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"
"$SCRIPT_DIR/create-source-archive.sh" "$ROOT" "$DIST/zingg-duckdb-source.zip"
"$SCRIPT_DIR/verify-source-archive.sh" "$DIST/zingg-duckdb-source.zip"
if [ "$CREATE_JRE" = true ]; then [ -n "${JAVA_HOME:-}" ] || { echo 'JAVA_HOME is required when CREATE_JRE=true' >&2; exit 2; }; "$SCRIPT_DIR/create-jre.sh" "$BUNDLE/runtime/java"; fi
python3 "$SCRIPT_DIR/write_bundle_manifest.py" "$BUNDLE"
"$SCRIPT_DIR/verify-package.sh" "$BUNDLE" "$CREATE_JRE"
ROLLBACK="$DIST/zingg-duckdb-0.1.0-rollback.zip"
"$SCRIPT_DIR/create-rollback-package.sh" "$BUNDLE" "$ROLLBACK"
"$SCRIPT_DIR/verify-rollback-package.sh" "$ROLLBACK"
echo "PACKAGE_NATIVE_SUCCESS $BUNDLE"
