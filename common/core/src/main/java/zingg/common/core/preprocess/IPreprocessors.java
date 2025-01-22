package zingg.common.core.preprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;

public interface IPreprocessors<S,D,R,C,T> extends INeedsPreprocMap<S,D,R,C,T>, INeedsPreprocOrder {

    public static final Log LOG = LogFactory.getLog(IPreprocessors.class);
    
    public void setContext(IContext<S,D,R,C,T> c); 

    public IContext<S,D,R,C,T> getContext();

    public IZArgs getArgs();

    public void setArgs(IZArgs args);

    default ZFrame<D,R,C> preprocess(ZFrame<D,R,C> df) throws ZinggClientException { 
        ZFrame<D,R,C> dfp = df; 
        try{ 
            for(FieldDefinition def:((IArguments) getArgs()).getFieldDefinition()){
                for(IPreprocType o: getPreprocOrder().getOrder()){
                    //creating new instance of the class
                    IPreprocessor<S,D,R,C,T> ip = getPreprocMap().get(o).getDeclaredConstructor().newInstance(); 
                    LOG.info("trying preproc " + ip);
                    //setting context and field defn
                    ip.setContext(getContext());
                    ip.init();
                    ip.setFieldDefinition(def);
                    dfp = ip.preprocess(dfp);
                    LOG.info("after preproc ");
                    dfp.show();
                }
            }  
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return dfp;
    }

}
