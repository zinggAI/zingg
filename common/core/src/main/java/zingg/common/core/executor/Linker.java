package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.data.BlockedData;
import zingg.common.client.data.IData;
import zingg.common.client.data.LinkInputData;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.filter.PredictionFilter;
import zingg.common.core.match.output.IMatchOutputBuilder;
import zingg.common.core.match.output.LinkOutputBuilder;
import zingg.common.core.pairs.IPairBuilder;
import zingg.common.core.pairs.SelfPairBuilderSourceSensitive;

import java.util.Arrays;


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
	protected ZFrame<D,R,C> getActualDupes(BlockedData<D,R,C>[] blocked, IData<D,R,C> testData) throws Exception, ZinggClientException{
		PredictionFilter<D, R, C> predictionFilter = new PredictionFilter<D, R, C>();
		return getActualDupes(blocked, testData,predictionFilter, getIPairBuilder(), null);
	}	
	
	@Override
	public IMatchOutputBuilder<S,D,R,C> getMatchOutputBuilder(){
		if (this.matchOutputBuilder == null) {
			this.matchOutputBuilder = new LinkOutputBuilder<S,D,R,C>(getDSUtil(), args);
		}
		return this.matchOutputBuilder;
	}

	public ZFrame<D,R,C> getClusterAdjustedDF(ZFrame<D, R, C> dupes) {
		//no adjustment required here
		//some class extending it may adjust cluster like adding guid etc
		return dupes;
	}

	@Override
	public IPairBuilder<S, D, R, C> getIPairBuilder(){
		if (this.iPairBuilder == null){
			iPairBuilder = new SelfPairBuilderSourceSensitive<S, D, R, C> (getDSUtil(),args);
		}
		return iPairBuilder;
	}

	@Override
	protected IData<D, R, C> getPreprocessedInputData(IData<D, R, C> inputData) throws ZinggClientException {
		ZFrame<D, R, C> primaryInput = ((LinkInputData<D, R, C>)inputData).getPrimaryInput();
		ZFrame<D, R, C> secondaryInput = ((LinkInputData<D, R, C>)inputData).getSecondaryInput();
		primaryInput = preprocess(primaryInput);
		secondaryInput = preprocess(secondaryInput);
		return new LinkInputData<D, R, C>(Arrays.asList(primaryInput, secondaryInput));
	}

	@Override
	protected IData<D, R, C> getFieldDefColumnsDF(IData<D, R, C> inputData) throws ZinggClientException {
		ZFrame<D, R, C> primaryInput = ((LinkInputData<D, R, C>)inputData).getPrimaryInput();
		ZFrame<D, R, C> secondaryInput = ((LinkInputData<D, R, C>)inputData).getSecondaryInput();
		primaryInput = getFieldDefColumnsDS(primaryInput);
		secondaryInput = getFieldDefColumnsDS(secondaryInput);
		return new LinkInputData<D, R, C>(Arrays.asList(primaryInput, secondaryInput));
	}
}
