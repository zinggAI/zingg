import unittest
from unittest.case import TestCase
import unittest
from io import StringIO


from zingg import *
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

#testing
class Accuracy_recordCount(TestCase):
	def test_recordCount(self):
		client = Zingg(args, options)
		client.initAndExecute()
		pMarkedDF = getPandasDfFromDs(client.getMarkedRecords())
		labelledData = getSparkSession().createDataFrame(pMarkedDF)

		total_marked = pMarkedDF.shape[0]

		# marked record count test
		self.assertEqual(total_marked, 76)

		pMarkedDF.drop(pMarkedDF[pMarkedDF[ColName.PREDICTION_COL] == -1].index, inplace=True)
		acc = (pMarkedDF[ColName.MATCH_FLAG_COL]== pMarkedDF[ColName.PREDICTION_COL]).mean()

		# accuracy test
		self.assertGreater(acc, 0.9)

