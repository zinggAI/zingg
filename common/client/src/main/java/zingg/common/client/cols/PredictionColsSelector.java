package zingg.common.client.cols;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.util.ColName;

public class PredictionColsSelector extends SelectedCols {

	public PredictionColsSelector() {
		
		List<String> cols = new ArrayList<String>();
		cols.add(ColName.ID_COL);
		cols.add(ColName.COL_PREFIX + ColName.ID_COL);
		cols.add(ColName.PREDICTION_COL);
		cols.add(ColName.SCORE_COL);
		
		setCols(cols);
		
	}	
	

}