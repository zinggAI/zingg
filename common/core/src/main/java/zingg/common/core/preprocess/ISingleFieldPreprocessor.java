package zingg.common.core.preprocess;

import zingg.common.client.FieldDefinition;

public interface ISingleFieldPreprocessor<S, D, R, C, T> extends IPreprocessor<S, D, R, C, T> {
    void setFieldDefinition(FieldDefinition fd);
    FieldDefinition getFieldDefinition();
}
