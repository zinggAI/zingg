package zingg.common.core.util;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;

public abstract class AMultiFieldPreprocessorUtility<S, D, R, C, T> {
    
    protected List<List<FieldDefinition>> multiFieldsList;

    public AMultiFieldPreprocessorUtility() throws ZinggClientException {
        this.multiFieldsList = new ArrayList<List<FieldDefinition>>();
    }

    public void buildFieldDefinitions() throws ZinggClientException{

        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setFieldName("field1");
        fieldDefinition1.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition1.setDataType("STRING");

        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setFieldName("field2");
        fieldDefinition2.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition2.setDataType("STRING");

        FieldDefinition fieldDefinition3 = new FieldDefinition();
        fieldDefinition3.setFieldName("field3");
        fieldDefinition3.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition3.setDataType("STRING");

        FieldDefinition fieldDefinition4 = new FieldDefinition();
        fieldDefinition4.setFieldName("field1");
        fieldDefinition4.setMatchType(List.of(MatchTypes.DONT_USE));
        fieldDefinition4.setDataType("STRING");

        FieldDefinition fieldDefinition5 = new FieldDefinition();
        fieldDefinition5.setFieldName("field3");
        fieldDefinition5.setMatchType(List.of(MatchTypes.DONT_USE));
        fieldDefinition5.setDataType("STRING");

        FieldDefinition fieldDefinition6 = new FieldDefinition();
        fieldDefinition6.setFieldName("field3");
        fieldDefinition6.setMatchType(List.of(MatchTypes.FUZZY));
        fieldDefinition6.setDataType("INTEGER");

        addFieldDefinitionsList(List.of(fieldDefinition1, fieldDefinition2, fieldDefinition3));
        addFieldDefinitionsList(List.of(fieldDefinition1));
        addFieldDefinitionsList(List.of(fieldDefinition6));
        addFieldDefinitionsList(List.of(fieldDefinition4, fieldDefinition2, fieldDefinition5));

    }

    public List<FieldDefinition> getFieldDefinitionsWhenAllFieldsString(){
        //fieldDefinition1,fieldDefinition2,fieldDefinition3 - all are included in preprocess of datatype String
        return multiFieldsList.get(0);
    }

    public List<FieldDefinition> getFieldDefinitionsWhenSomeFieldsString(){
        //fieldDefinition1 - preprocess is only applied to field1 of datatype String
        return multiFieldsList.get(1);
    }

    public List<FieldDefinition> getFieldDefinitionsWhenNoneFieldsString(){
        //preprocess is applied to none of the fields because their datatype is not string
        return multiFieldsList.get(2);
    }

    public List<FieldDefinition> getFieldDefinitionsWhenSingleFieldStringAndDont_UseMatchType(){
        //fieldDefinition4, fieldDefinition2, fieldDefinition5 - preprocess is only applied to field2 of datatype String
        //field1 and field3 - they are of datatype String but are not being used for preprocess
        return multiFieldsList.get(3);
    }

    protected abstract void addFieldDefinitionsList(List<FieldDefinition> fd);
}
