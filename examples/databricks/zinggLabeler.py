# Databricks notebook source
import pandas as pd
from pathlib import Path
import time
import os
import traceback

def getZinggBaseTrainingDataDir() :
	return zinggDir + "/" + modelId + "/trainingData/";

def getZinggTrainingDataUnmarkedDir() :
	return getZinggBaseTrainingDataDir() + "/unmarked/";

def  getZinggTrainingDataMarkedDir() :
    return getZinggBaseTrainingDataDir() + "/marked/";

def writeLabelledOutput(records) :
    if (records.empty == False) :
        print("Writing marked records in a parquet file ...")
        print(records.shape[0])
        now_ms = int( time.time_ns() / 1000 )
        fileName = "/markedRecords_" + str(now_ms) + ".parquet"
        if not os.path.exists(getZinggTrainingDataMarkedDir()):
            os.makedirs(getZinggTrainingDataMarkedDir())
        records.to_parquet(path=getZinggTrainingDataMarkedDir() + fileName, compression=None, engine='pyarrow')

def showWidget() :
    dbutils.widgets.dropdown("X", "2", [str(x) for x in range(0, 3)], "0: No, 1: Yes, 2: Cant say")
    
def printPostMessage(prediction, score) :
    IS_MATCH_PREDICTION = 1.0;
    IS_NOT_A_MATCH_PREDICTION = 0.0;
    IS_NOT_KNOWN_PREDICTION = -1.0;

    labelMatchType = {IS_MATCH_PREDICTION: "MATCH",
                IS_NOT_A_MATCH_PREDICTION: "DO NOT MATCH",
                  IS_NOT_KNOWN_PREDICTION: "ARE NOT KNOWN IF MATCH"};				

    matchType = labelMatchType.get(prediction);				
    if (prediction ==  IS_NOT_KNOWN_PREDICTION) :
        msg2 =  "Zingg has no prediction as it is still collecting training data to build the preliminary models.";
    else :
        msg2 =  "Zingg predicts the above records {0} with a similarity score of {1:.6g}".format(matchType, score);

    print(msg2);

def printDF(origPair, pair) :
    score = origPair.iloc[0, zScoreIndex];
    prediction = origPair.iloc[0, zPredictionIndex]
    pair.drop(skipCols, axis = 0, inplace = True);
    print(pair.to_string(header=False))
    print("")
    printPostMessage(prediction, score)

skipCols = {"z_cluster", "z_zid", "z_prediction", "z_score", "z_isMatch"}
zinggDir = "/dbfs/models"
modelId = "102"
unmarkedDataDir = getZinggTrainingDataUnmarkedDir()
showWidget()
i = 0
try:
    dfU = pd.read_parquet(getZinggTrainingDataUnmarkedDir(), engine='pyarrow')
    print("Unmarked : " + str(dfU.shape[0]))
    zIsMatchIndex = dfU.columns.get_loc("z_isMatch")
    zScoreIndex = dfU.columns.get_loc("z_score")
    zPredictionIndex = dfU.columns.get_loc("z_prediction")
    try:
        dfM = pd.read_parquet(getZinggTrainingDataMarkedDir(), engine='pyarrow')
        if not dfM.empty:
            print("Marked   : " + str(dfM.shape[0]))
            #with pd.option_context('display.max_rows', None, 'display.max_columns', None):
            #    print (dfM)
            markedClusterIds = dfM['z_cluster']
            
            df = dfU[~dfU['z_cluster'].isin(markedClusterIds)]
        else:
            df = dfU
    except Exception:
        print ("Error while reading marked parquet file")
        df = dfU

    print("To be labelled : " + str(df.shape[0]/2))
    df = df.sort_values('z_cluster')
    markedRecords = pd.DataFrame()
   
    print("");
    
    if not df.empty :        
        print("Is this pair a Match?\n")
        dis = pd.concat([df.iloc[i], df.iloc[i+1] ], axis = 1)
        #print(dis.to_string(header=False))
        disOrig = df.iloc[i:i+1]
        printDF(disOrig, dis)
        #choice = readCliOption()        
        
except Exception:
    print ("Error while reading parquet file")
    traceback.print_exc()





# COMMAND ----------

# COMMAND ----------

choice = dbutils.widgets.get("X")
df.iloc[i, zIsMatchIndex] = choice
df.iloc[i+1, zIsMatchIndex] = choice

# markedRecords = pd.concat([markedRecords, df.iloc[i]], axis = 0)
# markedRecords = pd.concat([markedRecords, df.iloc[i+1]], axis = 0)
markedRecords = markedRecords.append(df.iloc[i], ignore_index=True)
markedRecords = markedRecords.append(df.iloc[i+1], ignore_index=True)
#print(markedRecords)
i += 2


if i < len(df) :        
    print("Is this pair a Match?\n")
    dis = pd.concat([df.iloc[i], df.iloc[i+1] ], axis = 1)
    printDF(df[i:i+1],dis)
else:
   print("markedRecords: " + str(markedRecords.shape[0]))
   writeLabelledOutput(markedRecords)

    

