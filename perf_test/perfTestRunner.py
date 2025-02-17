import subprocess
import json
import time
import os
from datetime import datetime, date
from subprocess import PIPE
from perfTestInput import phases, load_configs, ZINGG

ZINGG = ZINGG
phases_to_test = phases
load = load_configs

now = datetime.now()
current_time = now.strftime("%H:%M:%S")

reportFile = "./perf_test/perf_test_report/loadTestReport.json"

propertyFile = "./config/zingg.conf"
PERFORMANCE_THRESHOLD = 1.1  # 10% increase threshold


def load_previous_results():
    """Load previous test results if available."""
    if os.path.exists(reportFile):
        with open(reportFile, "r") as f:
            try:
                return json.load(f)
            except json.JSONDecodeError:
                return {}
    return {}


def save_results(data):
    """Save current test results to the report file."""
    with open(reportFile, "w") as f:
        json.dump(data, f, indent=4)


def run_phase(phase, conf):
    """Run a single test phase."""
    print(f"Running phase - {phase}")
    return subprocess.call(ZINGG + " %s %s %s %s %s %s" % ("--phase", phase, "--conf", conf, "--properties-file", propertyFile), shell=True)


def write_on_start():
    """Initialize test report with metadata."""
    test_data = {
        "date": str(date.today()),
        "time": current_time,
        "load_samples": list(load_configs.keys()),
        "phases": phases,
        "results": {}
    }
    save_results(test_data)


def write_results(phase_time, phase_error):
    """Save success and failure results in JSON format."""
    report_data = load_previous_results()

    for load_size, times in phase_time.items():
        if load_size not in report_data["results"]:
            report_data["results"][load_size] = {}

        for phase, duration in times.items():
            report_data["results"][load_size][phase] = {
                "time_taken_minutes": round(duration / 60, 2),
                "status": "success"
            }

    for load_size, errors in phase_error.items():
        if load_size not in report_data["results"]:
            report_data["results"][load_size] = {}

        for phase, error in errors.items():
            report_data["results"][load_size][phase] = {
                "error": str(error),
                "status": "failure"
            }

    save_results(report_data)


def compare_results(new_results):
    """Compare new results with previous ones and check for performance degradation."""
    prev_results = load_previous_results().get("results", {})
    
    for load_size, phases in new_results.items():
        if load_size in prev_results:
            for phase, new_data in phases.items():
                if phase in prev_results[load_size] and "time_taken_minutes" in prev_results[load_size][phase]:
                    prev_time = prev_results[load_size][phase]["time_taken_minutes"]
                    new_time = new_data["time_taken_minutes"]

                    if new_time > prev_time * PERFORMANCE_THRESHOLD:
                        print(f"Performance degradation detected in phase {phase} (Load: {load_size})!")
                        print(f"Previous time: {prev_time} min, New time: {new_time} min")
                        exit(1)  # Exit with failure code if performance degrades


def perform_load_test():
    """Execute the test and compare with previous results."""
    if not load_configs:
        print("No load configured to test, first set it!")
        return
    if not phases_to_test:
        print("No phase set for test, first set it!")
        return

    phase_time = {}
    phase_error = {}

    for load_size, config in load_configs.items():
        phase_time[load_size] = {}
        phase_error[load_size] = {}

        for phase in phases_to_test:
            try:
                t1 = time.time()
                result = run_phase(phase, config)
                t2 = time.time()
                phase_time[load_size][phase] = t2 - t1
            except Exception as e:
                phase_error[load_size][phase] = e

    if phase_time:
        write_results(phase_time, phase_error)
        compare_results(phase_time)


def main():
    write_on_start()
    perform_load_test()


if __name__ == "__main__":
    main()
