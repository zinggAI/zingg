package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ISelectedCols;
import zingg.common.client.cols.PredictionColsSelector;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Blocker;
import zingg.common.core.block.IBlocker;
import zingg.common.core.block.InputDataGetter;
import zingg.common.core.filter.IFilter;
import zingg.common.core.filter.PredictionFilter;
import zingg.common.core.match.data.IDataGetter;
import zingg.common.core.match.output.GraphMatchOutputBuilder;
import zingg.common.core.match.output.IMatchOutputBuilder;
import zingg.common.core.model.Model;
import zingg.common.core.pairs.IPairBuilder;
import zingg.common.core.pairs.SelfPairBuilder;
import zingg.common.core.preprocess.IPreprocessors;
import zingg.common.core.preprocess.stopwords.StopWordsRemover;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.Metric;

public abstract class Matcher<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> implements IPreprocessors<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.Matcher";
	public static final Log LOG = LogFactory.getLog(Matcher.class);   
	protected IMatchOutputBuilder<S,D,R,C> matchOutputBuilder; 
	ZFrame<D, R, C> output = null;
	boolean toWrite = true;
	protected ISelectedCols predictionColsSelector;
	protected IDataGetter<S, D, R, C> dataGetter;
	protected IPairBuilder<S, D, R, C> iPairBuilder;
	protected IFilter<D, R, C> predictionFilter;
	protected IBlocker<S,D,R,C,T> blocker;

	
	public Matcher() {
        setZinggOption(ZinggOptions.MATCH);
		
    }

	@Override 
	public void init(IZArgs args, S session, ClientOptions c) throws ZinggClientException{
		super.init(args, session, c);
	}

	protected IFilter<D, R, C> getPredictionFilter() {
		if (this.predictionFilter == null) {
	 		this.predictionFilter = new PredictionFilter<D, R, C>();
		}
		return predictionFilter;
	}


	public void setPredictionFilter(IFilter<D, R, C> predictionFilter) {
		this.predictionFilter = predictionFilter;
	}


	public ZFrame<D, R, C> getOutput() {
		return output;
	}

	public void setOutput(ZFrame<D, R, C> output) {
		this.output = output;
	}

	public boolean isToWrite() {
		return toWrite;
	}

	public void setToWrite(boolean toWrite) {
		this.toWrite = toWrite;
	}

	public ZFrame<D,R,C> getTestData() throws ZinggClientException{
		return getDataGetter().getData(args, getPipeUtil());
	}

	public void setDataGetter(IDataGetter<S,D,R,C> idg){
		this.dataGetter = idg;
	}

	public IDataGetter<S,D,R,C> getDataGetter(){
		if (dataGetter == null){
			this.dataGetter = new InputDataGetter<S,D,R,C>(getPipeUtil());
		}
		return dataGetter;
	}

	public ZFrame<D, R, C> getFieldDefColumnsDS(ZFrame<D, R, C> testDataOriginal) {
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition());
		return testDataOriginal.select(zidAndFieldDefSelector.getCols());
		//return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
	}

	public ZFrame<D,R,C>  getBlocked(ZFrame<D,R,C>  testData) throws Exception, ZinggClientException{
		return getBlocker().getBlocked(testData,args, getModelHelper(),getBlockingTreeUtil());	
	}

	public IBlocker<S,D,R,C,T> getBlocker(){
		if (blocker == null){
			this.blocker = new Blocker<S,D,R,C,T>(getBlockingTreeUtil());
		}
		return blocker;
	}

	public IPairBuilder<S, D, R, C> getIPairBuilder(){
		if (this.iPairBuilder == null){
			iPairBuilder = new SelfPairBuilder<S, D, R, C> (getDSUtil(),args);
		}
		return iPairBuilder;
	}

	public void setIPairbuilder(IPairBuilder<S, D, R, C> p){
		this.iPairBuilder = p;
	}
	
	public ZFrame<D,R,C> getPairs(ZFrame<D,R,C>blocked, ZFrame<D,R,C>bAll, IPairBuilder<S, D, R, C> iPairBuilder) throws Exception{
		return iPairBuilder.getPairs(blocked, bAll);
	}

	protected abstract Model getModel() throws ZinggClientException;

	protected ZFrame<D,R,C> selectColsFromBlocked(ZFrame<D,R,C>blocked) {
		return blocked.select(ColName.ID_COL, ColName.HASH_COL);
	}

	protected ZFrame<D,R,C> predictOnBlocks(ZFrame<D,R,C>blocks) throws Exception, ZinggClientException{
		if (LOG.isDebugEnabled()) {
				LOG.debug("block size" + blocks.count());
		}
		Model model = getModel();
		ZFrame<D,R,C> dupes = model.predict(blocks); 
		if (LOG.isDebugEnabled()) {
				LOG.debug("Found dupes " + dupes.count());	
		}
		return dupes;
	}

	protected ZFrame<D,R,C> getActualDupes(ZFrame<D,R,C> blocked, ZFrame<D,R,C> testData) throws Exception, ZinggClientException{
		return getActualDupes(blocked, testData, getPredictionFilter(), getIPairBuilder(), getPredictionColsSelector());
	}

	public ISelectedCols getPredictionColsSelector(){
		if (predictionColsSelector == null) {
			this.predictionColsSelector = new PredictionColsSelector();
		}
		return predictionColsSelector;
	}

	public void setPredictionColsSelector(ISelectedCols s){
		this.predictionColsSelector = s;
	}

	protected ZFrame<D,R,C> getActualDupes(ZFrame<D,R,C> blocked, ZFrame<D,R,C> testData, IFilter<D, R, C> predictionFilter, IPairBuilder<S, D, R, C> iPairBuilder, ISelectedCols colsSelector) throws Exception, ZinggClientException{
		ZFrame<D,R,C> blocks = getPairs(blocked, testData, iPairBuilder);
		ZFrame<D,R,C>dupesActual = predictOnBlocks(blocks); 
		ZFrame<D, R, C> filteredData = predictionFilter.filter(dupesActual);
		if(colsSelector!=null) {
			filteredData = filteredData.select(colsSelector.getCols());
		}
		return filteredData;
	}
	
	@Override
    public void execute() throws ZinggClientException {
        try {
			// read input, filter, remove self joins
			ZFrame<D,R,C>  testDataOriginal = getTestData();
			testDataOriginal =  getFieldDefColumnsDS(testDataOriginal).cache();
			ZFrame<D,R,C>  testData = preprocess(testDataOriginal);
			//testData = testData.repartition(args.getNumPartitions(), testData.col(ColName.ID_COL));
			//testData = dropDuplicates(testData);
			long count = testData.count();
			LOG.info("Read " + count);
			Analytics.track(Metric.DATA_COUNT, count, args.getCollectMetrics());

			ZFrame<D,R,C>blocked = getBlocked(testData);
			LOG.info("Blocked ");
			if (LOG.isDebugEnabled()) {
				LOG.debug("Num distinct hashes " + blocked.select(ColName.HASH_COL).distinct().count());
				blocked.show();
			}
			//LOG.warn("Num distinct hashes " + blocked.agg(functions.approx_count_distinct(ColName.HASH_COL)).count());
			ZFrame<D,R,C> dupesActual = getActualDupes(blocked, testData);
			
			//dupesActual.explain();
			//dupesActual.toJavaRDD().saveAsTextFile("/tmp/zdupes");
			
			writeOutput(testDataOriginal, dupesActual);		
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

	public void setMatchOutputBuilder(IMatchOutputBuilder<S,D,R,C> o){
		this.matchOutputBuilder = o;
	}

	public IMatchOutputBuilder<S,D,R,C> getMatchOutputBuilder(){
		if (this.matchOutputBuilder == null) {
			this.matchOutputBuilder = new GraphMatchOutputBuilder<S,D,R,C>(getGraphUtil(), getDSUtil(), (IArguments) args);
		}
		return this.matchOutputBuilder;
	}

	
	public void writeOutput( ZFrame<D,R,C>  testDataOriginal,  ZFrame<D,R,C>  dupesActual) throws ZinggClientException {
		try{
		//input dupes are pairs
		///pick ones according to the threshold by user
		//all clusters consolidated in one place
		ZFrame<D, R, C> graphWithScores = getMatchOutputBuilder().getOutput(testDataOriginal, dupesActual);
		setOutput(graphWithScores);
		if (args.getOutput() != null && toWrite) {
				getPipeUtil().write(graphWithScores, args.getOutput());
		}
		}
		catch(Exception e) {
			e.printStackTrace(); 
		}
		
	}

	
    protected abstract StopWordsRemover<S,D,R,C,T> getStopWords();
	

	    
}
