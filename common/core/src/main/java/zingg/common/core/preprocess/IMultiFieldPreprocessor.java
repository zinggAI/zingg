package zingg.common.core.preprocess;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;

import java.util.ArrayList;
import java.util.List;

public interface IMultiFieldPreprocessor<S, D, R, C, T> extends IPreprocessor<S, D, R, C, T> {

    void setFieldDefinitions(List<?  extends FieldDefinition> fieldDefinitions);
    
    List<? extends FieldDefinition> getFieldDefinitions();

    default List<String> getRelevantFields(String type) {
        List<String> stringFields = new ArrayList<>();
        for (FieldDefinition fieldDefinition : getFieldDefinitions()) {
            if (fieldDefinition.dataType != null && fieldDefinition.matchType != null &&
                    fieldDefinition.dataType.equalsIgnoreCase(type) && !fieldDefinition.matchType.contains(MatchTypes.DONT_USE)) {
                stringFields.add(fieldDefinition.fieldName);
            }
        }
        return stringFields;
    }
}
