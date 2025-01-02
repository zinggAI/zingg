package zingg.common.core.preprocess;

import java.io.Serializable;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;

public interface IPreprocessor<S,D,R,C,T> extends Serializable{

    public void setContext(IContext c); 

/* if the field will be altered by the processor. For eg for stop words line 37 of StopWordRemover – method is preprocessForStopWords processor) 
   if (!(def.getStopWords() == null || def.getStopWords() == "")) 
*/ 

    public boolean isApplicable(FieldDefinition fd); 

    public ZFrame<D,R,C> preprocess(ZFrame<D,R,C> df); 

}
