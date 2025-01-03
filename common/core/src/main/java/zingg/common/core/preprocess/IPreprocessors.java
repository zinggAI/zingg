package zingg.common.core.preprocess;

import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;

public interface IPreprocessors<S,D,R,C,T> extends INeedsPreprocMap<S,D,R,C,T> {
    
    public void setContext(IContext<S,D,R,C,T> c); 

    public void setArgs(IArguments args); 

    public IArguments getArgs();

    public void setPreprocOrder(List<IPreprocType> orderList); 

    public List<IPreprocType> getPreprocOrder(); 

    default ZFrame<D,R,C> preprocess(ZFrame<D,R,C> df) throws InstantiationException, IllegalAccessException, ZinggClientException { 
        ZFrame<D,R,C> dfp = df;  
        for(FieldDefinition def: getArgs().getFieldDefinition()){
            for(IPreprocType o: getPreprocOrder()){
                //creating new instance of the class
                IPreprocessor ip = (IPreprocessor) getPreprocMap().get(o).newInstance(); 
                //setting context and field defn
                ip.getContext();
                ip.setFieldDefinition(def);
                if(ip.isApplicable(def)){
                    dfp = ip.preprocess(dfp);
                }
            }
        }
        return dfp;  
    }

}
