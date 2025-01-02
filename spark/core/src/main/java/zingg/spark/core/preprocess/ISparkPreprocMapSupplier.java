package zingg.spark.core.preprocess;

import zingg.common.core.preprocess.INeedsPreprocMap;
import zingg.common.core.preprocess.IPreprocMap;

public interface ISparkPreprocMapSupplier extends INeedsPreprocMap {

    default IPreprocMap getPreprocMap(){
        return new SparkPreprocMap();
    }
    
}
