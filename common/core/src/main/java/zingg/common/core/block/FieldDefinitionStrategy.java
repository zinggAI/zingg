package zingg.common.core.block;

import zingg.common.client.FieldDefinition;

import java.util.List;

public interface FieldDefinitionStrategy<R> {
    List<? extends FieldDefinition> getAdjustedFieldDefinitions(List<? extends FieldDefinition> fieldDefinitions, Canopy<R> node);
}
