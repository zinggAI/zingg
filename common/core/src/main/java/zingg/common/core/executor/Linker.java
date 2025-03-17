package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.model.IInputData;
import zingg.common.client.model.LinkInputData;
import zingg.common.client.model.MatchInputData;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.filter.PredictionFilter;
import zingg.common.core.match.data.IDataGetter;
import zingg.common.core.match.output.IMatchOutputBuilder;
import zingg.common.core.match.output.LinkOutputBuilder;
import zingg.common.core.pairs.IPairBuilder;
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
	protected ZFrame<D,R,C> getActualDupes(IInputData<D,R,C> blocked, IInputData<D,R,C> testData) throws Exception, ZinggClientException{
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
	public IDataGetter<S,D,R,C> getDataGetter(){
		if (dataGetter == null){
			this.dataGetter = new LinkDataGetter<S, D, R, C>();
		}
		return dataGetter;
	}

	@Override
	public IInputData<D, R, C> getBlocked(IInputData<D, R, C> testData) throws Exception, ZinggClientException {
		ZFrame<D, R, C> blockedInputOneData = getBlocker().getBlocked(((LinkInputData<D, R, C>)testData).getInputOne(),args, getModelHelper(),getBlockingTreeUtil());
		ZFrame<D, R, C> blockedInputTwoData = getBlocker().getBlocked(((LinkInputData<D, R, C>)testData).getInputTwo(),args, getModelHelper(),getBlockingTreeUtil());
		return new LinkInputData<>(blockedInputOneData, blockedInputTwoData);
	}

	@Override
	protected IInputData<D, R, C> getPreprocessedInputData(IInputData<D, R, C> testDataOriginal) throws ZinggClientException {
		ZFrame<D, R, C> inputOneData = ((LinkInputData<D, R, C>)testDataOriginal).getInputOne();
		ZFrame<D, R, C> inputTwoData = ((LinkInputData<D, R, C>)testDataOriginal).getInputTwo();
		inputOneData =  getFieldDefColumnsDS(inputOneData).cache();
		inputTwoData =  getFieldDefColumnsDS(inputTwoData).cache();
		ZFrame<D,R,C>  testInputOneData = preprocess(inputOneData);
		ZFrame<D,R,C>  testInputTwoData = preprocess(inputTwoData);
		return new LinkInputData<D, R, C>(testInputOneData, testInputTwoData);
	}
}
