from zingg.zinggGenerated.client import *
from zingg.otherThanGeneratedFieldDefinition import *

class ExtendedArgumentsGenerated(Arguments):
    def __init__(self):
        super().__init__()
    
    def setFieldDefinition(self, fieldDef):
        javaFieldDef = []
        for f in fieldDef:
            javaFieldDef.append(f.getFieldDefinition())
        self.arguments.setFieldDefinition(javaFieldDef)
    
    def setData(self, *pipes):
        dataPipe = getGateway().new_array(getJVM().zingg.common.client.pipe.Pipe, len(pipes))
        for idx, pipe in enumerate(pipes):
            dataPipe[idx] = pipe.getPipe()
        self.arguments.setData(dataPipe)
    
    def setOutput(self, *pipes):
        outputPipe = getGateway().new_array(getJVM().zingg.common.client.pipe.Pipe, len(pipes))
        for idx, pipe in enumerate(pipes):
            outputPipe[idx] = pipe.getPipe()
        self.arguments.setOutput(outputPipe)
    
    def getArgs(self):
        return self.arguments
    
    def setTrainingSamples(self, *pipes):
        dataPipe = getGateway().new_array(getJVM().zingg.common.client.pipe.Pipe, len(pipes))
        for idx, pipe in enumerate(pipes):
            dataPipe[idx] = pipe.getPipe()
        self.arguments.setTrainingSamples(dataPipe)
        
    def writeArgumentsToJSON(self, fileName):
        getJVM().zingg.common.client.ArgumentsUtil().writeArgumentsToJSON(fileName, self.arguments)
    
    @staticmethod
    def createArgumentsFromJSON(fileName, phase):
        obj = Arguments()
        obj.arguments = getJVM().zingg.common.client.ArgumentsUtil().createArgumentsFromJSON(fileName, phase)
        return obj
    
    def writeArgumentsToJSONString(self):
        return getJVM().zingg.common.client.ArgumentsUtil().writeArgumentstoJSONString(self.arguments)
    
    @staticmethod
    def createArgumentsFromJSONString(jsonArgs, phase):
        obj = Arguments()
        obj.arguments = getJVM().zingg.common.client.ArgumentsUtil().createArgumentsFromJSONString(jsonArgs, phase)
        return obj
    
    def copyArgs(self, phase):
        argsString = self.writeArgumentsToJSONString()
        return self.createArgumentsFromJSONString(argsString, phase)
    