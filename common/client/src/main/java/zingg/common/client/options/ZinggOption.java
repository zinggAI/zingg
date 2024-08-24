package zingg.common.client.options;

public class ZinggOption {
    String name;

    public ZinggOption(String name) {
        this.name = name;
        ZinggOptions.put(this);
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString(){
        return name;
    }
}
