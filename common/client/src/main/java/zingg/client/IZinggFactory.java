package zingg.client;

public interface IZinggFactory {

    public IZingg get(ZinggOptions z) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
    
}
