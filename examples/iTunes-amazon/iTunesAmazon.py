from zingg import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
id = FieldDefinition("id", "string", MatchType.DONT_USE)
Song_Name = FieldDefinition("Song_Name", "string", MatchType.FUZZY)
Artist_Name = FieldDefinition("Artist_Name", "string", MatchType.FUZZY)
Album_Name = FieldDefinition("Album_Name","string", MatchType.FUZZY)
Genre = FieldDefinition("Genre", "string", MatchType.FUZZY)
Price = FieldDefinition("Price", "double", MatchType.FUZZY)
CopyRight = FieldDefinition("CopyRight", "string", MatchType.FUZZY)
Time = FieldDefinition("Time", "string", MatchType.FUZZY)
Released = FieldDefinition("Released", "string", MatchType.FUZZY)


fieldDefs = [id, Song_Name, Artist_Name, Album_Name, Genre, Price, CopyRight, Time, Released]

args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("105")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
dfiTunes = spark.read.format("csv").schema("id string, Song_Name string, Artist_Name string, Album_Name string, Genre string, Price double, CopyRight string, Time string, Released string").load("examples/iTunes-amazon/iTunesMusic.csv")
dfSchemaiTunes = str(dfiTunes.schema.json())
inputPipeiTunes = CsvPipe("testiTunes", dfSchemaiTunes, "examples/iTunes-amazon/iTunesMusic.csv")

dfAmazon = spark.read.format("csv").schema("id string, Song_Name string, Artist_Name string, Album_Name string, Genre string, Price double, CopyRight string, Time string, Released string").load("examples/iTunes-amazon/AmazonMusic.csv")
dfSchemaAmazon = str(dfAmazon.schema.json())
inputPipeAmazon = CsvPipe("testAmazon", dfSchemaAmazon, "examples/iTunes-amazon/AmazonMusic.csv")

args.setData(inputPipeiTunes,inputPipeAmazon)

#setting outputpipe in 'args'
outputPipe = CsvPipe("iTunesAmazonresult", None, "/tmp/iTunesAmazonOutput")

args.setOutput(outputPipe)

options = ClientOptions()
options.setPhase("link")

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()