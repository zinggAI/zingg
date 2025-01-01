package zingg.common.core.context;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DSUtil;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;

public interface IContext <S, D, R, C, T> {
    
    public HashUtil<S, D, R, C, T> getHashUtil();
    
    public void setHashUtil(HashUtil<S, D, R, C, T> hashUtil);
    
    public GraphUtil<D, R, C> getGraphUtil();
    
    public void setGraphUtil(GraphUtil<D, R, C> graphUtil);
    
    public ModelUtil<S, T, D, R, C> getModelUtil();
    
    public void setModelUtil(ModelUtil<S, T, D, R, C> modelUtil);
    
    public PipeUtilBase<S, D, R, C> getPipeUtil();
    
    public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil);
    
    public DSUtil<S, D, R, C> getDSUtil();
    
    public void setDSUtil(DSUtil<S, D, R, C> dsUtil);
    
    public BlockingTreeUtil<S, D, R, C, T> getBlockingTreeUtil();
    
    public void setBlockingTreeUtil(BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil);
    
    public S getSession();
    
    public void setSession(S session);
    
    public void init(S session) throws ZinggClientException;
    
    public void cleanup();
    
    public void setUtils();

     public IModelHelper getModelHelper();
    public void setModelHelper(IModelHelper modelHelper);

}










