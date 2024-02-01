from zingg.otherThanGenerated import *
class FieldDefinition:
    def __init__(self, name, dataType, *matchType):
        self.fielddefinition = getJVM().zingg.common.client.FieldDefinition()
        self.fielddefinition.setFieldName(name)
        self.fielddefinition.setDataType(self.stringify(dataType))
        self.fielddefinition.setMatchType(matchType)
        self.fielddefinition.setFields(name)

    def getFieldDefinition(self):
        return self.fielddefinition

    def setFields(self, fields):
        self.fielddefinition.setFields(fields)

    def setMatchType(self, type):
        self.fielddefinition.setMatchType(type)

    def setStopWords(self, stopWords):
        self.fielddefinition.setStopWords(stopWords)

    def setFieldName(self, fieldName):
        self.fielddefinition.setFieldName(fieldName)

