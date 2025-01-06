package zingg.common.core.preprocess;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;

public interface IPreprocessors<S,D,R,C,T> extends INeedsPreprocMap<S,D,R,C,T>, IPreprocOrder {
    
    public void setContext(IContext<S,D,R,C,T> c); 

    public IZArgs getArgs();

    default ZFrame<D,R,C> preprocess(ZFrame<D,R,C> df) throws InstantiationException, IllegalAccessException, ZinggClientException { 
        ZFrame<D,R,C> dfp = df;  
        for(FieldDefinition def:((IArguments) getArgs()).getFieldDefinition()){
            for(IPreprocType o: getPreprocOrder()){
                //creating new instance of the class
                IPreprocessor ip = getPreprocMap().get(o).newInstance(); 
                //setting context and field defn
                ip.getContext();
                ip.setFieldDefinition(def);
                dfp = ip.preprocess(dfp);
            }
        }
        return dfp;  
    }

}
