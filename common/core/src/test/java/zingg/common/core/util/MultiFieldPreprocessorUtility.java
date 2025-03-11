package zingg.common.core.util;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;

public abstract class MultiFieldPreprocessorUtility<S, D, R, C, T> {
    
    protected List<List<FieldDefinition>> multiFieldsList;

    public MultiFieldPreprocessorUtility() throws ZinggClientException {
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

        addFieldDefinitionsList(List.of(fieldDefinition1, fieldDefinition2, fieldDefinition3));
        addFieldDefinitionsList(List.of(fieldDefinition1));
        addFieldDefinitionsList(null);
        addFieldDefinitionsList(List.of(fieldDefinition4, fieldDefinition2, fieldDefinition5));

    }

    public List<List<FieldDefinition>> getFieldDefinitions() {
        return this.multiFieldsList;
    }

    protected abstract void addFieldDefinitionsList(List<FieldDefinition> fd);
}
