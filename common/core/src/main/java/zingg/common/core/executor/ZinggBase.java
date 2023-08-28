package zingg.common.core.executor;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ILabelDataViewHelper;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.IZingg;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.DSUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.Metric;
import zingg.common.core.util.ModelUtil;
import zingg.common.core.util.PipeUtilBase;


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

   
    
    public void init(Arguments args, IZinggLicense license)
        throws ZinggClientException {
            startTime = System.currentTimeMillis();
            this.args = args;
            
        }


    public void setSession(S s) {
        getContext().setSession(s);
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

    /* 
    public ZinggOptions getZinggOptions() {
        return zinggOptions;
    }*/

    public ZFrame<D,R,C> getMarkedRecords() {
		try {
            return getPipeUtil().read(false, false, getPipeUtil().getTrainingDataMarkedPipe(args));
        } catch (ZinggClientException e) {
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

	@Override
	public void cleanup() throws ZinggClientException {
		context.cleanup();
	}
    
    public HashUtil<S,D,R,C,T> getHashUtil() {
        return context.getHashUtil();
    }

    public void setHashUtil(HashUtil<S,D,R,C,T> t) {
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
    
    @Override   
    public ITrainingDataModel<S, D, R, C> getTrainingDataModel() throws UnsupportedOperationException {
    	throw new UnsupportedOperationException("not implement in "+this.getClass());
    }    
    
    @Override  
    public ILabelDataViewHelper<S, D, R, C> getLabelDataViewHelper() throws UnsupportedOperationException {
    	throw new UnsupportedOperationException("not implement in "+this.getClass());
    }
    
 }