from zingg.otherThanGenerated import *
class Arguments:
    def __init__(self):
        self.arguments = getJVM().zingg.common.client.Arguments()

    def setNumPartitions(self, numPartitions):
        self.arguments.setNumPartitions(numPartitions)

    def setLabelDataSampleSize(self, labelDataSampleSize):
        self.arguments.setLabelDataSampleSize(labelDataSampleSize)

    def getModelId(self):
        return self.arguments.getModelId()

    def setModelId(self, modelId):
        self.arguments.setModelId(modelId)

    def setOutput(self, outputDir):
        self.arguments.setOutput(outputDir)

    def setZinggDir(self, zinggDir):
        self.arguments.setZinggDir(zinggDir)

    def getZinggBaseModelDir(self):
        return self.arguments.getZinggBaseModelDir()

    def getZinggModelDir(self):
        return self.arguments.getZinggModelDir()

    def getZinggBaseTrainingDataDir(self):
        return self.arguments.getZinggBaseTrainingDataDir()

    def getZinggTrainingDataUnmarkedDir(self):
        return self.arguments.getZinggTrainingDataUnmarkedDir()

    def getZinggTrainingDataMarkedDir(self):
        return self.arguments.getZinggTrainingDataMarkedDir()

    def setStopWordsCutoff(self, stopWordsCutoff):
        self.arguments.setStopWordsCutoff(stopWordsCutoff)

    def setColumn(self, column):
        self.arguments.setColumn(column)

