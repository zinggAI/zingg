package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.data.df.ZFrameDataSelector;
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
		PredictionFilter<D, R, C> predictionFilter = new PredictionFilter<D, R, C>();
		SelfPairBuilderSourceSensitive<S, D, R, C> iPairBuilder = new SelfPairBuilderSourceSensitive<S, D, R, C> (getDSUtil(),args);
		return getActualDupes(blocked, testData,predictionFilter, iPairBuilder, null);
	}
		
	@Override
	public void writeOutput( ZFrameDataSelector<S,D,R,C,T>  rawData,  ZFrame<D,R,C> dupes) throws ZinggClientException {
		try {
			// input dupes are pairs
			/// pick ones according to the threshold by user
			PredictionFilter<D, R, C> predictionFilter = new PredictionFilter<D, R, C>();
			ZFrame<D,R,C> dupesActual = predictionFilter.filter(dupes);

			// all clusters consolidated in one place
			if (args.getOutput() != null) {

				// input dupes are pairs
				//dupesActual = DFUtil.addClusterRowNumber(dupesActual, spark);
				dupesActual = dupesActual.withColumn(ColName.CLUSTER_COLUMN, dupesActual.col(ColName.ID_COL));
				dupesActual = getDSUtil().addUniqueCol(dupesActual, ColName.CLUSTER_COLUMN);
				ZFrame<D,R,C>dupes2 =  getDSUtil().alignLinked(dupesActual, args);
				dupes2 =  getDSUtil().postprocessLinked(dupes2, rawData.getFieldDefColumnsDS());
				LOG.debug("uncertain output schema is " + dupes2.showSchema());
				getPipeUtil().write(dupes2, args.getOutput());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
