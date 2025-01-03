package zingg.common.core.preprocess;


public interface IPreprocMap<S,D,R,C,T>  {
    
    public void put(IPreprocType t, Class<? extends IPreprocessor<S,D,R,C,T>> p);

    public Class<? extends IPreprocessor<S,D,R,C,T>> get(IPreprocType t);

}
