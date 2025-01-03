package zingg.common.core.preprocess;

public class PreprocType implements IPreprocType {

    String name;

    public PreprocType(){

    }

    public PreprocType(String type){
        this.name = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
    
}
