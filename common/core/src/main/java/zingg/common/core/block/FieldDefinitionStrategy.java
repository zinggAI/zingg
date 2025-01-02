package zingg.common.core.block;

import zingg.common.client.FieldDefinition;

import java.util.List;

public interface FieldDefinitionStrategy<R> {
    List<FieldDefinition> getAdjustedFieldDefinitions(List<FieldDefinition> fieldDefinitions, Canopy<R> node);
}
