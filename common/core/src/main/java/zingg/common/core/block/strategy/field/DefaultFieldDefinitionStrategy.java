package zingg.common.core.block.strategy.field;

import zingg.common.client.FieldDefinition;
import zingg.common.core.block.Canopy;

import java.util.List;

public class DefaultFieldDefinitionStrategy<R> implements FieldDefinitionStrategy<R> {
    @Override
    public List<? extends FieldDefinition> getAdjustedFieldDefinitions(List<? extends FieldDefinition> fieldDefinitions, Canopy<R> node) {
        //returning fieldDefinitions
        //as it is here
        return fieldDefinitions;
    }
}
