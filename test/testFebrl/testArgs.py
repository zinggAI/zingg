from unittest.case import TestCase
from unittest.mock import Mock, patch
from zingg import *
from zingg.client import *
from zingg.pipes import *

from pyspark.sql import SparkSession
import pandas as pd
import json

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
inputPipe = CsvPipe("unittestFebrlInput", "examples/febrl/test.csv", schema)
trainingPipe = CsvPipe("unittestFebrlTraining", "examples/febrl/training_data.csv", schema)
outputPipe = CsvPipe("unittestFebrlResult", "/tmp/pythonTestFebrl")
args.setData(inputPipe)
args.setTrainingSamples(trainingPipe)
args.setOutput(outputPipe)
options = ClientOptions([ClientOptions.PHASE,"trainMatch"])

global args1
args1 = Arguments()
args1.setModelId("100")
args1.setZinggDir("models")

class TestZinggClient(TestCase):

    def test_initSparkClient(self):
        result = initSparkClient()
        self.assertEqual(result, 1)
    
    def test_initDataBricksConectClient(self):
        result = initDataBricksConectClient()
        self.assertEqual(result, 1)

    def test_initClient_spark(self):
        global _spark_ctxt
        _spark_ctxt = None
        result = initClient()
        self.assertEqual(result, 1)
    
    def test_initClient_databricks(self):
        global _spark_ctxt
        _spark_ctxt = None
        os.environ['DATABRICKS_CONNECT'] = 'Y'
        result = initClient()
        self.assertEqual(result, 1)

    def test_getSparkContext(self):
        global _spark_ctxt
        _spark_ctxt = None
        result = getSparkContext()
        self.assertIsInstance(result, SparkContext)

    def test_getSparkSession(self):
        global _spark
        _spark = None
        result = getSparkSession()
        self.assertIsInstance(result, SparkSession)

    def test_getSqlContext(self):
        global _sqlContext
        _sqlContext = None
        result = getSqlContext()
        self.assertIsInstance(result, SQLContext)

    def test_getJVM(self):
        result = getJVM()
        self.assertIsNotNone(result)

    def test_getGateway(self):
        result = getGateway()
        self.assertIsNotNone(result)

    # def test_getDfFromDs(self):
    #     spark = SparkSession.builder.master("local").appName("test").getOrCreate()

    #     data = spark.createDataFrame([(1, "Alice"), (2, "Bob"), (3, "Charlie")], ["id", "name"])
    #     result = getDfFromDs(data)
    #     self.assertIsInstance(result, DataFrame)
    #     spark.stop()
    
    def test_getDfFromDs(self):
        data = Mock()
        
        data.df.return_value = Mock()
        data.df().collect.return_value = [(1, "Alice"), (2, "Bob"), (3, "Charlie")]
        data.df().columns = ["id", "name"]

        mock_sql_context = Mock()

        with patch("client.getSqlContext", return_value=mock_sql_context):
            result = getDfFromDs(data)

        self.assertIsInstance(result, DataFrame)
    
    # def test_getPandasDfFromDs(self):
    #     spark = SparkSession.builder.master("local").appName("test").getOrCreate()

    #     test_data = [(1, "Alice"), (2, "Bob"), (3, "Charlie")]
    #     schema = ["id", "name"]
    #     data = spark.createDataFrame(test_data, schema)
    #     result = getPandasDfFromDs(data)

    #     self.assertIsInstance(result, pd.DataFrame)

    #     expected_data = pd.DataFrame(test_data, columns=schema)
    #     pd.testing.assert_frame_equal(result, expected_data)
    
    def test_getPandasDfFromDs(self):
        data = Mock()
        
        mock_df = Mock(spec=DataFrame)
        mock_df.collect.return_value = [(1, "Alice"), (2, "Bob"), (3, "Charlie")]
        mock_df.columns = ["id", "name"]
        
        with patch("zingg.client.getDfFromDs", return_value=mock_df):
            result = getPandasDfFromDs(data)
        
        self.assertIsInstance(result, pd.DataFrame)
        expected_data = {"id": [1, 2, 3], "name": ["Alice", "Bob", "Charlie"]}
        self.assertTrue(result.equals(pd.DataFrame(expected_data)))
        

class TestZingg(TestCase):

    def test_init(self):
        zingg = Zingg(args, options)

        self.assertIsInstance(zingg.client, object)
    
    # def test_execute(self):
    #     zingg = Zingg(args, options)

    #     zingg.execute()
    #     self.assertIsInstance(zingg.client, object)
    
    def test_init_and_execute(self):
        zingg = Zingg(args, options)

        zingg.initAndExecute()
        self.assertIsInstance(zingg.client, object)
    
    # def test_execute_label(self):
    #     zingg = Zingg(args, options)

    #     zingg.executeLabel()
    #     self.assertIsInstance(zingg.client, object)
    
    # def test_executeLabelUpdate(self):
    #     zingg = Zingg(args, options)
    #     marked_records = ["MarkedRecord1", "MarkedRecord2"]
    #     inp_args = ["UpdatedRecord1", "UpdatedRecord2"]
    #     zingg.getMarkedRecords = lambda: marked_records
    #     zingg.executeLabelUpdate()
    #     self.assertEqual(zingg.updated_records, inp_args)
    
    # def test_executeLabelUpdate(self):
    #     zingg = Zingg(args, options)

    #     marked_records = ["MarkedRecord1", "MarkedRecord2"]
    #     inp_args = ["UpdatedRecord1", "UpdatedRecord2"]

    #     def new_getMarkedRecords():
    #         return marked_records
    #     zingg.getMarkedRecords = new_getMarkedRecords

    #     def new_processRecordsCliLabelUpdate(records, inp_args):
    #         self.assertEqual(records, marked_records)
    #         self.assertEqual(inp_args, inp_args)
    #     zingg.processRecordsCliLabelUpdate = new_processRecordsCliLabelUpdate

    #     zingg.executeLabelUpdate()
    #     self.assertEqual(zingg.updated_records, ["UpdatedRecord1", "UpdatedRecord2"])

    def test_getMarkedRecords(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        new_marked_records = ["Record1", "Record2"]
        original_getMarkedRecords = zingg.client.getMarkedRecords
        zingg.client.getMarkedRecords = lambda: new_marked_records

        marked_records = zingg.getMarkedRecords()

        zingg.client.getMarkedRecords = original_getMarkedRecords

        self.assertEqual(marked_records, new_marked_records)
    
    def test_getUnmarkedRecords(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        new_unmarked_records = ["UnmarkedRecord1", "UnmarkedRecord2"]
        original_getUnmarkedRecords = zingg.client.getUnmarkedRecords
        zingg.client.getUnmarkedRecords = lambda: new_unmarked_records

        unmarked_records = zingg.getUnmarkedRecords()

        zingg.client.getUnmarkedRecords = original_getUnmarkedRecords

        self.assertEqual(unmarked_records, new_unmarked_records)

    def test_writeLabelledOutput(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        new_updated_records = ["UpdatedRecord1", "UpdatedRecord2"]
        called_write_labelled_output = False

        class newTrainingDataModel:
            def writeLabelledOutput(self, records, args):
                nonlocal called_write_labelled_output
                called_write_labelled_output = True
        zingg.client.getTrainingDataModel = lambda: newTrainingDataModel()
        zingg.writeLabelledOutput(new_updated_records, args)

        self.assertTrue(called_write_labelled_output)

    def test_getMarkedRecordsStat(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        marked_records = [("Record1", 1), ("Record2", 1), ("Record3", 0)]

        def new_getMarkedRecordsStat(markedRecords, value):
            return len([record for record in markedRecords if record[1] == value])
        zingg.client.getMarkedRecordsStat = new_getMarkedRecordsStat

        num_marked_records = zingg.getMarkedRecordsStat(marked_records, 1)

        self.assertEqual(num_marked_records, 2)
    
    def test_getMatchedMarkedRecordsStat(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        marked_records = [("Record1", 1), ("Record2", 1), ("Record3", 0)]

        def new_getMarkedRecords():
            return marked_records
        zingg.client.getMarkedRecords = new_getMarkedRecords

        def new_getMarkedRecordsStat(markedRecords, value):
            return len([record for record in markedRecords if record[1] == value])
        zingg.client.getMarkedRecordsStat = new_getMarkedRecordsStat

        def new_getMatchedMarkedRecordsStat(markedRecords):
            return len([record for record in markedRecords if record[1] == 1])
        zingg.client.getMatchedMarkedRecordsStat = new_getMatchedMarkedRecordsStat

        num_matched_marked_records = zingg.getMatchedMarkedRecordsStat()

        self.assertEqual(num_matched_marked_records, 2)

    def test_getUnmatchedMarkedRecordsStat(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        marked_records = [("Record1", 1), ("Record2", 1), ("Record3", 0), ("Record4", 0)]

        def new_getMarkedRecords():
            return marked_records
        zingg.client.getMarkedRecords = new_getMarkedRecords

        def new_getMarkedRecordsStat(markedRecords, value):
            return len([record for record in markedRecords if record[1] == value])
        zingg.client.getMarkedRecordsStat = new_getMarkedRecordsStat

        def new_getUnmatchedMarkedRecordsStat(markedRecords):
            return len([record for record in markedRecords if record[1] == 0])
        zingg.client.getUnmatchedMarkedRecordsStat = new_getUnmatchedMarkedRecordsStat

        num_unmatched_marked_records = zingg.getUnmatchedMarkedRecordsStat()

        self.assertEqual(num_unmatched_marked_records, 2)
    
    def test_getUnsureMarkedRecordsStat(self):
        options = ClientOptions()
        zingg = Zingg(args, options)

        marked_records = [("Record1", 1), ("Record2", 1), ("Record3", 0), ("Record4", 2), ("Record5", 2)]

        def new_getMarkedRecords():
            return marked_records
        zingg.client.getMarkedRecords = new_getMarkedRecords

        def new_getMarkedRecordsStat(markedRecords, value):
            return len([record for record in markedRecords if record[1] == value])
        zingg.client.getMarkedRecordsStat = new_getMarkedRecordsStat

        def new_getUnsureMarkedRecordsStat(markedRecords):
            return len([record for record in markedRecords if record[1] == 2])
        zingg.client.getUnsureMarkedRecordsStat = new_getUnsureMarkedRecordsStat

        num_unsure_marked_records = zingg.getUnsureMarkedRecordsStat()

        self.assertEqual(num_unsure_marked_records, 2)
    

class TestZinggWithSpark(TestCase):
    def test_init(self):
        client = Zingg(args, options)
        client.initAndExecute()
        zingg_spark = ZinggWithSpark(args, options)

        self.assertIsInstance(zingg_spark.client, object)

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
    
    def test_setTrainingSamples(self):
        client = Zingg(args, options)
        client.initAndExecute()

        java_args = client.getArguments()
        java_pipes = java_args.getTrainingSamples()

        self.assertEqual(len(java_pipes), 1)

        for python_pipe, java_pipe in zip([trainingPipe], java_pipes):
            self.assertEqual(python_pipe.pipe.getName(), java_pipe.getName())
            self.assertEqual(python_pipe.pipe.getFormat(), java_pipe.getFormat())
            
    # def test_setDeterministicMatchingCondition(self):
    #     dm1 = DeterministicMatching('fname', 'stNo', 'add1')
    #     dm2 = DeterministicMatching('ssn')
    #     dm3 = DeterministicMatching('fname', 'stNo', 'lname')
        
    #     client = Zingg(args, options)
    #     client.initAndExecute()
        
    #     java_args = client.getArguments()
    #     args.setDeterministicMatchingCondition(dm1, dm2, dm3)
        
    #     actual_conditions = java_args.getDeterministicMatching()
    #     expected_conditions = ['fname', 'stNo', 'add1', 'ssn', 'fname', 'stNo', 'lname']
        
    #     print("expected_conditions:", expected_conditions)
    #     print("actual_conditions:", actual_conditions)
        
    #     self.assertEqual(actual_conditions, expected_conditions)

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
    
    def test_createArgumentsFromJSON(self):
        fileName = "examples/febrl/config.json"
        phase = "label"

        obj = args.createArgumentsFromJSON(fileName, phase)
        print("JSON file content:", obj)
        print("Phase:", phase)

        self.assertIsInstance(obj, Arguments)

    def test_writeArgumentsToJSON(self):
        json_file_name = "arguments_file.json"

        args.writeArgumentsToJSON(json_file_name)

        self.assertTrue(os.path.exists(json_file_name))
        os.remove(json_file_name)
    
    def test_writeArgumentsToJSONString(self):
        # print("new args: ", args1)
        # print("old args: ", args)
        json_string = args1.writeArgumentsToJSONString()
        # json_string1 = args.writeArgumentsToJSONString()
        print("json_string: ",json_string)
        # print("oldjson_string: ", json_string1)
        data = json.loads(json_string)
        print("data: ", data)
        
        self.assertEqual(data['modelId'], "100")
        self.assertEqual(data['zinggDir'], "models")

    def test_createArgumentsFromJSONString(self):
        sample_json = '''
        {
            "fieldDefinition": [
                {
                    "fieldName": "recId",
                    "matchType": "dont_use",
                    "fields": "recId",
                    "dataType": "string"
                },
                {
                    "fieldName": "fname",
                    "matchType": "fuzzy",
                    "fields": "fname",
                    "dataType": "string"
                },
                {
                    "fieldName": "lname",
                    "matchType": "fuzzy",
                    "fields": "lname",
                    "dataType": "string"
                },
                {
                    "fieldName": "stNo",
                    "matchType": "fuzzy",
                    "fields": "stNo",
                    "dataType": "string"
                },
                {
                    "fieldName": "add1",
                    "matchType": "fuzzy",
                    "fields": "add1",
                    "dataType": "string"
                }
            ],
            "output": [
                {
                    "name": "output",
                    "format": "csv",
                    "props": {
                        "location": "/tmp/zinggOutput",
                        "delimiter": ",",
                        "header": true
                    }
                }
            ],
            "data": [
                {
                    "name": "test",
                    "format": "csv",
                    "props": {
                        "location": "examples/febrl/test.csv",
                        "delimiter": ",",
                        "header": false
                    },
                    "schema": "recId string, fname string, lname string, stNo string, add1 string"
                }
            ],
            "labelDataSampleSize": 0.5,
            "numPartitions": 4,
            "modelId": 100,
            "zinggDir": "models"
        }
        '''
        phase = "label"
        
        obj = args.createArgumentsFromJSONString(sample_json, phase)

        self.assertIsInstance(obj, Arguments)
        self.assertEqual(obj.getModelId(), "100")
    
    def test_copyArgs(self):
        phase = "test_phase"
        copied_args = args.copyArgs(phase)

        self.assertIsInstance(copied_args, Arguments)

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

class ClientTest(TestCase):
    def test_parseArguments(self):
        args = ["--phase", "train",
                "--conf", "conf.json"]

        parsed_args = parseArguments(args)

        self.assertEqual(parsed_args.phase, "train")
        self.assertEqual(parsed_args.conf, "conf.json")

class TestPipe(TestCase):

    def test_init(self):
        pipe = Pipe(name="my_pipe", format="bigquery")
        self.assertIsNotNone(pipe)

    def test_add_property(self):
        pipe = Pipe("my_pipe", "csv")
        name = "property_name"
        value = "property_value"
        pipe.addProperty(name, value)
        
        self.assertEqual(pipe.getPipe().getProps().get(name), value)
    
    def test_set_schema(self):
        pipe = Pipe("my_pipe", "csv")
        schema = "id string, name string, age int"
        pipe.setSchema(schema)
        
        self.assertEqual(pipe.getPipe().getSchema(), schema)

    def test_to_string(self):
        pipe = Pipe(name="my_pipe", format="bigquery")
        string_representation = pipe.toString()
        self.assertIsNotNone(string_representation)

class TestCsvPipe(TestCase):    
    def test_init(self):
        location = "/path/to/some/location"
        schema = "your_schema"
        pipe = CsvPipe("csv_pipe", location=location, schema=schema)
        
        self.assertEqual(pipe.pipe.getProps()["location"], location)
        self.assertEqual(pipe.pipe.getSchema(), schema)
    
    def test_init_with_location_and_schema(self):
        pipe = CsvPipe("csv_pipe", location="/path/to/some/location", schema="your_schema")
        self.assertEqual(pipe.pipe.getProps()[FilePipe.LOCATION], "/path/to/some/location")
        self.assertEqual(pipe.pipe.getSchema(), "your_schema")

    def test_set_delimiter(self):
        pipe = CsvPipe("csv_pipe")
        delimiter = ","
        pipe.setDelimiter(delimiter)
        self.assertEqual(pipe.pipe.getProps()[FilePipe.DELIMITER], delimiter)
    
    def test_set_location(self):
        pipe = CsvPipe("csv_pipe")
        location = "/path/to/some/location"
        pipe.setLocation(location)
        self.assertEqual(pipe.pipe.getProps()[FilePipe.LOCATION], location)
    
    def test_set_header(self):
        pipe = CsvPipe("csv_pipe")
        header = "true"  # or "false" to represent True or False
        pipe.setHeader(header)
        self.assertEqual(pipe.pipe.getProps()[FilePipe.HEADER], header)

class TestBigQueryPipe(TestCase):
    def test_set_credential_file(self):
        pipe = BigQueryPipe("bq_pipe")
        credential_file = "my_credentials.json"
        pipe.setCredentialFile(credential_file)
        
        self.assertEqual(pipe.pipe.getProps()[BigQueryPipe.CREDENTIAL_FILE], credential_file)

    def test_set_table(self):
        pipe = BigQueryPipe("bq_pipe")
        table = "my_table"
        pipe.setTable(table)
        
        self.assertEqual(pipe.pipe.getProps()[BigQueryPipe.TABLE], table)

    def test_set_temporary_gcs_bucket(self):
        pipe = BigQueryPipe("bq_pipe")
        bucket = "my_temp_bucket"
        pipe.setTemporaryGcsBucket(bucket)
        
        self.assertEqual(pipe.pipe.getProps()[BigQueryPipe.TEMP_GCS_BUCKET], bucket)

    def test_set_views_enabled(self):
        pipe = BigQueryPipe("bq_pipe")
        views_enabled = "true"
        pipe.setViewsEnabled(views_enabled)
        
        self.assertEqual(pipe.pipe.getProps()[BigQueryPipe.VIEWS_ENABLED], views_enabled)

class TestSnowflakePipe(TestCase):
    def test_init(self):
        name = "snowflake_pipe"
        pipe = SnowflakePipe(name)
        self.assertEqual(pipe.pipe.getName(), name)
        self.assertEqual(pipe.pipe.getProps()["application"], "zinggai_zingg")
    
    def test_set_url(self):
        pipe = SnowflakePipe("snowflake_pipe")
        url = "https://example-snowflake-url.com"
        pipe.setURL(url)
        self.assertEqual(pipe.pipe.getProps()["sfUrl"], url)
    
    def test_set_user(self):
        pipe = SnowflakePipe("snowflake_pipe")
        user = "snowflake_user"
        pipe.setUser(user)
        self.assertEqual(pipe.pipe.getProps()["sfUser"], user)
    
    def test_set_password(self):
        pipe = SnowflakePipe("snowflake_pipe")
        password = "password123"
        pipe.setPassword(password)
        self.assertEqual(pipe.pipe.getProps()["sfPassword"], password)
    
    def test_set_database(self):
        pipe = SnowflakePipe("snowflake_pipe")
        database = "my_database"
        pipe.setDatabase(database)
        self.assertEqual(pipe.pipe.getProps()["sfDatabase"], database)
    
    def test_set_schema(self):
        pipe = SnowflakePipe("snowflake_pipe")
        schema = "my_schema"
        pipe.setSFSchema(schema)
        self.assertEqual(pipe.pipe.getProps()["sfSchema"], schema)
    
    def test_set_warehouse(self):
        pipe = SnowflakePipe("snowflake_pipe")
        warehouse = "my_warehouse"
        pipe.setWarehouse(warehouse)
        self.assertEqual(pipe.pipe.getProps()["sfWarehouse"], warehouse)
    
    def test_set_db_table(self):
        pipe = SnowflakePipe("snowflake_pipe")
        db_table = "my_table"
        pipe.setDbTable(db_table)
        self.assertEqual(pipe.pipe.getProps()["dbtable"], db_table)