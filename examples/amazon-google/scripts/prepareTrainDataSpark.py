# import modules
from distutils.log import Log
from pyspark.sql import SparkSession
from pyspark.sql.functions import col
import sys,logging
from datetime import datetime

# Logging configuration
formatter = logging.Formatter('[%(asctime)s] %(levelname)s @ line %(lineno)d: %(message)s')
handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.INFO)
handler.setFormatter(formatter)
logger = logging.getLogger()
logger.setLevel(logging.INFO)
logger.addHandler(handler)

# current time variable to be used for logging purpose
dt_string = datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
# change it to your app name
AppName = "ProcessData"

def getClusterId(x) :
	cluster_id = x.ltable_id + ":" + x.rtable_id
	return (x.ltable_id, x.rtable_id, x.label, cluster_id)

tableA="../original/tableA.csv"
tableB="../original/tableB.csv"
train="../original/train.csv"
# test="../original/test.csv"
valid="../original/valid.csv"

train_out = "../train.csv"

def readCSVFile(spark, fileName) :
	logger.info("Reading CSV File location " + fileName)
	df = spark.read \
				.option("delimiter",",") \
				.option("header", True) \
				.option("nullValue", '') \
				.option("mode", "PERMISSIVE") \
				.csv(fileName)
	return df

def writeCSVFile(df, fileName) :
	logger.info("Writing to CSV File location " + fileName)
	df.coalesce(1) \
		.write.format("csv") \
		.mode("overwrite") \
		.option("header", "true") \
		.option("nullValue", "") \
		.save(fileName)

def main():
	# start spark code
	spark = SparkSession.builder.appName(AppName+"_"+str(dt_string)).getOrCreate()
	spark.sparkContext.setLogLevel("ERROR")
	logger.info("Starting spark application")

	tableADF = readCSVFile(spark, tableA)
	tableBDF = readCSVFile(spark, tableB)
	trainDF = readCSVFile(spark, train)
	validDF = readCSVFile(spark, valid)



	logger.info("Previewing CSV File Data")
	#tableADF.show(truncate=False)

	#testDF.show(truncate=False)

	print (tableADF.columns)
	
	tableACols = tableADF.columns
	tableBCols = tableBDF.columns

	trainingCols = [] 
	trainingCols.append("cluster_id")
	trainingCols.append("label")
	trainingCols.extend(tableACols)
	print(trainingCols)

	testDF = trainDF.union(validDF)
	print(trainDF.count())
	print(validDF.count())
	print(testDF.count())

	testCols = testDF.columns
	testCols.append("cluster_id")
	print(testCols)
	rdd2 = testDF.rdd.map(lambda x: getClusterId(x))
	df = rdd2.toDF(testCols)
	#df.show()

	recordIdA = df.join(tableADF, tableADF.id == df.ltable_id)
	outputTestA = recordIdA.select(trainingCols)
	outputTestA.show()
	print("OutputA count: " + str(recordIdA.count()))

	recordIdB = df.join(tableBDF, tableBDF.id == df.rtable_id)
	outputTestB = recordIdB.select(trainingCols)
	outputTestB.show()
	print("OutputB count: " + str(recordIdB.count()))

	totalTrainData = outputTestA.union(outputTestB)

	print("Output count: " + str(totalTrainData.count()))

	writeCSVFile(totalTrainData, train_out)

	logger.info("Ending spark application")
	spark.stop()
	return None

# Starting point
if __name__ == '__main__':
	main()
	sys.exit()