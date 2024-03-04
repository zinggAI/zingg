package zingg.common.client.cols;

import java.util.Arrays;
import java.util.List;

import zingg.common.client.util.ColName;

public class ZidAndFieldDefSelector extends SelectedCols {

    public ZidAndFieldDefSelector(String[] fieldDefs) {

        List<String> fieldDefList = Arrays.asList(fieldDefs);
        fieldDefList.add(0, ColName.ID_COL);
        setCols(fieldDefList);
    }
}