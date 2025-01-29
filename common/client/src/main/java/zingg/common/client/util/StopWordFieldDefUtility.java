package zingg.common.client.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import zingg.common.client.FieldDefinition;
import zingg.common.client.HasStopWords;
import zingg.common.client.IArguments;

public class StopWordFieldDefUtility implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<? extends FieldDefinition> getFieldDefinitionWithStopwords(List<? extends FieldDefinition> fieldDefinition) {
		return fieldDefinition.stream()
				.filter(f -> HasStopWords.isStopwordField(f))
				.collect(Collectors.toList());
	}

	public  String getFieldDefinitionWithStopwords(IArguments args) {
		List<FieldDefinition> list = args.getFieldDefinition()
										.stream()
										.filter(f -> HasStopWords.isStopwordField(f))
										.collect(Collectors.toList());

		List<String> fieldNamesList = new ArrayList<String>();	
		for(FieldDefinition fd: list){
			fieldNamesList.add(fd.getName());
		}			
						
		String fieldNames = fieldNamesList.stream()
								.collect(Collectors.joining(", "));
		return fieldNames;
	}
    
}
