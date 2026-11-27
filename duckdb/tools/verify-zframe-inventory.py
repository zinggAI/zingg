"""Compare the pinned upstream ZFrame method surface with a reviewed inventory."""
from __future__ import annotations

import hashlib
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASELINE = ROOT / "docs" / "zframe-v07-inventory.json"
UPSTREAM_CLASS = "zingg.common.client.ZFrame"
PINNED_SHA256 = "a289903fae2ed9ff3a0a3360fed3c80232d70fce848df27e2c6a6ee8406c4b5d"


def inventory(jar: Path) -> dict:
    digest = hashlib.sha256(jar.read_bytes()).hexdigest()
    if digest != PINNED_SHA256:
        raise ValueError(f"upstream capsule SHA-256 mismatch: {digest}")
    result = subprocess.run(["javap", "-classpath", str(jar), "-p", UPSTREAM_CLASS],
                            check=True, capture_output=True, text=True)
    methods = sorted(line.strip() for line in result.stdout.splitlines()
                     if line.strip().startswith("public abstract ") and line.strip().endswith(";"))
    if not methods or len(methods) != len(set(methods)):
        raise ValueError("upstream ZFrame method inventory is empty or duplicated")
    return {"upstreamClass": UPSTREAM_CLASS, "capsuleSha256": digest,
            "methodCount": len(methods), "methods": methods}


def main() -> None:
    if len(sys.argv) not in (2, 3) or (len(sys.argv) == 3 and sys.argv[2] != "--print-json"):
        raise SystemExit("usage: verify-zframe-inventory.py <pinned-client-capsule.jar> [--print-json]")
    jar = Path(sys.argv[1]).resolve(strict=True)
    current = inventory(jar)
    if len(sys.argv) == 3:
        print(json.dumps(current, ensure_ascii=False, indent=2))
        return
    expected = json.loads(BASELINE.read_text(encoding="utf-8"))
    if current != expected:
        old = set(expected.get("methods", []))
        new = set(current["methods"])
        raise SystemExit("ZFrame inventory drift: " + json.dumps({
            "expectedCount": expected.get("methodCount"), "actualCount": current["methodCount"],
            "added": sorted(new - old), "removed": sorted(old - new)}, ensure_ascii=False))
    print(f"ZFRAME_INVENTORY_SUCCESS methods={current['methodCount']} sha256={current['capsuleSha256']}")


if __name__ == "__main__":
    main()
