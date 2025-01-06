package zingg.common.core.preprocess;

import java.util.Arrays;
import java.util.List;

public interface IPreprocOrder {

    List<IPreprocType> PREPROC_ORDER = Arrays.asList(IPreprocTypes.STOPWORDS);
    //to do - add lowercase before stopwords

    default List<IPreprocType> getPreprocOrder(){
        return PREPROC_ORDER;
    }
    
}
