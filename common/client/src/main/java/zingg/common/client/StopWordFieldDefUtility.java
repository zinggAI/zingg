package zingg.common.client;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class StopWordFieldDefUtility implements Serializable {

    private static final long serialVersionUID = 1L;

    public  List<? extends FieldDefinition> getFieldDefinitionWithStopwords(List<? extends FieldDefinition> fieldDefinition) {
		return fieldDefinition.stream()
				.filter(f -> !(f.getStopWords() == null || f.getStopWords() == ""))
				.collect(Collectors.toList());
	}
    
}
