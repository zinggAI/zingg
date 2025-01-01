package zingg.common.core.block;

import zingg.common.client.FieldDefinition;

import java.util.List;

public class DefaultFieldDefinitionStrategy<R> implements FieldDefinitionStrategy<R> {
    @Override
    public List<FieldDefinition> getAdjustedFieldDefinitions(List<FieldDefinition> fieldDefinitions, Canopy<R> node) {
        //returning fieldDefinitions
        //as it is here
        return fieldDefinitions;
    }
}
