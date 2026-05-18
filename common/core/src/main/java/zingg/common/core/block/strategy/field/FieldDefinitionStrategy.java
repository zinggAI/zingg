package zingg.common.core.block.strategy.field;

import zingg.common.client.FieldDefinition;
import zingg.common.core.block.Canopy;

import java.util.List;

public interface FieldDefinitionStrategy<R> {
    List<? extends FieldDefinition> getAdjustedFieldDefinitions(List<? extends FieldDefinition> fieldDefinitions, Canopy<R> node);
}
