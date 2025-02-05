package zingg.common.core.executor;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.IZArgs;
import zingg.common.client.IZingg;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.options.ZinggOption;
import zingg.common.client.util.DSUtil;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.context.IContext;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.Metric;
import zingg.common.core.util.ModelUtil;


public abstract class ZinggBaseCommon<S,D, R, C, T> implements Serializable, IZingg<S, D, R, C> {

    protected IContext<S,D,R,C,T> context;
    protected String name;
    protected ZinggOption zinggOption;
    protected long startTime;
    protected ClientOptions clientOptions;

    public static final Log LOG = LogFactory.getLog(ZinggBaseCommon.class);
   

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
    
    public ZinggBaseCommon() {

    }

    public void setSession(S s) {
        getContext().setSession(s);
    }
   
    public void track( boolean collectMetrics){

    }
    public abstract IZArgs getArgs();

	public abstract void setArgs(IZArgs a);
   
	public void postMetrics() {
        boolean collectMetrics = getArgs().getCollectMetrics();
        track(false);
        Analytics.track(Metric.EXEC_TIME, (System.currentTimeMillis() - startTime) / 1000, collectMetrics);
		Analytics.track(Metric.ZINGG_VERSION, "0.4.1-SNAPSHOT", collectMetrics);
        Analytics.trackEnvProp(Metric.DATABRICKS_RUNTIME_VERSION, collectMetrics);
        Analytics.trackEnvProp(Metric.DB_INSTANCE_TYPE, collectMetrics);
        Analytics.trackEnvProp(Metric.JAVA_HOME, collectMetrics); 
        Analytics.trackEnvProp(Metric.JAVA_VERSION, collectMetrics); 
        Analytics.trackEnvProp(Metric.OS_ARCH, collectMetrics); 
        Analytics.trackEnvProp(Metric.OS_NAME, collectMetrics); 
        //Analytics.trackEnvProp(Metric.USER_NAME, collectMetrics); 
        //Analytics.trackEnvProp(Metric.USER_HOME, collectMetrics); 
        Analytics.trackDomain(Metric.DOMAIN, collectMetrics);
        Analytics.track(Metric.ZINGG_VERSION, "0.4.1-SNAPSHOT", collectMetrics);
        Analytics.postEvent(zinggOption.getName(), collectMetrics);
	}

    
    public IContext<S,D,R,C,T> getContext() {
        return this.context;
    }

    public void setContext(IContext<S,D,R,C,T> source) {
        this.context = source;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public void setZinggOption(ZinggOption zinggOptions) {
        this.zinggOption = zinggOptions;
    }
    

	public String getName() {
        return name;
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

    public void setModelHelper(IModelHelper h){
        context.setModelHelper(h);
    }

    public IModelHelper getModelHelper(){
        return context.getModelHelper();
    }

   
    @Override
    public void init(IZArgs args, S session, ClientOptions c)
        throws ZinggClientException {
        startTime = System.currentTimeMillis();
        setArgs(args);
        setSession(session);
        setClientOptions(c);
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

    //TODO needs to revisit this
    @Override
    public ITrainingDataModel<S, D, R, C> getTrainingDataModel() {
        return null;
    }
 }