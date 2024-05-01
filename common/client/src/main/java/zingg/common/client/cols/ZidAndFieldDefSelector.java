package zingg.common.client.cols;

import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.util.ColName;

public class ZidAndFieldDefSelector extends FieldDefSelectedCols {

	public ZidAndFieldDefSelector(List<? extends FieldDefinition> fieldDefs) {
		this(fieldDefs, true, false);
	}	
	
	public ZidAndFieldDefSelector(List<? extends FieldDefinition> fieldDefs, boolean includeZid, boolean showConcise) {
		List<String> colList = getColList(fieldDefs, showConcise);
		
		if (includeZid) colList.add(0, ColName.ID_COL);
		
		colList.add(ColName.SOURCE_COL);		
		
		setCols(colList);
	}

}