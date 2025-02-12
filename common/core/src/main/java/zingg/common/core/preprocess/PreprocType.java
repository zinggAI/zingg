package zingg.common.core.preprocess;

public class PreprocType implements IPreprocType {

    String name;
    ProcessingType processingType;

    public PreprocType(){

    }

    public PreprocType(String name, ProcessingType processingType){
        this.name = name;
        this.processingType = processingType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setProcessingType(ProcessingType processingType) {
        this.processingType = processingType;
    }

    @Override
    public ProcessingType getProcessingType() {
        return this.processingType;
    }
}
