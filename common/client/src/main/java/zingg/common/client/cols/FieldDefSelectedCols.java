package zingg.common.client.cols;

import java.util.ArrayList;
import java.util.List;

public class FieldDefSelectedCols extends SelectedCols {
    public FieldDefSelectedCols(List<FieldDefinition> fieldDefs, boolean showConcise) {
        List<Named> namedList = new ArrayList<>();
        for (FieldDefinition fieldDef : fieldDefs) {
            if (showConcise && fieldDef.isDontUse()) {
                continue;
            }
            namedList.add(fieldDef);
        }
        namedList.add(new Named("source"));
        setCols(namedList);
    }
}
