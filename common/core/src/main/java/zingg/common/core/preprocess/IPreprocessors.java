package zingg.common.core.preprocess;

import zingg.common.client.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;

public interface IPreprocessors<D,R,C> extends INeedsPreprocMap {

    public void setContext(IContext c); 

    public void setArgs(IZArgs args); 

    default ZFrame<D,R,C> preprocess(ZFrame<D,R,C> df){ 
        //go over field defs from args 
        //for each field def, go over iprocessor list from IPreprocOrder 
        //if ip is applicable to field, call its process.  
        //Pass returned zframe to next ip
        return null;  
    }

}
