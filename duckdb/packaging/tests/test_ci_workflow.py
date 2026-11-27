"""Guard high-impact cross-platform workflow wiring against shell mistakes."""
from __future__ import annotations

import re
import unittest
from pathlib import Path


WORKFLOW = Path(__file__).resolve().parents[2] / ".github/workflows/build.yml"
SECURITY_WORKFLOW = Path(__file__).resolve().parents[2] / ".github/workflows/security.yml"


class BuildWorkflowTest(unittest.TestCase):
    def test_native_validation_matrix_covers_promoted_profile_and_secondstring_oracle(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        start = source.index("  native-validation:")
        end = source.index("\n  windows-package:", start)
        job = source[start:end]
        for target in ("linux-amd64", "linux-arm64", "macos-x86_64", "macos-arm64", "windows-x86_64"):
            self.assertIn(f"target: {target}", job)
        self.assertEqual(0, job.count("1.5.5.1"))
        self.assertGreaterEqual(job.count("1.5.6.0"), 2)
        self.assertIn("21f54e9b4693d49d6f26b2851853b140951a7502", job)
        self.assertIn("5267ae94cb2feff6b75bf112c1d22976b7835e04bad14b369cbd5525f20b520", job)
        self.assertIn("SecondStringJaccardDifferentialTest", job)

    def test_benchmark_ci_separates_clean_timings_from_resource_sampling(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        start = source.index("      - name: Run benchmark smoke")
        end = source.find("      - name:", start + 1)
        step = source[start:end if end >= 0 else None]
        self.assertIn("BenchmarkMain --arrow-only --size 1000 --repetitions 3", step)
        self.assertIn("BenchmarkMain --arrow-only --resource-sampling --size 1000 --repetitions 3", step)
        self.assertIn("benchmark-arrow-linux.json", step)
        self.assertIn("benchmark-arrow-resources-linux.json", step)
        self.assertIn("verify-benchmark-report.py benchmark-arrow-linux.json --timing-only", step)
        self.assertIn("verify-benchmark-report.py benchmark-arrow-resources-linux.json --resource-sampling", step)
        self.assertIn("resourceSamplingEnabled", step)
        self.assertIn("MaxObservedRssBytes", step)
        self.assertIn("MaxObservedSpillBytes", step)
        self.assertIn("ResourceSampleCounts", step)

    def test_zframe_inventory_step_has_no_orphaned_heredoc_terminator(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        start = source.index("      - name: Verify pinned upstream ZFrame API inventory")
        end = source.find("      - name:", start + 1)
        step = source[start:end if end >= 0 else None]
        self.assertNotRegex(step, re.compile(r"(?m)^\s+PY\s*$"))
        self.assertIn("python3 tools/verify-zframe-inventory.py", step)
        self.assertIn("python3 tools/generate-zframe-adapter-matrix.py --check", step)

    def test_pinned_zframe_reference_matrix_runs_exact_overload_resolver_on_windows_and_linux(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        self.assertEqual(2, source.count("ZFrameCoreDifferentialTest,ZFrameOverloadResolutionTest"))
        self.assertIn("name: Differential ZFrame semantics and resolve exact overloads", source)
        self.assertIn("name: Differential group-count semantics against Spark 3.5.5", source)

    def test_release_provenance_is_not_added_after_bundle_checksum_inventory(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("./packaging/generate-provenance.sh . dist/zingg-duckdb-provenance.json", source)
        self.assertIn("path: dist/zingg-duckdb-provenance.json", source)
        self.assertNotIn("-Output dist/zingg-duckdb-0.1.0/provenance.json", source)

    def test_linux_validation_uses_native_posix_package_and_verifier_entrypoints(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        start = source.index("  validation:")
        end = source.find("\n  python-matrix:", start)
        job = source[start:end]
        self.assertIn("runs-on: ubuntu-24.04", job)
        self.assertIn("shell: bash\n        run: CREATE_JRE=true ./packaging/package-native.sh dist", job)
        self.assertIn("./packaging/verify-source-archive.sh dist/zingg-duckdb-source.zip", job)
        self.assertIn("./packaging/verify-package.sh dist/zingg-duckdb-0.1.0 true", job)
        self.assertIn("./packaging/generate-provenance.sh . dist/zingg-duckdb-provenance.json", job)
        self.assertNotIn("./packaging/package.sh", job)
        self.assertNotIn("./packaging/package.ps1", job)
        self.assertNotIn("./packaging/verify-package.ps1", job)

    def test_unix_package_matrix_forces_native_posix_assembler(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        start = source.index("  offline-packages:")
        end = source.find("\n  duckdb-canary:", start)
        job = source[start:end]
        unix_step = job[job.index("      - name: Build offline package (Unix)"):job.index("      - name: Build offline package (Windows)")]
        windows_step = job[job.index("      - name: Build offline package (Windows)"):job.index("      - name: Upload offline package")]
        self.assertIn("CREATE_JRE=true ./packaging/package-native.sh dist", unix_step)
        self.assertNotIn("./packaging/package.sh", unix_step)
        self.assertIn("PYTHONPATH=python PYTHONWARNINGS=error python -m unittest discover", unix_step)
        self.assertIn("test_worker_process_cancellation.py", unix_step)
        self.assertIn("python -m unittest discover -s python/tests", windows_step)
        self.assertIn("test_worker_process_cancellation.py", windows_step)
        self.assertLess(unix_step.index("CREATE_JRE=true ./packaging/package-native.sh dist"),
                        unix_step.index("test_worker_process_cancellation.py"))
        self.assertLess(windows_step.index("./packaging/package.ps1 -Output dist"),
                        windows_step.index("test_worker_process_cancellation.py"))

    def test_linux_package_is_smoke_tested_in_clean_offline_docker_container(self) -> None:
        source = WORKFLOW.read_text(encoding="utf-8")
        start = source.index("      - name: Build offline package (Unix)")
        end = source.index("      - name: Build offline package (Windows)", start)
        step = source[start:end]
        self.assertIn('if [ "$RUNNER_OS" = Linux ]; then', step)
        self.assertIn("sh packaging/verify-docker-package.sh dist/zingg-duckdb-0.1.0", step)
        self.assertLess(step.index("CREATE_JRE=true ./packaging/package-native.sh dist"),
                        step.index("verify-docker-package.sh"))
        docker_script = (WORKFLOW.parents[2] / "packaging/verify-docker-package.sh").read_text(encoding="utf-8")
        self.assertIn("--network none", docker_script)
        self.assertIn("--read-only", docker_script)
        self.assertIn("--tmpfs /tmp:rw,nosuid,nodev,exec,size=256m", docker_script)
        self.assertIn("--tmpfs /zingg-spill:rw,nosuid,nodev,size=16m", docker_script)
        self.assertIn("--temp-directory /zingg-spill --max-temp-bytes 1048576", docker_script)
        self.assertIn("--mount \"type=bind,source=$TEST_CLASSES_SOURCE,target=/opt/zingg-test-classes,readonly\"", docker_script)
        self.assertIn("io.zingg.duckdb.worker.SpillQuotaProbe /zingg-spill", docker_script)
        self.assertIn("io.zingg.duckdb.worker.OfflineNetworkProbe /tmp/zingg-offline-network-probe", docker_script)
        self.assertIn("OFFLINE_NETWORK_AND_DUCKDB_URL_REJECTED", docker_script)
        self.assertIn("MODEL_ARTIFACT_BUDGET_REJECTED_AND_CLEANED", docker_script)
        self.assertIn("CONCURRENT_SPILL_QUOTA_REJECTED_AND_CLEANED", docker_script)
        self.assertIn("ubuntu:24.04", docker_script)
        self.assertIn("test ! -e /usr/bin/java", docker_script)
        self.assertIn('"$DOCKER" image inspect', docker_script)
        self.assertIn("DOCKER_CLI", docker_script)
        self.assertIn("python:3.12-slim", docker_script)
        self.assertIn("target=/opt/zingg-test-classes,readonly", docker_script)
        self.assertIn("ZINGG_TEST_JAVA=/opt/zingg/runtime/java/bin/java", docker_script)
        self.assertIn("ZINGG_TEST_WORKER_JAR=/opt/zingg/worker/runtime-worker-0.1.0-SNAPSHOT.jar", docker_script)
        self.assertIn("test_worker_process_cancellation.py", docker_script)
        self.assertIn("ZINGG_TEST_WORKER_JAR=", step)
        self.assertIn("runtime/java/bin/java", step)

    def test_mandatory_osv_scan_is_pinned_and_runs_on_changes_and_schedule(self) -> None:
        source = SECURITY_WORKFLOW.read_text(encoding="utf-8")
        package_script = (SECURITY_WORKFLOW.parents[2] / "packaging/package-native.sh").read_text(encoding="utf-8")
        self.assertIn("  push:", source)
        self.assertIn("  pull_request:", source)
        self.assertIn("  schedule:", source)
        self.assertIn("cron: '23 9 * * 1'", source)
        self.assertIn("  package-security-sbom:", source)
        self.assertIn("runs-on: ubuntu-24.04", source)
        self.assertIn("CREATE_JRE=true ./packaging/package-native.sh dist", source)
        self.assertIn("name: zingg-duckdb-security-sbom", source)
        self.assertIn("path: dist/zingg-duckdb-0.1.0/sbom.spdx.json", source)
        self.assertLess(source.index("CREATE_JRE=true ./packaging/package-native.sh dist"),
                        source.index("- name: Upload package SBOM"))
        self.assertLess(package_script.index('write_bundle_manifest.py'),
                        package_script.index('"$SCRIPT_DIR/verify-package.sh" "$BUNDLE" "$CREATE_JRE"'))
        self.assertIn(
            "uses: google/osv-scanner-action/.github/workflows/osv-scanner-reusable.yml@a345acffa64b0eaede81a3d9aae6141214d9c8fc # v2.6.0",
            source,
        )
        self.assertIn("    needs: package-security-sbom", source)
        self.assertIn("download-artifact: zingg-duckdb-security-sbom", source)
        self.assertIn("-L ./sbom.spdx.json", source)
        self.assertIn("fail-on-vuln: true", source)
        self.assertIn("upload-sarif: false", source)
        self.assertIsNone(re.search(r"(?m)^\s+if\s*:", source))


if __name__ == "__main__":
    unittest.main()
