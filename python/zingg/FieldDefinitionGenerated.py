from zingg.otherThanGenerated import *
'''
This class defines each field that we use in matching We can use this to
 configure the properties of each field we use for matching in Zingg.
 
 @author sgoyal
'''
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

    '''
Set the field type which defines the kind of matching we want to do
 
 @see MatchType
 @param type
            the type to set
    '''
    def setMatchType(self, type):
        self.fielddefinition.setMatchType(type)

    def setStopWords(self, stopWords):
        self.fielddefinition.setStopWords(stopWords)

    def setFieldName(self, fieldName):
        self.fielddefinition.setFieldName(fieldName)

