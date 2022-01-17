# import modules
import json
import traceback
from pyspark.sql import SparkSession
import sys,logging
from datetime import datetime

class PairedEntities : 

	def __init__(self, first, second):
		self.first  = first
		self.second = second
		self.initAttributes()
		self.initSparkSession()

	def initAttributes(self) :
		# Input files
		self.tableA="../original/tableA.csv"
		self.tableB="../original/tableB.csv"
		self.train="../original/train.csv"
		self.test="../original/test.csv"
		self.valid="../original/valid.csv"
		# Output files
		self.testA_out = "../testA"
		self.testB_out = "../testB"
		self.train_out = "../train"
		self.schemaFile = "../trainingSchema.csv"

	def readFiles(self) :
		logger.info("")
		logger.info("Reading Input Files ...")
		self.tableADF = readCSVFile(self.spark, self.tableA)
		self.tableBDF = readCSVFile(self.spark, self.tableB)
		self.trainDF = readCSVFile(self.spark, self.train)
		self.validDF = readCSVFile(self.spark, self.valid)
		self.testDF = readCSVFile(self.spark, self.test)

	def writeTestFiles(self) :
		logger.info("")
		logger.info("Writing Test Data Files ...")
		
		tableACols = self.tableADF.columns
		tableBCols = self.tableBDF.columns

		testCols = tableACols

		cols = self.testDF.columns
		cols.append("cluster_id")
		rdd2 = self.testDF.rdd.map(lambda x: getClusterId(x))
		df = rdd2.toDF(cols)

		recordIdA = df.join(self.tableADF, self.tableADF.id == df.ltable_id)
		outputTestA = recordIdA.select(testCols)
		outputTestA = outputTestA.dropDuplicates(['id'])
		recordIdB = df.join(self.tableBDF, self.tableBDF.id == df.rtable_id)
		outputTestB = recordIdB.select(testCols)
		outputTestB = outputTestB.dropDuplicates(['id'])
 
		writeCSVFile(outputTestA, self.testA_out)
		writeCSVFile(outputTestB, self.testB_out)


	def writeTrainingDataFile(self) :
		logger.info("")
		logger.info("Writing Training Data Files ...")
	
		tableACols = self.tableADF.columns
		trainingCols = [] 
		trainingCols.append("cluster_id")
		trainingCols.append("label")
		trainingCols.extend(tableACols)

		trainDF = self.trainDF.union(self.validDF)

		cols = trainDF.columns
		cols.append("cluster_id")
		rdd2 = trainDF.rdd.map(lambda x: getClusterId(x))
		df = rdd2.toDF(cols)

		recordIdA = df.join(self.tableADF, self.tableADF.id == df.ltable_id)
		outputTestA = recordIdA.select(trainingCols)
		recordIdB = df.join(self.tableBDF, self.tableBDF.id == df.rtable_id)
		outputTestB = recordIdB.select(trainingCols)

		totalTrainData = outputTestA.union(outputTestB)
		writeCSVFile(totalTrainData, self.train_out)
		writeSchemaToFile(totalTrainData, self.schemaFile)
		
		
	def initSparkSession(self) :
		logger.info("Starting spark application")
		# current time variable to be used for logging purpose
		dt_string = datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
		AppName = "ProcessData"
		self.spark = SparkSession.builder.appName(AppName+"_"+str(dt_string)).getOrCreate()
		self.spark.sparkContext.setLogLevel("ERROR")

	def __del__(self):
		logger.info("Ending spark application")
		self.spark.stop()
# End of class PairedEntities

def getClusterId(x) :
	cluster_id = x.ltable_id + ":" + x.rtable_id
	return (x.ltable_id, x.rtable_id, x.label, cluster_id)


def readCSVFile(spark, fileName) :
	logger.info("Reading CSV File %s", fileName)
	df = spark.read \
				.option("delimiter",",") \
				.option("header", True) \
				.csv(fileName)
	logger.info("\t\tRecords: %d, Columns: %s", df.count(), df.columns)
	return df

def writeCSVFile(df, fileName) :
	logger.info("Writing CSV File at location %s", fileName)
	df.coalesce(1) \
		.write.format("csv") \
		.mode("overwrite") \
		.option("header", "true") \
		.save(fileName)
	logger.info("\t\tRecords: %d, Columns: %s", df.count(), df.columns)


def writeSchemaToFile(df, fileName) :
	with open(fileName, 'w') as f:
		json.dump(df.schema.jsonValue(), f)

# Logging configuration
formatter = logging.Formatter('[%(asctime)s] %(levelname)s @ line %(lineno)d: %(message)s')
handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.INFO)
handler.setFormatter(formatter)
logger = logging.getLogger()
logger.setLevel(logging.INFO)
logger.addHandler(handler)
	
def main():
	try :
		object = PairedEntities("google", "aws")
		object.readFiles()
		object.writeTestFiles()
		object.writeTrainingDataFile()

	except Exception as e:
		logger.warning("An exception has occured: ")
		traceback.print_exc()

	return None

# Starting point for PySpark
if __name__ == '__main__':
	main()
	sys.exit()