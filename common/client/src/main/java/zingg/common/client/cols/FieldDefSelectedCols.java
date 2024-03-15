package zingg.common.client.cols;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;

public class FieldDefSelectedCols extends SelectedCols {

	protected FieldDefSelectedCols() {
		
	}
	
    public FieldDefSelectedCols(List<? extends FieldDefinition> fieldDefs, boolean showConcise) {
        List<String> colList = getColList(fieldDefs, showConcise);
        setCols(colList);
    }
    
	protected List<String> getColList(List<? extends FieldDefinition> fieldDefs) {
		return getColList(fieldDefs,false);
	}

	protected List<String> getColList(List<? extends FieldDefinition> fieldDefs, boolean showConcise) {
		List<FieldDefinition> namedList = new ArrayList<FieldDefinition>();

        for (FieldDefinition fieldDef : fieldDefs) {
            if (showConcise && fieldDef.isDontUse()) {
                continue;
            }
            namedList.add(fieldDef);
        }
        List<String> stringList = convertNamedListToStringList(namedList);
		return stringList;
	}

	protected List<String> convertNamedListToStringList(List<? extends FieldDefinition> namedList) {
        List<String> stringList = new ArrayList<String>();
        for (FieldDefinition named : namedList) {
            stringList.add(named.getName());
        }
        return stringList;
    }
}