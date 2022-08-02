from zingg.client import *
import sys
import argparse
import os

logging.basicConfig(level=logging.INFO)
LOG = logging.getLogger("zingg.exportModel")

def main():
    
    # ckecking for mandatory option --location for this phase
    if(ClientOptions(sys.argv[1:]).hasLocation()==False):
        LOG.error("--location argument is mandatory for this phase")
        LOG.info("--location is location of CSV file for exported data")
        sys.exit()
    
    LOG.info("Phase ExportModel starts")   

    options = ClientOptions(sys.argv[1:])
    options.setPhase("peekModel")
    arguments = Arguments.createArgumentsFromJSON(options.getConf(), options.getPhase())
    client = ZinggWithSpark(arguments, options)
    client.init()

    pMarkedDF = client.getPandasDfFromDs(client.getMarkedRecords())
    labelledData = spark.createDataFrame(pMarkedDF)
    location = options.getLocation()

    export_data(labelledData, location)

    LOG.info("Phase ExportModel ends")

def export_data(labelledData, location):

    baseCols = ['z_cluster', 'z_zid', 'z_prediction', 'z_score', 'z_source', 'z_isMatch']
    sourceDataColumns =  [c for c in labelledData.columns if c not in  baseCols]
    additionalTrainingColumns = ['z_cluster','z_isMatch']
    trainingSampleColumns = [*additionalTrainingColumns, *sourceDataColumns]
    trainingSamples = labelledData.select(trainingSampleColumns)

    # Getting schema
    trainingSamples.schema.jsonValue()
    trainingSamples.show()
    trainingSamples.columns
    print(trainingSampleColumns)

    # Exporting the labelled data as CSV
    trainingSamples.toPandas().to_csv(os.path.join(location,r'exportedData.csv'))

    
if __name__ == "__main__":
    main()
