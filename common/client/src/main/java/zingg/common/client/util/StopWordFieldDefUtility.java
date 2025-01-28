package zingg.common.client.util;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import zingg.common.client.FieldDefinition;
import zingg.common.client.HasStopWords;

public class StopWordFieldDefUtility implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<? extends FieldDefinition> getFieldDefinitionWithStopwords(List<? extends FieldDefinition> fieldDefinition) {
		return fieldDefinition.stream()
				.filter(f -> HasStopWords.isStopwordField(f))
				.collect(Collectors.toList());
	}
    
}
