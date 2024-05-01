package zingg.common.client;

import zingg.common.client.options.ZinggOption;

public interface IZinggFactory {

    public IZingg get(ZinggOption z) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
    
}
