package zingg.common.client;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Util methods related to FieldDefinition objects
 *
 */
public class FieldDefUtil implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public List<? extends FieldDefinition> getFieldDefinitionDontUse(List<? extends FieldDefinition> fieldDefinition) {
		return fieldDefinition.stream()
			    .filter(x->x.matchType.contains(MatchTypes.DONT_USE))
			    .collect(Collectors.toList());
	}
	
	public List<? extends FieldDefinition> getFieldDefinitionToUse(List<? extends FieldDefinition> fieldDefinition) {
		return fieldDefinition.stream()
			    .filter(x->!x.matchType.contains(MatchTypes.DONT_USE))
			    .collect(Collectors.toList());
	}
	


}
