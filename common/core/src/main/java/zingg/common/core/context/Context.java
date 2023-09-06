package zingg.common.core.context;

import java.io.Serializable;

import zingg.common.client.ZinggClientException;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.DSUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;
import zingg.common.core.util.PipeUtilBase;

public abstract class Context <S,D, R, C,T> implements Serializable {
    protected S session;
    protected PipeUtilBase<S,D,R,C> pipeUtil;
    protected HashUtil<S,D,R,C,T> hashUtil;
    protected DSUtil<S,D,R,C> dsUtil;
    protected GraphUtil<D,R,C> graphUtil;
    protected ModelUtil<S,T,D,R,C> modelUtil;
    protected BlockingTreeUtil<S, D,R,C, T> blockingTreeUtil;

	public static final String hashFunctionFile = "hashFunctions.json";

    public HashUtil<S,D,R,C,T> getHashUtil() {
        return this.hashUtil;
    }
    public void setHashUtil(HashUtil<S,D,R,C,T> t) {
        this.hashUtil = t;
    }
    public GraphUtil<D,R,C> getGraphUtil() {
        return this.graphUtil;
    }

    public void setGraphUtil(GraphUtil<D,R,C> t) {
        this.graphUtil = t;
    }

    public void setModelUtil(ModelUtil<S,T,D,R,C> t){
        this.modelUtil = t;
    }
    public void setBlockingTreeUtil(BlockingTreeUtil<S, D,R,C,T> t) {
        this.blockingTreeUtil = t;
    }

    public ModelUtil<S,T,D,R,C>  getModelUtil(){
        return this.modelUtil;
    }

    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil){
        this.pipeUtil = pipeUtil;
    }
    public void setDSUtil(DSUtil<S,D,R,C> d){
        this.dsUtil = d;
    }
    public DSUtil<S,D,R,C> getDSUtil() {
        return this.dsUtil;
    }
    public PipeUtilBase<S,D,R,C> getPipeUtil(){
        return this.pipeUtil;
    }
    public BlockingTreeUtil<S, D,R,C,T> getBlockingTreeUtil() {
        return this.blockingTreeUtil;
    }

    public abstract void init()
        throws ZinggClientException;
    
    public abstract void cleanup();
    
    /**convenience method to set all utils
     * especially useful when you dont want to create the connection/spark context etc
     * */
    public abstract void setUtils();

    public S getSession(){
        return session;
    }

    public void setSession(S session){
        this.session = session;
    }


  
 }


    

