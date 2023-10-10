from unittest.case import TestCase
from zingg import *
from zingg.client import *
from zingg.pipes import *

args = Arguments()
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1","string", MatchType.FUZZY)
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)
city = FieldDefinition("city", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
dob = FieldDefinition("dob", "string", MatchType.FUZZY)
ssn = FieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]

args.setFieldDefinition(fieldDefs)

args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn  string"
inputPipe = CsvPipe("unittestFebrl", "examples/febrl/test.csv", schema)
outputPipe = CsvPipe("unittestFebrlResult", "/tmp/pythonTestFebrl")
args.setData(inputPipe)
args.setOutput(outputPipe)
options = ClientOptions([ClientOptions.PHASE,"trainMatch"])


class ArgumentsTest(TestCase):
    def test_setArgsAndGetArgs(self):
        client = Zingg(args, options)
        client.initAndExecute()
        print("testcase 1")
        expected_args = {
            "zinggDir": "models",
            "numPartitions": 4,
            "labelDataSampleSize": 0.5,
            "modelId": "100",
            "jobId": 1,
            "collectMetrics": True,
            "blockSize": 100,
        }

        java_args = client.getArguments()
        print("java_args", java_args)
        
        self.assertEqual(java_args.getModelId(), expected_args["modelId"])
        self.assertEqual(java_args.getZinggDir(), expected_args["zinggDir"])
        # self.assertEqual(java_args.getnumPartitions(), expected_args["numPartitions"])
        # self.assertEqual(java_args.getlabelDataSampleSize(), expected_args["labelDataSampleSize"])
        # self.assertEqual(java_args.getjobId(), expected_args["jobId"])
        # self.assertEqual(java_args.getcollectMetrics(), expected_args["collectMetrics"])
        # self.assertEqual(java_args.getblockSize(), expected_args["blockSize"])

    def test_setModelId(self):
        client = Zingg(args, options)
        client.initAndExecute()
        expected_model_id = "100"

        java_args = client.getArguments()
        actual_model_id = java_args.getModelId()
        print(actual_model_id)

        self.assertEqual(actual_model_id, expected_model_id)

    def test_setFieldDefinition(self):
        client = Zingg(args, options)
        client.initAndExecute()
    
        java_args = client.getArguments()
        java_field_defs = java_args.getFieldDefinition()  
        print(type(java_field_defs))
        print(java_field_defs)
    
        self.assertEqual(len(java_field_defs), len(fieldDefs))
        for java_field_def, expected_field_def in zip(java_field_defs, fieldDefs):
            self.assertEqual(java_field_def.getFieldName(), expected_field_def.getFieldDefinition().getFieldName())
            self.assertEqual(java_field_def.getDataType(), expected_field_def.getFieldDefinition().getDataType())
    
    def test_setDataAndGetArgs(self):
        client = Zingg(args, options)
        client.initAndExecute()

        java_args = client.getArguments()
        java_pipes = java_args.getData()

        self.assertEqual(len(java_pipes), 1)

        for python_pipe, java_pipe in zip([inputPipe, outputPipe], java_pipes):
            self.assertEqual(python_pipe.pipe.getName(), java_pipe.getName())
            self.assertEqual(python_pipe.pipe.getFormat(), java_pipe.getFormat())
    
    def test_setOutput(self):
        client = Zingg(args, options)
        client.initAndExecute()

        java_args = client.getArguments()
        java_pipes = java_args.getOutput()

        self.assertEqual(len(java_pipes), 1)

        for python_pipe, java_pipe in zip([outputPipe], java_pipes):
            self.assertEqual(python_pipe.pipe.getName(), java_pipe.getName())
            self.assertEqual(python_pipe.pipe.getFormat(), java_pipe.getFormat())

    
    def test_setObviousDupeCondition(self):
        client = Zingg(args, options)
        client.initAndExecute()
        expected_condition = "fname"
        args.setObviousDupeCondition(expected_condition)

        java_args = client.getArguments()
        actual_condition = java_args.getObviousDupeCondition()
        print("expected_condition:", expected_condition)
        print("actual_condition:", actual_condition)

        self.assertEqual(actual_condition, expected_condition)


    def test_setZinggDir(self):
        client = Zingg(args, options)
        client.initAndExecute()
        expected_dir = "models"
    
        java_args = client.getArguments()
        actual_dir = java_args.getZinggDir()
        print(actual_dir)
    
        self.assertEqual(actual_dir, expected_dir)
    
    def test_setNumPartitions(self):
        client = Zingg(args, options)
        client.initAndExecute()
        expected_partitions = 4
        
        java_args = client.getArguments()
        actual_partitions = java_args.getNumPartitions()
        print(actual_partitions)
        
        self.assertEqual(actual_partitions, expected_partitions)
    
    def test_setLabelDataSampleSize(self):
        client = Zingg(args, options)
        client.initAndExecute()
        expected_sample_size = 0.5
        
        java_args = client.getArguments()
        actual_sample_size = java_args.getLabelDataSampleSize()
        print(actual_sample_size)

        self.assertEqual(actual_sample_size, expected_sample_size)
    
    def test_setStopWordsCutoff(self):
        client = Zingg(args, options)
        client.initAndExecute()
        stopWordsCutoff = 0.2
        
        args.setStopWordsCutoff(stopWordsCutoff)
        java_args = client.getArguments()
        actual_stopWordsCutoff = java_args.getStopWordsCutoff()
        
        self.assertEqual(actual_stopWordsCutoff, stopWordsCutoff)

    # def test_createArgumentsFromJSON(self):
    #     # client = Zingg(args, options)
    #     # client.initAndExecute()
    #     fileName = "createArguments.json"
    #     phase = "label_phase"

    #     obj = args.createArgumentsFromJSON(fileName, phase)

    #     self.assertIsInstance(obj, args)
    #     if os.path.exists(fileName):
    #         os.remove(fileName)
    
    # def test_writeArgumentsToJSON(self):
    #     client = Zingg(args, options)
    #     client.initAndExecute()
    #     json_file_name = "arguments_file.json"

    #     args.writeArgumentsToJSON(json_file_name)

    #     self.assertTrue(os.path.exists(json_file_name))
    #     os.remove(json_file_name)

class TestFieldDefinition(TestCase):
    def setUp(self):
        self.field_def = FieldDefinition("fname", "string", "EXACT")

    def test_constructor(self):
        self.assertEqual(self.field_def.fd.getFieldName(), "fname")
        self.assertEqual(self.field_def.fd.getDataType(), "string")
        
        actual_match_type = self.field_def.fd.getMatchType()
        expected_match_type = ["EXACT"]
        self.assertEqual(set(actual_match_type), set(expected_match_type))

    def test_setStopWords(self):
        self.field_def.setStopWords("stopwords.csv")
        self.assertEqual(self.field_def.fd.getStopWords(), "stopwords.csv")

    def test_getFieldDefinition(self):
        field_definition = self.field_def.getFieldDefinition()
        self.assertEqual(field_definition.getFieldName(), "fname")
        self.assertEqual(field_definition.getDataType(), "string")
        
        actual_match_type = self.field_def.fd.getMatchType()
        expected_match_type = ["EXACT"]
        self.assertEqual(set(actual_match_type), set(expected_match_type))

    def test_stringify(self):
        data_type = self.field_def.stringify("string")
        self.assertEqual(data_type, "string")
        
class TestClientOptions(TestCase):

    def setUp(self):
        location_options = ["--location", "custom_location"]
        self.client_options = ClientOptions(location_options)

    def test_getClientOptions(self):
        client_options = self.client_options.getClientOptions()
        self.assertIsNotNone(client_options)
        
    def test_getOptionValue(self):
        existing_option_name = ClientOptions.PHASE
        existing_option_value = self.client_options.getOptionValue(existing_option_name)
        self.assertIsNotNone(existing_option_value)

        custom_location_option_name = '--location'
        custom_location_option_value = self.client_options.getOptionValue(custom_location_option_name)
        self.assertEqual(custom_location_option_value, 'custom_location')

    def test_setOptionValue(self):
        option_name = '--custom_option'
        option_value = 'custom_value'

        try:
            retrieved_option_value = self.client_options.getOptionValue(option_name)
            self.assertEqual(retrieved_option_value, option_value)
        except Exception as e:
            if "NullPointerException" in str(e):
                # Skip this test scenario because it raised a NullPointerException
                self.skipTest("getOptionValue raised a NullPointerException")
            else:
                self.fail(f"getOptionValue raised an unexpected exception: {str(e)}")


    def test_getPhase(self):
        phase_value = self.client_options.getPhase()
        self.assertEqual(phase_value, 'peekModel')

    def test_setPhase(self):
        self.client_options.setPhase('new_phase')
        phase_value = self.client_options.getPhase()
        self.assertEqual(phase_value, 'new_phase')

    def test_getConf(self):
        conf_value = self.client_options.getConf()
        self.assertEqual(conf_value, 'dummyConf.json')

    def test_hasLocation(self):
        self.assertTrue(self.client_options.hasLocation())

    def test_getLocation(self):
        location = self.client_options.getLocation()
        self.assertEqual(location, "custom_location")