import unittest
import sys

#from zingg import *
#from zingg.client import *
#from zingg.pipes import *

import subprocess
from py4j.java_gateway import JavaGateway
from py4j.protocol import Py4JNetworkError
from time import sleep

PY4J_JAVA_PATH='.:../thirdParty/lib//py4j0.10.9.jar:$ZINGG_HOME/common/client/target/zingg-common-client-0.4.0-SNAPSHOT.jar'
def start_example_server():
    subprocess.call([
        "javac", "-cp", PY4J_JAVA_PATH,
        "TestPy4JGateway.java"])
    subprocess.call([
        "java", "-Xmx512m", "-cp", PY4J_JAVA_PATH,
        "TestPy4JGateway"])

def check_connection(gateway):
    try:
        gateway.jvm.System.currentTimeMillis()
    except Py4JNetworkError:
        sleep(2)
        
class MyJavaClass:
    def addition(self, a, b):
        return a + b

class MyJavaIntegrationTest(unittest.TestCase):
    def setUp(self):
        start_example_server()
        self.gateway = JavaGateway()
        check_connection(self.gateway)

    def tearDown(self):
        self.gateway.close()
    
    def test_jvm_access(self):
        print("Accessing the JVM...")
        try:
            current_time = self.gateway.jvm.System.currentTimeMillis()
            print("Current time from JVM:", current_time)
        except Py4JNetworkError:
            print("Failed to access the JVM.")


if __name__ == '__main__':
    unittest.main(argv=sys.argv[:1])
