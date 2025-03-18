package zingg.common.core.preprocess;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZFrame;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public interface IMultiFieldPreprocessor<S, D, R, C, T> extends IPreprocessor<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(IMultiFieldPreprocessor.class);

    void setFieldDefinitions(List<?  extends FieldDefinition> fieldDefinitions);
    
    List<? extends FieldDefinition> getFieldDefinitions();

    public ZFrame<D, R, C> applyPreprocessor(ZFrame<D, R, C> incomingDataFrame, List<String> relevantFields);

    default List<String> getRelevantFields(String type) {
        List<String> stringFields = new ArrayList<String>();
        for (FieldDefinition fieldDefinition : getFieldDefinitions()) {
            if (fieldDefinition.dataType != null && fieldDefinition.matchType != null &&
                    fieldDefinition.dataType.equalsIgnoreCase(type) && !fieldDefinition.matchType.contains(MatchTypes.DONT_USE)) {
                stringFields.add(fieldDefinition.fieldName);
            }
        }
        return stringFields;
    }

}
