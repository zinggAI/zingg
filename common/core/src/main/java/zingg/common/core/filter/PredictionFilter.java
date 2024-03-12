package zingg.common.core.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.cols.PredictionColsSelector;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;

public class PredictionFilter<D, R, C> implements IFilter<D, R, C> {

	public static final Log LOG = LogFactory.getLog(PredictionFilter.class);   
	
	protected PredictionColsSelector colsSelector;
	
	public PredictionFilter() {
		super();
	}
	
	public PredictionFilter(PredictionColsSelector colsSelector) {
		super();
		this.colsSelector = colsSelector;
	}

	@Override
	public ZFrame<D, R, C> filter(ZFrame<D, R, C> dupes) {		
		dupes = filterMatches(dupes);		
		dupes = selectCols(dupes);
		return dupes;
	}

	protected ZFrame<D, R, C> selectCols(ZFrame<D, R, C> dupes) {
		if(colsSelector!=null) {
			dupes = dupes.select(colsSelector.getCols());
		}
		return dupes;
	}

	protected ZFrame<D, R, C> filterMatches(ZFrame<D, R, C> dupes) {
		dupes = dupes.filter(dupes.equalTo(ColName.PREDICTION_COL,ColValues.IS_MATCH_PREDICTION));
		return dupes;
	}

}
