#  Zingg
#  Copyright (C) 2021-Present  Zingg Labs,inc
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from zingg.client import *
from zingg.pipes import *
from pyspark.sql.types import *
import pandas

#build the arguments for zingg
args = Arguments()
#set field definitions
id = FieldDefinition("id", "string", MatchType.DONT_USE)
title = FieldDefinition("title", "string", MatchType.NUMERIC)
description = FieldDefinition("description", "string", MatchType.TEXT)
description.setStopWords("examples/amazon-google/stopWords.csv")
manufacturer = FieldDefinition("manufacturer","string", MatchType.FUZZY)
price = FieldDefinition("price", "double", MatchType.FUZZY)

fieldDefs = [id, title, description, manufacturer, price]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("103")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
#schemaType = {'id': 'string', 'title': 'string', 'description': 'string', 'manufacturer': 'string', 'price': 'string'}
#amzDF = pandas.read_csv("~/zingg/examples/amazon-google/Amazon.csv", encoding="iso-8859-1", dtype=schemaType)
#amzDF.info()
#print(amzDF)
#amzDF = amzDF[~amzDF['description'].isnull()] 
#amzDF.info()
schema = StructType([StructField("id", StringType(), True)\
                   ,StructField("title", StringType(), True)\
                   ,StructField("description", StringType(), True)\
                   ,StructField("manufacturer", StringType(), True)\
                   ,StructField("price", DoubleType(), True)])

#gDF = pandas.read_csv("~/zingg/examples/amazon-google/Amazon.csv", encoding="iso-8859-1")
#amzDF = pandas.DataFrame()
#gDF=pandas.DataFrame()
inputPipeAmazon=InMemoryPipe("amz")
#inputPipeAmazon.setSchema("id string, title string, description string, manufacturer string, price string")
inputPipeAmazon.setDataset(getSparkSession().read.format("csv").schema(schema).load("examples/amazon-google/Amazon.csv"))
inputPipeGoogle=InMemoryPipe("google")
inputPipeGoogle.setDataset(getSparkSession().read.format("csv").schema(schema).load("examples/amazon-google/GoogleProducts.csv"))

args.setData(inputPipeAmazon,inputPipeGoogle)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultAmazonGoogle", "/tmp/AwsGoogleOutput")

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"match"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()

