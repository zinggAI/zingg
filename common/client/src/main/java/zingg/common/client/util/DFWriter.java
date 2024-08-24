package zingg.common.client.util;

public interface DFWriter<D,R,C> {

    public void setMode(String s);
    public DFWriter<D,R,C> format(String f);
    public DFWriter<D,R,C> option(String k, String v);
    public void save(String location);
    public void save();
    
}
