package zingg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import zingg.common.Context;
import zingg.util.Metric;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;
import zingg.feature.Feature;
import zingg.feature.FeatureFactory;
import zingg.hash.HashFunction;

import zingg.util.HashUtil;


public abstract class ZinggBase<S,D, R, C, T> implements Serializable, IZingg<S, D, R, C> {

    protected Arguments args;
	
    protected Context<S,D,R,C,T> context;
    protected String name;
    protected ZinggOptions zinggOptions;
   protected long startTime;
    protected ClientOptions clientOptions;

    public static final Log LOG = LogFactory.getLog(ZinggBase.class);
   

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

   
    
    public void init(Arguments args, String license)
        throws ZinggClientException {
            startTime = System.currentTimeMillis();
            this.args = args;
            
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

   
    
    
    public Context<S,D,R,C,T> getContext() {
        return this.context;
    }

    public void setContext(Context<S,D,R,C,T> source) {
        this.context = source;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
	}

	public ZFrame<D,R,C> getUnmarkedRecords(){
        try{
            ZFrame<D,R,C> unmarkedRecords = null;
            ZFrame<D,R,C> markedRecords = null;
            unmarkedRecords = getPipeUtil().read(false, false, getPipeUtil().getTrainingDataUnmarkedPipe(args));
            markedRecords = getMarkedRecords();
            if (markedRecords != null ) {
                unmarkedRecords = unmarkedRecords.join(markedRecords,ColName.CLUSTER_COLUMN, false, "left_anti");
            }
            return unmarkedRecords;
        }
        catch(ZinggClientException e) {
            return null;
        }
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

    public abstract void execute() throws ZinggClientException ;

    public HashUtil<D,R,C,T> getHashUtil() {
        return context.getHashUtil();
    }

    public void setHashUtil(HashUtil<D,R,C,T> t) {
        context.setHashUtil(t);
    }

    public GraphUtil<D,R,C> getGraphUtil() {
        return context.getGraphUtil();
    }

   

    public ModelUtil<S,T,D,R,C>  getModelUtil() {
        return context.getModelUtil();
    }

   
  
   

    public DSUtil<S,D,R,C> getDSUtil() {
        return context.getDSUtil();
    }

    
    public PipeUtilBase<S,D,R,C> getPipeUtil() {
        return context.getPipeUtil();
    }

    public BlockingTreeUtil<S, D,R,C,T> getBlockingTreeUtil() {
        return context.getBlockingTreeUtil();
    }



  
 }