#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)

POWERSHELL=$(command -v pwsh 2>/dev/null || command -v pwsh.exe 2>/dev/null || true)
if [ -z "$POWERSHELL" ]; then
  echo "pwsh not found; using the native POSIX package assembler" >&2
  CREATE_JRE=false
  while [ "$#" -gt 0 ]; do
    case "$1" in
      -CreateJre|--CreateJre) CREATE_JRE=true ;;
      -Output|--Output) shift; OUTPUT=${1:?Output path is required} ;;
      *) echo "unsupported native package option: $1" >&2; exit 2 ;;
    esac
    shift
  done
  CREATE_JRE="$CREATE_JRE" exec "$SCRIPT_DIR/package-native.sh" "${OUTPUT:-dist}"
fi

cd "$ROOT"
SCRIPT_FILE="$SCRIPT_DIR/package.ps1"
case "$POWERSHELL" in
  *.exe)
    if command -v wslpath >/dev/null 2>&1; then
      SCRIPT_FILE=$(wslpath -w "$SCRIPT_FILE")
    fi
    ;;
esac
exec "$POWERSHELL" -NoLogo -NoProfile -NonInteractive -File "$SCRIPT_FILE" "$@"
