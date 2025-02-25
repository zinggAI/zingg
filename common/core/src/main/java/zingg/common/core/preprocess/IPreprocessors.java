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
            for(IPreprocType preprocType: getPreprocOrder().getOrder()) {
                if (ProcessingType.SINGLE.equals(preprocType.getProcessingType())) {
                    for(FieldDefinition def:((IArguments) getArgs()).getFieldDefinition()){
                        //creating new instance of the class
                        ISingleFieldPreprocessor<S,D,R,C,T> ip = (ISingleFieldPreprocessor<S, D, R, C, T>) getPreprocMap().get(preprocType).getDeclaredConstructor().newInstance();
                        ip.setFieldDefinition(def);
                        dfp = executeAndBuildPreprocessedDF(ip, dfp);
                    }
                } else {
                    //creating new instance of the class
                    IMultiFieldPreprocessor<S,D,R,C,T> ip = (IMultiFieldPreprocessor<S, D, R, C, T>) getPreprocMap().get(preprocType).getDeclaredConstructor().newInstance();
                    ip.setFieldDefinitions(((IArguments) getArgs()).getFieldDefinition());
                    dfp = executeAndBuildPreprocessedDF(ip, dfp);
                }
            }
        } catch(Exception e){
            if (LOG.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        return dfp;
    }

    default ZFrame<D, R, C> executeAndBuildPreprocessedDF(IPreprocessor<S, D, R, C, T> preprocessor, ZFrame<D, R, C> inputDF) throws ZinggClientException {
        preprocessor.setContext(getContext());
        preprocessor.init();
        return preprocessor.preprocess(inputDF);
    }

}
