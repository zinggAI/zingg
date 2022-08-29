package zingg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.client.Arguments;
import zingg.client.ClientOptions;
import zingg.client.FieldDefinition;
import zingg.client.IZingg;
import zingg.client.MatchType;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.util.Analytics;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.GraphUtil;
import zingg.client.util.ListMap;
import zingg.util.Metric;
import zingg.util.ModelUtil;
import zingg.feature.Feature;
import zingg.feature.FeatureFactory;
import zingg.hash.HashFunction;

import zingg.util.HashUtil;
import zingg.util.PipeUtilBase;

//Spark Session
//Dataset
//row
//column
public abstract class ZinggBase<S,D, R, C, T> implements Serializable, IZingg<D, R, C> {

    protected Arguments args;
	
    protected S context;
    protected static String name;
    protected ZinggOptions zinggOptions;
    protected ListMap<DataType, HashFunction> hashFunctions;
	protected Map<FieldDefinition, Feature> featurers;
    protected long startTime;
	public static final String hashFunctionFile = "hashFunctions.json";
    protected ClientOptions clientOptions;

    public static final Log LOG = LogFactory.getLog(ZinggBase.class);
    protected PipeUtilBase<S,D,R,C> pipeUtil;
    protected HashUtil<D,R,C,T> hashUtil;
    protected DSUtil<S,D,R,C> dsUtil;
    protected GraphUtil<D,R,C> graphUtil;
    protected ModelUtil<S,D,R,C> modelUtil;
    protected BlockingTreeUtil<D,R,C,T> blockingTreeUtil;
    ZinggBase base;


    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public ClientOptions getClientOptions() {
        return this.clientOptions;
    }

    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }
    
    public ZinggBase() {

    }

    public void setBase(ZinggBase<S,D,R,C,T> base) {
        this.base = base;
    }

    
    public void init(Arguments args, String license)
        throws ZinggClientException {
            base.init(args, license);
        }

    
    protected void initHashFns() throws ZinggClientException {
        base.initHashFns();
	}

    public void loadFeatures() throws ZinggClientException {
		try{
		LOG.info("Start reading internal configurations and functions");
		if (args.getFieldDefinition() != null) {
			featurers = new HashMap<FieldDefinition, Feature>();
			for (FieldDefinition def : args.getFieldDefinition()) {
				if (! (def.getMatchType() == null || def.getMatchType().contains(MatchType.DONT_USE))) {
					Feature fea = (Feature) FeatureFactory.get(def.getDataType());
					fea.init(def);
					featurers.put(def, fea);			
				}
			}
			LOG.info("Finished reading internal configurations and functions");
			}
		}
		catch(Throwable t) {
			LOG.warn("Unable to initialize internal configurations and functions");
			if (LOG.isDebugEnabled()) t.printStackTrace();
			throw new ZinggClientException("Unable to initialize internal configurations and functions");
		}
	}

    
	public void postMetrics() {
        boolean collectMetrics = args.getCollectMetrics();
        Analytics.track(Metric.EXEC_TIME, (System.currentTimeMillis() - startTime) / 1000, collectMetrics);
		Analytics.track(Metric.TOTAL_FIELDS_COUNT, args.getFieldDefinition().size(), collectMetrics);
        Analytics.track(Metric.MATCH_FIELDS_COUNT, getDSUtil().getFieldDefinitionFiltered(args, MatchType.DONT_USE).size(),
                collectMetrics);
		Analytics.track(Metric.DATA_FORMAT, getPipeUtil().getPipesAsString(args.getData()), collectMetrics);
		Analytics.track(Metric.OUTPUT_FORMAT, getPipeUtil().getPipesAsString(args.getOutput()), collectMetrics);

		Analytics.postEvent(zinggOptions.getValue(), collectMetrics);
	}

    public Arguments getArgs() {
        return this.args;
    }

    public void setArgs(Arguments args) {
        this.args = args;
    }

    public ListMap getHashFunctions() {
        return this.hashFunctions;
    }

    public void setHashFunctions(ListMap hashFunctions) {
        this.hashFunctions = hashFunctions;
    }

    public Map<FieldDefinition,Feature> getFeaturers() {
        return this.featurers;
    }

    public void setFeaturers(Map<FieldDefinition,Feature> featurers) {
        this.featurers = featurers;
    }

    
    public S getContext() {
        return this.context;
    }

    public void setContext(S spark) {
        this.context = spark;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setZinggOptions(ZinggOptions zinggOptions) {
        this.zinggOptions = zinggOptions;
    }

	public String getName() {
        return name;
    }

    public ZinggOptions getZinggOptions() {
        return zinggOptions;
    }

    public ZFrame<D,R,C> getMarkedRecords() {
		try {
			return getPipeUtil().read(false, false, getPipeUtil().getTrainingDataMarkedPipe(args));
		} catch (ZinggClientException e) {
			LOG.warn("No record has been marked yet");
		}
		return null;
	}

	public ZFrame<D,R,C> getUnmarkedRecords() {
		ZFrame<D,R,C> unmarkedRecords = null;
		ZFrame<D,R,C> markedRecords = null;
		unmarkedRecords = getPipeUtil().read(false, false, getPipeUtil().getTrainingDataUnmarkedPipe(args));
        markedRecords = getMarkedRecords();
        if (markedRecords != null ) {
        	unmarkedRecords = unmarkedRecords.join(markedRecords,ColName.CLUSTER_COLUMN, false, "left_anti");
        }
		return unmarkedRecords;
	}

   
    public Long getMarkedRecordsStat(ZFrame<D,R,C> markedRecords, long value) {
        return markedRecords.filter(markedRecords.equalTo(ColName.MATCH_FLAG_COL, value)).count() / 2;
    }

    @Override
    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_MATCH);
    }

    @Override
    public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_A_MATCH);
    }

    @Override
    public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_SURE);
    }



    public HashUtil<D,R,C,T> getHashUtil() {
        return base.getHashUtil();
    }

    public void setHashUtil(HashUtil<D,R,C,T> t) {
        base.setHashUtil(t);
    }

    public GraphUtil<D,R,C> getGraphUtil() {
        return base.getGraphUtil();
    }

    public void setGraphUtil(GraphUtil<D,R,C> t) {
        base.setGraphUtil(t);
    }

    public void setModelUtil(ModelUtil<S,D,R,C> t) {
        base.setModelUtil(t);
    }

    public ModelUtil<S,D,R,C>  getModelUtil() {
        return base.getModelUtil();
    }

    public abstract void execute() throws ZinggClientException ;

    
    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil) {
        base.setPipeUtil(pipeUtil);
        
    }

   
    public void setDSUtil(DSUtil<S,D,R,C> pipeUtil) {
       base.setDSUtil(pipeUtil);
        
    }

    public DSUtil<S,D,R,C> getDSUtil() {
        return base.dsUtil;
    }

    
    public PipeUtilBase<S,D,R,C> getPipeUtil() {
        return base.pipeUtil;
    }

    public BlockingTreeUtil<D,R,C,T> getBlockingTreeUtil() {
        return base.blockingTreeUtil;
    }



  
 }