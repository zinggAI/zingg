package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.filter.PredictionFilter;
import zingg.common.core.pairs.SelfPairBuilderSourceSensitive;



public abstract class Linker<S,D,R,C,T> extends Matcher<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.Linker";
	public static final Log LOG = LogFactory.getLog(Linker.class);

	public Linker() {
		setZinggOption(ZinggOptions.LINK);
	}
	
	@Override
	public ZFrame<D,R,C> selectColsFromBlocked(ZFrame<D,R,C> blocked) {
		return blocked;
	}
	
	@Override
	public ZFrame<D,R,C> getPairs(ZFrame<D,R,C>blocked, ZFrame<D,R,C>bAll) throws Exception{
		return getPairs(blocked, bAll, new SelfPairBuilderSourceSensitive<S, D, R, C> (getDSUtil(),args));
	}	

	@Override
	protected ZFrame<D,R,C> getActualDupes(ZFrame<D,R,C> blocked, ZFrame<D,R,C> testData) throws Exception, ZinggClientException{
		// input dupes are pairs
		/// pick ones according to the threshold by user
			
		PredictionFilter<D, R, C> predictionFilter = new PredictionFilter<D, R, C>();
		SelfPairBuilderSourceSensitive<S, D, R, C> iPairBuilder = new SelfPairBuilderSourceSensitive<S, D, R, C> (getDSUtil(),args);
		return getActualDupes(blocked, testData,predictionFilter, iPairBuilder, null);
	}
		
	

}
