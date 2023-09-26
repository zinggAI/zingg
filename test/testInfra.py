import unittest
import sys

#from zingg import *
#from zingg.client import *
#from zingg.pipes import *

import subprocess
from py4j.java_gateway import JavaGateway
from py4j.protocol import Py4JNetworkError
from time import sleep
from multiprocessing import Process
from py4j.java_gateway import JavaGateway, GatewayParameters

PY4J_JAVA_PATH='.:../thirdParty/lib//py4j0.10.9.jar:$ZINGG_HOME/common/client/target/zingg-common-client-0.4.0-SNAPSHOT.jar'
def compileGatewayEntry():
    subprocess.call([
        "javac", "-cp", PY4J_JAVA_PATH,
        "TestPy4JGateway.java"])
    
def startGatewayEntry():
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "TestPy4JGateway"])
    
def start_example_app_process():
    # XXX DO NOT FORGET TO KILL THE PROCESS IF THE TEST DOES NOT SUCCEED
    p = Process(target=startGatewayEntry)
    p.start()
    sleep(2)
    return p

def check_connection(gateway_parameters=None):
    test_gateway = JavaGateway(gateway_parameters=gateway_parameters)
    try:
        # Call a dummy method just to make sure we can connect to the JVM
        test_gateway.jvm.System.currentTimeMillis()
    except Py4JNetworkError:
        # We could not connect. Let"s wait a long time.
        # If it fails after that, there is a bug with our code!
        sleep(2)
    finally:
        test_gateway.close()

def safe_shutdown(instance):
    if hasattr(instance, 'gateway'):
        try:
            instance.gateway.shutdown()
        except Exception:
            print("exception")
        


class MyJavaIntegrationTest(unittest.TestCase):
    def setUp(self):
        compileGatewayEntry()
        self.p = start_example_app_process()
        self.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_convert=True))

    def tearDown(self):
        safe_shutdown(self)
        self.p.join()
        sleep(2)
   
    
    def test_jvm_access(self):
        print("Accessing the JVM...")
        try:
            current_time = self.gateway.jvm.System.currentTimeMillis()
            print("Current time from JVM:", current_time)
        except Py4JNetworkError:
            print("Failed to access the JVM.")


if __name__ == '__main__':
    unittest.main(argv=sys.argv[:1])
