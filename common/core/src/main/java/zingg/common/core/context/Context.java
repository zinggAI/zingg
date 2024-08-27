package zingg.common.core.context;

import java.io.Serializable;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DSUtil;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;
public abstract class Context <S,D, R, C,T> implements Serializable, IContext<S, D, R, C, T> {
    
    protected S session;
    protected PipeUtilBase<S,D,R,C> pipeUtil;
    protected HashUtil<S,D,R,C,T> hashUtil;
    protected DSUtil<S,D,R,C> dsUtil;
    protected GraphUtil<D,R,C> graphUtil;
    protected ModelUtil<S,T,D,R,C> modelUtil;
    protected BlockingTreeUtil<S, D,R,C, T> blockingTreeUtil;
    
    public static final String hashFunctionFile = "hashFunctions.json";
    
    @Override
    public HashUtil<S,D,R,C,T> getHashUtil() {
        return this.hashUtil;
    }

    @Override
    public void setHashUtil(HashUtil<S,D,R,C,T> t) {
        this.hashUtil = t;
    }

    @Override
    public GraphUtil<D,R,C> getGraphUtil() {
        return this.graphUtil;
    }

    @Override
    public void setGraphUtil(GraphUtil<D,R,C> t) {
        this.graphUtil = t;
    }

    @Override
    public void setModelUtil(ModelUtil<S,T,D,R,C> t){
        this.modelUtil = t;
    }

    @Override
    public void setBlockingTreeUtil(BlockingTreeUtil<S, D,R,C,T> t) {
        this.blockingTreeUtil = t;
    }
    @Override
    public ModelUtil<S,T,D,R,C>  getModelUtil(){
        return this.modelUtil;
    }

    @Override
    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil){
        this.pipeUtil = pipeUtil;
    }

    @Override
    public void setDSUtil(DSUtil<S,D,R,C> d){
        this.dsUtil = d;
    }

    @Override
    public DSUtil<S,D,R,C> getDSUtil() {
        return this.dsUtil;
    }

    @Override
    public PipeUtilBase<S,D,R,C> getPipeUtil(){
        return this.pipeUtil;
    }

    @Override
    public BlockingTreeUtil<S, D,R,C,T> getBlockingTreeUtil() {
        return this.blockingTreeUtil;
    }

    @Override
    public abstract void init(S session)
        throws ZinggClientException;

    @Override
    public abstract void cleanup();
    /**convenience method to set all utils
     * especially useful when you dont want to create the connection/spark context etc
     * */
    @Override
    public abstract void setUtils();
    
    @Override
    public S getSession(){
        return session;
    }
    @Override
    public void setSession(S session){
        this.session = session;
    }
 }