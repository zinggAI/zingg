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
description.setStopWords("dbfs:/FileStore/tables/stopWords.csv")
manufacturer = FieldDefinition("manufacturer","string", MatchType.FUZZY)
price = FieldDefinition("price", "double", MatchType.FUZZY)

fieldDefs = [id, title, description, manufacturer, price]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("103")
args.setZinggDir("dbfs:/FileStore/tables/models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)


schema = StructType([StructField("id", StringType(), True)\
                   ,StructField("title", StringType(), True)\
                   ,StructField("description", StringType(), True)\
                   ,StructField("manufacturer", StringType(), True)\
                   ,StructField("price", DoubleType(), True)])


inputPipeAmazon=InMemoryPipe("amz")
inputPipeAmazon.setDataset(getSparkSession().read.format("csv").schema(schema).load("dbfs:/FileStore/tables/Amazon.csv"))
inputPipeGoogle=InMemoryPipe("google")
inputPipeGoogle.setDataset(getSparkSession().read.format("csv").schema(schema).load("dbfs:/FileStore/tables/GoogleProducts.csv"))

args.setData(inputPipeAmazon,inputPipeGoogle)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultAmazonGoogle", "dbfs:/FileStore/tables/AwsGoogleOutput")

args.setOutput(outputPipe)

inpPhase = input("Enter phase: ")

options = ClientOptions([ClientOptions.PHASE,inpPhase])

#Zingg execution for the given phase
zingg = Zingg(args, options)

zingg.initAndExecute()

