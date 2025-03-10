package zingg.common.core.preprocess;

import zingg.common.client.FieldDefinition;

import java.util.List;

public interface IMultiFieldPreprocessor<S, D, R, C, T> extends IPreprocessor<S, D, R, C, T> {

    void setFieldDefinitions(List<?  extends FieldDefinition> fieldDefinitions);
    
    List<? extends FieldDefinition> getFieldDefinitions();
}
