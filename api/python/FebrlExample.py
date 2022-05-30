from pyspark import SparkConf, SparkContext, SQLContext
from pyspark.sql.session import SparkSession
sc = SparkContext.getOrCreate()
sqlContext = SQLContext(sc)
spark = SparkSession(sc)

#build the arguments for zingg
args = sc._jvm.zingg.client.Arguments()
#set field definitions
fname = sc._jvm.zingg.client.FieldDefinition()
fname.setFieldName("fname")
fname.setDataType("\"string\"")
fname.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
fname.setFields("fname")

lname = sc._jvm.zingg.client.FieldDefinition()
lname.setFieldName("lname")
lname.setDataType("\"string\"")
lname.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
lname.setFields("lname")

stNo = sc._jvm.zingg.client.FieldDefinition()
stNo.setFieldName("stNo")
stNo.setDataType("\"string\"")
stNo.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
stNo.setFields("stNo")

add1 = sc._jvm.zingg.client.FieldDefinition()
add1.setFieldName("add1")
add1.setDataType("\"string\"")
add1.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
add1.setFields("add1")

add2 = sc._jvm.zingg.client.FieldDefinition()
add2.setFieldName("add2")
add2.setDataType("\"string\"")
add2.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
add2.setFields("add2")

city = sc._jvm.zingg.client.FieldDefinition()
city.setFieldName("city")
city.setDataType("\"string\"")
city.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
city.setFields("city")

areacode = sc._jvm.zingg.client.FieldDefinition()
areacode.setFieldName("areacode")
areacode.setDataType("\"string\"")
areacode.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
areacode.setFields("areacode")

state = sc._jvm.zingg.client.FieldDefinition()
state.setFieldName("state")
state.setDataType("\"string\"")
state.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
state.setFields("state")

dob = sc._jvm.zingg.client.FieldDefinition()
dob.setFieldName("dob")
dob.setDataType("\"string\"")
dob.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
dob.setFields("dob")

ssn = sc._jvm.zingg.client.FieldDefinition()
ssn.setFieldName("ssn")
ssn.setDataType("\"string\"")
ssn.setMatchType([sc._jvm.zingg.client.MatchType.FUZZY])
ssn.setFields("ssn")

#fieldDef = sc._jvm.java.util.ArrayList(sc._jvm.zingg.client.FieldDefinition)
fieldDef = sc._jvm.java.util.ArrayList()
fieldDef.add(fname)
fieldDef.add(lname)
fieldDef.add(stNo)
fieldDef.add(add1)
fieldDef.add(add2)
fieldDef.add(city)
fieldDef.add(areacode)
fieldDef.add(state)
fieldDef.add(dob)
fieldDef.add(ssn)

args.setFieldDefinition(fieldDef)
#set the modelid and the zingg dir
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
df = spark.read.format("csv").schema("id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn  string").load("examples/febrl/test.csv")
#import zingg.client.pipe.InMemoryPipe
#import java.util.HashMap

inputPipe = sc._jvm.zingg.client.pipe.Pipe()
inputPipe.setName("test")
inputPipe.setFormat(sc._jvm.zingg.client.pipe.Format.CSV)
inputPipe.setProp("location","examples/febrl/test.csv")

dfSchema = str(df.schema.json())
inputPipe.setSchema(dfSchema)

pipes = sc._gateway.new_array(sc._jvm.zingg.client.pipe.Pipe, 1)
pipes[0] = inputPipe
args.setData(pipes)

#setting outputpipe in 'args'
outputPipe = sc._jvm.zingg.client.pipe.InMemoryPipe()
outputPipe.setName("result")
outputPipe.setFormat(sc._jvm.zingg.client.pipe.Format.INMEMORY)
outputPipes = sc._gateway.new_array(sc._jvm.zingg.client.pipe.Pipe, 1)

outputPipes[0] = outputPipe
args.setOutput(outputPipes)

options = sc._jvm.zingg.client.ClientOptions(["--phase", "trainMatch",  "--conf", "dummy", "--license", "dummy", "--email", "xxx@yyy.com"])

#Zingg execution for the given phase
client = sc._jvm.zingg.client.Client(args, options)
client.init()
client.execute()
#the output is in outputPipe.getRecords
outputPipe.getRecords().show()
