package zingg.common.client.cols;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.FieldDefinition;

public class FieldDefSelectedCols extends SelectedCols {

    public FieldDefSelectedCols(List<FieldDefinition> fieldDefs, boolean showConcise) {

        List<FieldDefinition> namedList = new ArrayList<>();

        for (FieldDefinition fieldDef : fieldDefs) {
            if (showConcise && fieldDef.isDontUse()) {
                continue;
            }
            namedList.add(fieldDef);
        }

        namedList.add(new FieldDefinition());
        List<String> stringList = convertNamedListToStringList(namedList);
        setCols(stringList);
    }

    private List<String> convertNamedListToStringList(List<FieldDefinition> namedList) {
        List<String> stringList = new ArrayList<>();
        for (FieldDefinition named : namedList) {
            stringList.add(named.getName());
        }
        return stringList;
    }
}