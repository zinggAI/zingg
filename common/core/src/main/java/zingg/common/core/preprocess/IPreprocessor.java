package zingg.common.core.preprocess;

import java.io.Serializable;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;

public interface IPreprocessor<S,D,R,C,T> extends Serializable{

    public void setContext(IContext<S,D,R,C,T> c); 

    public void init();

    public IContext<S,D,R,C,T> getContext();

    public void setFieldDefinition(FieldDefinition fd);

    public FieldDefinition getFieldDefinition();

    public boolean isApplicable(); 

    public ZFrame<D,R,C> preprocess(ZFrame<D,R,C> df) throws ZinggClientException; 

}
