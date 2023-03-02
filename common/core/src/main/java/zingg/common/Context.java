package zingg.common;

import java.io.Serializable;

import zingg.common.client.ZinggClientException;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.HashUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;

public interface Context <S,D, R, C,T> extends Serializable {

    public HashUtil<S,D,R,C,T> getHashUtil() ;
    public void setHashUtil(HashUtil<S,D,R,C,T> t) ;
    public GraphUtil<D,R,C> getGraphUtil() ;

    public void setGraphUtil(GraphUtil<D,R,C> t) ;

    public void setModelUtil(ModelUtil<S,T,D,R,C> t);
    public void setBlockingTreeUtil(BlockingTreeUtil<S, D,R,C,T> t) ;

    public ModelUtil<S,T,D,R,C>  getModelUtil();

    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil);
    public void setDSUtil(DSUtil<S,D,R,C> pipeUtil);
    public DSUtil<S,D,R,C> getDSUtil() ;
    public PipeUtilBase<S,D,R,C> getPipeUtil();
    public BlockingTreeUtil<S, D,R,C,T> getBlockingTreeUtil() ;

    public void init(String license)
        throws ZinggClientException;

    public S getSession();

    
    //public void initHashFns() throws ZinggClientException;




  
 }


    

