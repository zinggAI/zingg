#!/usr/bin/env sh
set -eu

BUNDLE=${1:?bundle directory required}
[ -x "$BUNDLE/runtime/java/bin/java" ] || {
  echo "bundled Java 21 runtime is required for the RSS verifier: $BUNDLE/runtime/java" >&2
  exit 2
}
python3 - "$BUNDLE" <<'PY'
import os, pathlib, subprocess, sys

bundle = pathlib.Path(sys.argv[1])
launcher = bundle / 'bin' / 'zingg-duckdb.sh'
if not launcher.is_file():
    raise SystemExit(f'launcher is missing: {launcher}')
process = subprocess.Popen([str(launcher)], stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True)
try:
    process.stdin.write('rss\tping\t\n')
    process.stdin.flush()
    response = process.stdout.readline().strip()
    if response != 'rss\tok\tpong':
        raise SystemExit(f'unexpected ping response: {response}')
    statm = pathlib.Path(f'/proc/{process.pid}/statm')
    resident_pages = int(statm.read_text().split()[1])
    resident_bytes = resident_pages * os.sysconf('SC_PAGE_SIZE')
    if resident_bytes <= 0:
        raise SystemExit('RSS measurement is not positive')
    print(f'INDEPENDENT_RSS_BYTES={resident_bytes}')
    process.stdin.write('rss\tshutdown\t\n')
    process.stdin.flush()
    response = process.stdout.readline().strip()
    if response != 'rss\tok\tstopping':
        raise SystemExit(f'unexpected shutdown response: {response}')
    process.wait(timeout=10)
finally:
    process.stdin.close()
    if process.poll() is None:
        process.terminate()
    process.wait(timeout=10)
if process.returncode != 0:
    raise SystemExit(f'worker exited with {process.returncode}')
print('RSS_LINUX_SMOKE_SUCCESS')
PY
