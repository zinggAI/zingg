#!/usr/bin/env sh
set -eu

BUNDLE=${1:?bundle directory required}
LAUNCHER="$BUNDLE/bin/zingg-duckdb.sh"
[ -x "$LAUNCHER" ] || { echo "bundled launcher is missing or not executable: $LAUNCHER" >&2; exit 1; }
[ -x "$BUNDLE/runtime/java/bin/java" ] || { echo "bundled Java runtime is missing" >&2; exit 1; }

python3 - "$LAUNCHER" <<'PY'
import os, signal, subprocess, sys, time

launcher = sys.argv[1]
process = subprocess.Popen(
    [launcher], stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True,
    start_new_session=True,
)
try:
    process.stdin.write('kill\tping\t\n')
    process.stdin.flush()
    if process.stdout.readline().strip() != 'kill\tok\tpong':
        raise SystemExit('worker did not answer ping before termination')
    os.killpg(process.pid, signal.SIGTERM)
    try:
        process.wait(timeout=10)
    except subprocess.TimeoutExpired:
        os.killpg(process.pid, signal.SIGKILL)
        process.wait(timeout=10)
        raise SystemExit('worker process group did not terminate')
    if process.returncode == 0:
        raise SystemExit('worker unexpectedly exited cleanly after SIGTERM')
    time.sleep(0.2)
    for entry in os.listdir('/proc'):
        if not entry.isdigit():
            continue
        try:
            fields = open(f'/proc/{entry}/stat').read().split()
            # Linux stat fields: pid, comm, state, ppid, pgrp.
            if int(fields[4]) == process.pid:
                raise SystemExit(f'orphaned process remains in killed group: {entry}')
        except (FileNotFoundError, ProcessLookupError):
            continue
    print('PROCESS_KILL_LINUX_SMOKE_SUCCESS')
finally:
    if process.poll() is None:
        os.killpg(process.pid, signal.SIGKILL)
        process.wait(timeout=10)
    process.stdin.close()
PY
