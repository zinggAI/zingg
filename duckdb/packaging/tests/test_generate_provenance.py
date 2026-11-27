"""Cross-platform source provenance generation tests."""
from __future__ import annotations

import hashlib
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "generate_provenance.py"
SPEC = importlib.util.spec_from_file_location("generate_provenance", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
GENERATOR = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(GENERATOR)


def make_source(root: Path) -> Path:
    for relative, data in {
        "pom.xml": b"root pom",
        "dependency-lock.json": b"root locks",
        "README.md": b"readme",
        "NOTICE.md": b"notice",
        "LICENSE": b"license",
        "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar": b"worker artifact",
        "engine-api/pom.xml": b"module pom",
        "engine-api/dependency-lock.json": b"module lock",
        "no-lock/pom.xml": b"module without lock",
    }.items():
        path = root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    return root


class GenerateProvenanceTest(unittest.TestCase):
    def test_includes_root_and_module_sources_in_sorted_deterministic_inventory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = make_source(Path(directory) / "source")
            first = Path(directory) / "first.json"
            second = Path(directory) / "second.json"
            GENERATOR.generate(root, first)
            GENERATOR.generate(root, second)
            self.assertEqual(first.read_bytes(), second.read_bytes())
            self.assertNotIn(b"\r", first.read_bytes(), "canonical JSON output must use LF on every OS")
            document = json.loads(first.read_text(encoding="utf-8"))
            paths = [entry["path"] for entry in document["files"]]
            self.assertEqual(sorted(paths), paths)
            self.assertIn("engine-api/pom.xml", paths)
            self.assertIn("engine-api/dependency-lock.json", paths)
            self.assertIn("no-lock/pom.xml", paths)
            self.assertNotIn("no-lock/dependency-lock.json", paths)
            worker = next(entry for entry in document["files"] if entry["path"].endswith("runtime-worker-0.1.0-SNAPSHOT.jar"))
            self.assertEqual(hashlib.sha256(b"worker artifact").hexdigest(), worker["sha256"])
            self.assertEqual(worker["sha256"], document["workerArtifactSha256"])

    def test_requires_worker_artifact(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = make_source(Path(directory) / "source")
            (root / "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar").unlink()
            with self.assertRaisesRegex(ValueError, "worker artifact is missing"):
                GENERATOR.generate(root, Path(directory) / "provenance.json")


if __name__ == "__main__":
    unittest.main()
