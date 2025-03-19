package zingg.common.core.model.model;

import zingg.common.core.preprocess.stopwords.model.PriorStopWordProcess;

public class InputDataModel extends PriorStopWordProcess {
    
    public InputDataModel(String z_zid, String field1, String field2, String field3, String z_zsource) {
        super(z_zid, field1, field2, field3, z_zsource);
    }
}
