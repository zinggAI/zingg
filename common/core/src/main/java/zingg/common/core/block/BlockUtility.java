package zingg.common.core.block;

import zingg.common.client.FieldDefinition;

import java.util.List;

public class BlockUtility<R> {
    public int getStartIndexToIterateOverFields(Canopy<R>best, List<FieldDefinition> fieldsOfInterest) {
        FieldDefinition bestFieldDefinition = best.getContext();
        for (int idx = 0; idx < fieldsOfInterest.size(); idx++) {
            if (fieldsOfInterest.get(idx).equals(bestFieldDefinition)) {
                return (idx + 1) % fieldsOfInterest.size();
            }
        }
        return -1;
    }
}
