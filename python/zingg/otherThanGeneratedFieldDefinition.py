from zingg.zinggGenerated.client import *

class ExtendedFieldDefinitionGenerated(FieldDefinition):
    def __init__(self, name, dataType, *matchType):
        super().__init__(name, dataType, *matchType)
    
    def getFieldDefinition(self):
        return self.fielddefinition
    
    #  should be stringify'ed before it is set in fd object
    def stringify(self, str):
        """ Method to stringify'ed the dataType before it is set in FieldDefinition object
        
        :param str: dataType of the FieldDefinition
        :type str: String
        :return: The stringify'ed value of the dataType
        :rtype: String
        """
        
        return str