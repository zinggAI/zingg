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
import sys
import argparse
import os

logging.basicConfig(level=logging.INFO)
LOG = logging.getLogger("zingg.exportModel")

def main():
    
    # ckecking for mandatory option --location for this phase
    if(ClientOptions(sys.argv[1:]).hasLocation()==False):
        LOG.error("--location argument is mandatory for this phase, please specify")
        LOG.info("--location is location of CSV file for exported data")
        sys.exit()
    
    LOG.info("Phase ExportModel starts")   

    options = ClientOptions(sys.argv[1:])
    options.setPhase("peekModel")
    arguments = Arguments.createArgumentsFromJSON(options.getConf(), options.getPhase())
    client = ZinggWithSpark(arguments, options)
    client.init()

    pMarkedDF = getPandasDfFromDs(client.getMarkedRecords())
    labelledData = getSparkSession().createDataFrame(pMarkedDF)
    location = options.getLocation()

    export_data(labelledData, location)

    LOG.info("Phase ExportModel ends")

def export_data(labelledData, location):

    baseCols = ['z_cluster', 'z_zid', 'z_prediction', 'z_score', 'z_zsource', 'z_isMatch']
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
