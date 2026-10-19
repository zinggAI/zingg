#!/usr/bin/env sh
set -eu

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <verified-linux-bundle>" >&2
  exit 2
fi
DOCKER=${DOCKER_CLI:-docker}
if ! command -v "$DOCKER" >/dev/null 2>&1; then
  echo "Docker CLI is required for the clean-container package smoke test" >&2
  exit 2
fi

BUNDLE=$(CDPATH= cd -- "$1" && pwd -P)
case "$DOCKER" in
  *.exe) BUNDLE_SOURCE=$(wslpath -w "$BUNDLE") ;;
  *) BUNDLE_SOURCE=$BUNDLE ;;
esac
IMAGE=${DOCKER_TEST_IMAGE:-ubuntu:24.04}
PYTHON_IMAGE=${DOCKER_TEST_PYTHON_IMAGE:-python:3.12-slim}
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd -P)
TEST_CLASSES="$REPO_ROOT/runtime-worker/target/test-classes"
if [ ! -f "$TEST_CLASSES/io/zingg/duckdb/worker/ArrowCancellationFixture.class" ] \
    || [ ! -f "$TEST_CLASSES/io/zingg/duckdb/worker/SpillQuotaProbe.class" ] \
    || [ ! -f "$TEST_CLASSES/io/zingg/duckdb/worker/OfflineNetworkProbe.class" ]; then
  echo "Docker package validation fixtures are missing; compile test classes before Docker validation" >&2
  exit 2
fi
case "$DOCKER" in
  *.exe)
    TEST_CLASSES_SOURCE=$(wslpath -w "$TEST_CLASSES")
    PYTHON_SOURCE=$(wslpath -w "$REPO_ROOT/python")
    ;;
  *)
    TEST_CLASSES_SOURCE=$TEST_CLASSES
    PYTHON_SOURCE=$REPO_ROOT/python
    ;;
esac

"$DOCKER" run --rm \
  --network none \
  --read-only \
  --tmpfs /tmp:rw,nosuid,nodev,exec,size=256m \
  --tmpfs /zingg-spill:rw,nosuid,nodev,size=16m \
  --mount "type=bind,source=$BUNDLE_SOURCE,target=/opt/zingg,readonly" \
  --mount "type=bind,source=$TEST_CLASSES_SOURCE,target=/opt/zingg-test-classes,readonly" \
  "$IMAGE" /bin/sh -eu -c '
    test -x /opt/zingg/bin/zingg-duckdb.sh
    test -x /opt/zingg/runtime/java/bin/java
    test ! -e /usr/bin/java
    actual=$(printf "docker-smoke\tping\t\ndocker-smoke\tshutdown\t\n" |
      env PATH=/usr/bin:/bin /opt/zingg/bin/zingg-duckdb.sh)
    expected=$(printf "docker-smoke\tok\tpong\ndocker-smoke\tok\tstopping")
    if [ "$actual" != "$expected" ]; then
      printf "unexpected packaged worker response:\n%s\n" "$actual" >&2
      exit 1
    fi
    network_probe=$(/opt/zingg/runtime/java/bin/java \
      --add-opens=java.base/java.nio=ALL-UNNAMED \
      --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
      -cp /opt/zingg/worker/runtime-worker-0.1.0-SNAPSHOT.jar:/opt/zingg-test-classes \
      io.zingg.duckdb.worker.OfflineNetworkProbe /tmp/zingg-offline-network-probe)
    case "$network_probe" in
      *OFFLINE_NETWORK_AND_DUCKDB_URL_REJECTED*) printf "%s\n" "$network_probe" ;;
      *) printf "offline network probe failed:\n%s\n" "$network_probe" >&2; exit 1 ;;
    esac
    spill_status=$(printf "spill-status\tstatus\t\nspill-stop\tshutdown\t\n" |
      env PATH=/usr/bin:/bin /opt/zingg/bin/zingg-duckdb.sh \
        --temp-directory /zingg-spill --max-temp-bytes 1048576)
    case "$spill_status" in
      *"temp_directory=/zingg-spill;"*) ;;
      *) printf "worker did not use the bounded spill mount:\n%s\n" "$spill_status" >&2; exit 1 ;;
    esac
    quota_probe=$(/opt/zingg/runtime/java/bin/java \
      --add-opens=java.base/java.nio=ALL-UNNAMED \
      --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
      -cp /opt/zingg/worker/runtime-worker-0.1.0-SNAPSHOT.jar:/opt/zingg-test-classes \
      io.zingg.duckdb.worker.SpillQuotaProbe /zingg-spill)
    case "$quota_probe" in
      *SPILL_TMPFS_QUOTA_REJECTED_AND_CLEANED*MODEL_ARTIFACT_BUDGET_REJECTED_AND_CLEANED*CONCURRENT_SPILL_QUOTA_REJECTED_AND_CLEANED*) printf "%s\n" "$quota_probe" ;;
      *) printf "spill quota probe failed:\n%s\n" "$quota_probe" >&2; exit 1 ;;
    esac
  '

"$DOCKER" image inspect "$IMAGE" --format='DOCKER_TEST_IMAGE_ID={{.Id}} REPO_DIGESTS={{json .RepoDigests}}'

# Run the Python facade integration inside a separate Python-enabled, networkless
# container, but use the bundle's own JVM and shaded worker JAR. This catches
# package/JNI/protocol regressions beyond the launcher ping smoke above.
"$DOCKER" run --rm \
  --network none \
  --read-only \
  --tmpfs /tmp:rw,nosuid,nodev,exec,size=512m \
  --mount "type=bind,source=$BUNDLE_SOURCE,target=/opt/zingg,readonly" \
  --mount "type=bind,source=$TEST_CLASSES_SOURCE,target=/opt/zingg-test-classes,readonly" \
  --mount "type=bind,source=$PYTHON_SOURCE,target=/opt/zingg-python,readonly" \
  "$PYTHON_IMAGE" /bin/sh -eu -c '
    test ! -e /usr/bin/java
    export PYTHONPATH=/opt/zingg-python
    export ZINGG_TEST_JAVA=/opt/zingg/runtime/java/bin/java
    export ZINGG_TEST_WORKER_JAR=/opt/zingg/worker/runtime-worker-0.1.0-SNAPSHOT.jar
    export ZINGG_TEST_CLASSES=/opt/zingg-test-classes
    python -m unittest discover -s /opt/zingg-python/tests -p test_worker_process_cancellation.py -v
  '

"$DOCKER" image inspect "$PYTHON_IMAGE" --format='DOCKER_PYTHON_IMAGE_ID={{.Id}} REPO_DIGESTS={{json .RepoDigests}}'
echo "DOCKER_PACKAGE_SMOKE_SUCCESS ubuntu_image=$IMAGE python_image=$PYTHON_IMAGE bundle=$BUNDLE"
