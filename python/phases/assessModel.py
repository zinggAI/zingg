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
import pandas as pd
import seaborn as sn
import matplotlib.pyplot as plt
import sys
from IPython.display import display
import tk

logging.basicConfig(level=logging.INFO)
LOG = logging.getLogger("zingg.assessModel")

def main():
    LOG.info("Phase AssessModel starts")    
    print("arguments are ", sys.argv[0:]) 

    #excluding argv[0] that is nothing but the current executable file
    options = ClientOptions(sys.argv[1:])
    arguments = Arguments.createArgumentsFromJSON(options.getConf(), options.getPhase())
    client = ZinggWithSpark(arguments, options)
    client.init()

    #exec(open(sys.argv[2] + ".py"))

    pMarkedDF = getPandasDfFromDs(client.getMarkedRecords())
    pUnmarkedDF = getPandasDfFromDs(client.getUnmarkedRecords())

    total_marked = pMarkedDF.shape[0]
    total_unmarked = pUnmarkedDF.shape[0]
    matched_marked = client.getMatchedMarkedRecordsStat()
    unmatched_marked = client.getUnmatchedMarkedRecordsStat()
    unsure_marked = client.getUnsureMarkedRecordsStat()

    LOG.info("")
    LOG.info("No. of Records Marked   : %d", total_marked)
    LOG.info("No. of Records Unmarked : %d", total_unmarked)
    LOG.info("No. of Matches          : %d", matched_marked)
    LOG.info("No. of Non-Matches      : %d", unmatched_marked)
    LOG.info("No. of Not Sure         : %d", unsure_marked)
    LOG.info("")
    plotConfusionMatrix(pMarkedDF)

    LOG.info("Phase AssessModel ends")

def plotConfusionMatrix(pMarkedDF):
    #As no model is yet created and Zingg is still learning, removing the records with prediciton = -1
    pMarkedDF.drop(pMarkedDF[pMarkedDF[ColName.PREDICTION_COL] == -1].index, inplace=True)

    confusion_matrix = pd.crosstab(pMarkedDF[ColName.MATCH_FLAG_COL], pMarkedDF[ColName.PREDICTION_COL], rownames=['Actual'], colnames=['Predicted'])
    confusion_matrix = confusion_matrix / 2
    sn.heatmap(confusion_matrix, annot=True)
    plt.show()

if __name__ == "__main__":
    main()
