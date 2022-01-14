# import modules
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
#train="../original/train.csv"
test="../original/test.csv"
valid="../original/valid.csv"

testA_out = "../testA.csv"
testB_out = "../testB.csv"

def readCSVFile(spark, fileName) :
	logger.info("Reading CSV File " + fileName)
	df = spark.read \
				.option("delimiter",",") \
				.option("header", True) \
				.csv(fileName)
	return df

def writeCSVFile(df, fileName) :
	df.coalesce(1) \
		.write.format("csv") \
		.mode("overwrite") \
		.option("header", "true") \
		.save(fileName)


def main():
	# start spark code
	spark = SparkSession.builder.appName(AppName+"_"+str(dt_string)).getOrCreate()
	spark.sparkContext.setLogLevel("ERROR")
	logger.info("Starting spark application")

	logger.info("Reading CSV Files")
	tableADF = readCSVFile(spark, tableA)
	tableBDF = readCSVFile(spark, tableB)
	#trainDF = readCSVFile(spark, train)
	testDF = readCSVFile(spark, test)

	print(tableADF.count())
	print(tableBDF.count())
	print(testDF.count())
	
	tableACols = tableADF.columns
	tableBCols = tableBDF.columns

	trainingCols = tableACols
	print (tableADF.columns)
	print (tableBCols)
	print(trainingCols)

	testCols = testDF.columns
	testCols.append("cluster_id")
	print(testCols)
	rdd2 = testDF.rdd.map(lambda x: getClusterId(x))
	df = rdd2.toDF(testCols)
	df.show()

	recordIdA = df.join(tableADF, tableADF.id == df.ltable_id)
	outputTestA = recordIdA.select(trainingCols)
	print(outputTestA.count())
	outputTestA = outputTestA.dropDuplicates(['id'])
	print(outputTestA.count())
	#outputTestA.show()
	print("")
	recordIdB = df.join(tableBDF, tableBDF.id == df.rtable_id)
	outputTestB = recordIdB.select(trainingCols)
	print(outputTestB.count())
	outputTestB = outputTestB.dropDuplicates(['id'])
	print(outputTestB.count())
	#outputTestB.show()

	writeCSVFile(outputTestA, testA_out)
	writeCSVFile(outputTestB, testB_out)

	logger.info("Ending spark application")
	# end spark code
	spark.stop()
	return None

# Starting point for PySpark
if __name__ == '__main__':
	main()
	sys.exit()