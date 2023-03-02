package zingg.common.client;

import zingg.common.client.IZingg;

public interface IZinggFactory {

    public IZingg get(ZinggOptions z) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
    
}
