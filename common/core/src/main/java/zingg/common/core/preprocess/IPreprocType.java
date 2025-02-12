package zingg.common.core.preprocess;

import zingg.common.client.Named;

public interface IPreprocType extends Named {

    void setProcessingType(ProcessingType processingType);
    ProcessingType getProcessingType();
}
