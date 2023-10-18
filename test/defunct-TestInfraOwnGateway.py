# Refer note.txt to run this file.

import unittest
import sys
from unittest.mock import MagicMock

from zingg import *
from zingg.client import *
from zingg.pipes import *

import subprocess
from py4j.java_gateway import JavaGateway
from py4j.protocol import Py4JNetworkError
from time import sleep
from multiprocessing import Process
from py4j.java_gateway import JavaGateway, GatewayParameters

PY4J_JAVA_PATH='.:../thirdParty/lib//py4j0.10.9.jar:/home/r_gnanaprakash/zingg/assembly/target/zingg-0.4.0-SNAPSHOT.jar:/opt/spark-3.2.4-bin-hadoop3.2/jars/jackson-databind-2.12.3.jar:/opt/spark-3.2.4-bin-hadoop3.2/jars/jackson-core-2.12.3.jar:/opt/spark-3.2.4-bin-hadoop3.2/jars/jackson-annotations-2.12.3.jar'
print(PY4J_JAVA_PATH)
def compileGatewayEntry():
    subprocess.call([
        "javac", "-cp", PY4J_JAVA_PATH, #"-source", "1.8", "-target", "1.8",
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
    @classmethod
    def setUpClass(cls):
        compileGatewayEntry()
        cls.p = start_example_app_process()
        cls.gateway = JavaGateway(
            gateway_parameters=GatewayParameters(auto_convert=True))

    @classmethod
    def tearDownClass(cls):
        safe_shutdown(cls)
        cls.p.join()
        sleep(2)
    
    def setUp(self):
        self.arguments = self.gateway.jvm.zingg.common.client.Arguments()
   
    
    def test_jvm_access(self):
        print("Accessing the JVM...")
        try:
            current_time = self.gateway.jvm.System.currentTimeMillis()
            print("Current time from JVM:", current_time)
            x = self.gateway.jvm.zingg.common.client.Arguments()
            print(x)
        except Py4JNetworkError:
            print("Failed to access the JVM.")

    # def test_setFieldDefinition(self):
    #     field_def1 = self.gateway.jvm.zingg.common.client.FieldDefinition("name", "string")
    #     field_def2 = self.gateway.jvm.zingg.common.client.FieldDefinition("city", "string")

    #     self.arguments.setFieldDefinition([field_def1, field_def2])

    #     expected_java_field_def = [field_def1.getFieldDefinition(), field_def2.getFieldDefinition()]
    #     self.arguments.args.setFieldDefinition.assert_called_once_with(expected_java_field_def)
        
    # def test_setFieldDefinition(self):
    #     field_def1 = self.gateway.jvm.zingg.common.client.FieldDefinition("name")
    #     field_def1.setDataType("string")

    #     field_def2 = self.gateway.jvm.zingg.common.client.FieldDefinition("city")
    #     field_def2.setDataType("string")

    #     self.arguments.setFieldDefinition([field_def1, field_def2])

    #     expected_java_field_def = [field_def1.getFieldDefinition(), field_def2.getFieldDefinition()]
    #     self.arguments.args.setFieldDefinition.assert_called_once_with(expected_java_field_def)


if __name__ == '__main__':
    unittest.main(argv=sys.argv[:1])
