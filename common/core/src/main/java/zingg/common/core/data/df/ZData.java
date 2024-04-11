package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.context.Context;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.Metric;

public class ZFrameDataSelector<S, D, R, C, T> {

	protected ZFrame<D,R,C> rawData;
	protected ZFrame<D,R,C> fieldDefColumnsDS;
	protected ZFrame<D,R,C> blockedData;
	protected IArguments args;
	protected Context<S,D,R,C,T> context;
	protected StopWordsRemover<S, D, R, C, T> stopWordsRemover;
	protected ZFrame<D, R, C> preprocessedRepartitionedData;
	
	public static final Log LOG = LogFactory.getLog(ZFrameDataSelector.class);   
	
	public ZFrameDataSelector(ZFrame<D, R, C> rawData, IArguments args, Context<S,D,R,C,T> context,StopWordsRemover<S, D, R, C, T> stopWordsRemover) {
		super();
		this.rawData = rawData;
		this.args = args;
		this.context = context;
		this.stopWordsRemover = stopWordsRemover;
	}

	public ZFrame<D, R, C> getRawData() {
		return rawData;
	}

	public ZFrame<D, R, C> getFieldDefColumnsDS() {
		if (fieldDefColumnsDS==null) {
			ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition());
			fieldDefColumnsDS = getRawData().select(zidAndFieldDefSelector.getCols());
		}
		return fieldDefColumnsDS;
//		return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
	}
	
	public ZFrame<D, R, C> getPreprocessedRepartitionedData() {
		return preprocessedRepartitionedData;
	}

	public ZFrame<D,R,C>  getBlocked() throws Exception, ZinggClientException{
		if (blockedData==null) {
			preprocessedRepartitionedData = stopWordsRemover.preprocessForStopWords(getFieldDefColumnsDS());
			preprocessedRepartitionedData = preprocessedRepartitionedData.repartition(args.getNumPartitions(), preprocessedRepartitionedData.col(ColName.ID_COL));
			//testData = dropDuplicates(testData);
			long count = preprocessedRepartitionedData.count();
			LOG.info("Read " + count);
			Analytics.track(Metric.DATA_COUNT, count, args.getCollectMetrics());
			LOG.debug("Blocking model file location is " + args.getBlockFile());
			ZFrame<D, R, C> blocked1; //.cache();
			Tree<Canopy<R>> tree = context.getBlockingTreeUtil().readBlockingTree(args);
			ZFrame<D, R, C> blocked = context.getBlockingTreeUtil().getBlockHashes(preprocessedRepartitionedData, tree);
			blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL));
			this.blockedData = blocked1;
		}
		return blockedData;
	}

	
	
}
