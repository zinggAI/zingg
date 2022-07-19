package zingg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.util.Analytics;
import zingg.util.DSUtil;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.ListMap;
import zingg.util.Metric;
import zingg.feature.Feature;
import zingg.feature.FeatureFactory;
import zingg.hash.HashFunction;

import zingg.util.HashUtil;
import zingg.util.PipeUtil;

public abstract class ZinggBase implements Serializable, IZingg {

    protected Arguments args;
	
    protected JavaSparkContext ctx;
	protected SparkSession spark;
    protected static String name;
    protected ZinggOptions zinggOptions;

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
    protected ListMap<DataType, HashFunction> hashFunctions;
	protected Map<FieldDefinition, Feature> featurers;
    protected long startTime;
	public static final String hashFunctionFile = "hashFunctions.json";
    protected ClientOptions clientOptions;

    public static final Log LOG = LogFactory.getLog(ZinggBase.class);

    @Override
    public void init(Arguments args, String license)
        throws ZinggClientException {
        startTime = System.currentTimeMillis();
        this.args = args;
        try{
            spark = SparkSession
                .builder()
                .appName("Zingg"+args.getJobId())
                .getOrCreate();
            ctx = new JavaSparkContext(spark.sparkContext());
            JavaSparkContext.jarOfClass(IZingg.class);
            LOG.debug("Context " + ctx.toString());
            initHashFns();
            loadFeatures();
            ctx.setCheckpointDir("/tmp/checkpoint");	
        }
        catch(Throwable e) {
            if (LOG.isDebugEnabled()) e.printStackTrace();
            throw new ZinggClientException(e.getMessage());
        }
    }


    @Override
    public void cleanup() throws ZinggClientException {
        if (ctx != null) ctx.stop();
    }

    void initHashFns() throws ZinggClientException {
		try {
			//functions = Util.getFunctionList(this.functionFile);
			hashFunctions = HashUtil.getHashFunctionList(this.hashFunctionFile, spark);
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			throw new ZinggClientException("Unable to initialize base functions");
		}		
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

    public void copyContext(ZinggBase b) {
            this.args = b.args;
            this.ctx = b.ctx;
            this.spark = b.spark;
            this.featurers = b.featurers;
            this.hashFunctions = b.hashFunctions;
    }

	public void postMetrics() {
        boolean collectMetrics = args.getCollectMetrics();
        Analytics.track(Metric.EXEC_TIME, (System.currentTimeMillis() - startTime) / 1000, collectMetrics);
		Analytics.track(Metric.TOTAL_FIELDS_COUNT, args.getFieldDefinition().size(), collectMetrics);
        Analytics.track(Metric.MATCH_FIELDS_COUNT, DSUtil.getFieldDefinitionFiltered(args, MatchType.DONT_USE).size(),
                collectMetrics);
		Analytics.track(Metric.DATA_FORMAT, PipeUtil.getPipesAsString(args.getData()), collectMetrics);
		Analytics.track(Metric.OUTPUT_FORMAT, PipeUtil.getPipesAsString(args.getOutput()), collectMetrics);

		Analytics.postEvent(zinggOptions.getValue(), collectMetrics);
	}

    public Arguments getArgs() {
        return this.args;
    }

    public void setArgs(Arguments args) {
        this.args = args;
    }

    public ListMap<DataType,HashFunction> getHashFunctions() {
        return this.hashFunctions;
    }

    public void setHashFunctions(ListMap<DataType,HashFunction> hashFunctions) {
        this.hashFunctions = hashFunctions;
    }

    public Map<FieldDefinition,Feature> getFeaturers() {
        return this.featurers;
    }

    public void setFeaturers(Map<FieldDefinition,Feature> featurers) {
        this.featurers = featurers;
    }

    public JavaSparkContext getCtx() {
        return this.ctx;
    }

    public void setCtx(JavaSparkContext ctx) {
        this.ctx = ctx;
    }

    public SparkSession getSpark() {
        return this.spark;
    }

    public void setSpark(SparkSession spark) {
        this.spark = spark;
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

    public Dataset<Row> getMarkedRecords() {
		try {
			return PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
		} catch (ZinggClientException e) {
			LOG.warn("No record has been marked yet");
		}
		return null;
	}

	public Dataset<Row> getUnmarkedRecords() {
		Dataset<Row> unmarkedRecords = null;
		Dataset<Row> markedRecords = null;
		try {
			unmarkedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataUnmarkedPipe(args));
			markedRecords = getMarkedRecords();
			if (markedRecords != null ) {
				unmarkedRecords = unmarkedRecords.join(markedRecords,
						unmarkedRecords.col(ColName.CLUSTER_COLUMN).equalTo(markedRecords.col(ColName.CLUSTER_COLUMN)),
						"left_anti");
			} 
		} catch (ZinggClientException e) {
			LOG.warn("No unmarked record");
		}
		return unmarkedRecords;
	}

    public Long getMarkedRecordsStat(Dataset<Row> markedRecords, long value) {
        return markedRecords.filter(markedRecords.col(ColName.MATCH_FLAG_COL).equalTo(value)).count() / 2;
    }

    public Long getMatchedMarkedRecordsStat(Dataset<Row> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_MATCH);
    }

    public Long getUnmatchedMarkedRecordsStat(Dataset<Row> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_A_MATCH);
    }

    public Long getUnsureMarkedRecordsStat(Dataset<Row> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_SURE);
    }




  
 }