import subprocess
from perfTestInput import phases, load_configs, ZINGG
import time
from datetime import date, datetime
from subprocess import PIPE
import os

#set working directory
os.chdir(os.path.dirname("../"))

ZINGG = ZINGG           
#phases to run: ftd, match
phases_to_test = phases

now = datetime.now()
current_time = now.strftime("%H:%M:%S")

#load to test: 65, 120k, 5m
load = load_configs

start_time = time.time()

reportFile = os.path.abspath(os.curdir)+"/perf_test/perf_test_report/loadTestReport"
print(reportFile + " printing report file \n")

def perf_test_small_all():
    return "small_test_running_all"

propertyFile = "./config/zingg.conf"

def run_phase(phase, conf):
    print("Running phase - " + phase)
    return subprocess.call(ZINGG + " %s %s %s %s %s %s" % ("--phase", phase, "--conf", conf, "--properties-file", propertyFile), shell=True)

def perf_test_small(phase):
    return "small_test_running"


def write_on_start():
    print(os.getcwd() + "printing working directory\n")
    f = open(reportFile, "w+")
    f.write("******************************** perf test report, " + str(date.today()) + ", " + current_time + " ********************************\n\n");
    f.write("------------ Test bed details ------------\n")
    f.write("Load samples: ")
    for load, config in load_configs.items():
        f.write(str(load) + " ")
    f.write("\n")
    f.write("Phases: ")
    for phase in phases:
        f.write(phase + " ")
    f.write("\n")
    f.write("------------------------------------------\n\n")
    f.close()

def write_on_complete():
    f = open(reportFile, "a+")
    f.write("********************************************************************************************************\n\n\n\n\n\n")




def write_success_stats(phase_time, load):
    f = open(reportFile, "a+")
    f.write("{:>50}".format("capturing for " + load) + "\n")
    f.write("PHASE {:>65}".format("TIME_TAKEN_IN_MINUTES") + "\n")
    for phase, time in phase_time.items():
        f.write(success_message(phase, round(time/60, 1)) + "\n")
    f.write("\n")
    f.close()

def write_failure_stats(phase_error):
    f = open(reportFile, "a+")
    for phase, error in phase_error.items():
        f.write(error_message(phase, error) + "\n\n")
    f.close()


def perform_load_test():
    if not load_configs:
        print("No load configured to test, first set it!")
        return
    if not phases_to_test:
        print("No phase set for test, first set it!")
        return

    for load, config in load_configs.items():
        phase_time = {}
        phase_error = {}
        for phase in phases_to_test:
            try:
                t1 = time.time()
                r = run_phase(phase, config)
                t2 = time.time()
                phase_time[phase] = t2 - t1
            except Exception as e:
                phase_error[phase] = e


        #write success data to file
        if phase_time:
            write_success_stats(phase_time, load)
        #write failure data to file
        if phase_error:
            write_failure_stats(phase_error)


def success_message(phase, time):
    return "{:<20} {:>50}".format(phase, str(time))

def error_message(phase, error):
    return phase + " failed with error " + str(error) + "\n"


def main():
    write_on_start()
    perform_load_test()
    write_on_complete()

if __name__ == "__main__":
    main()
