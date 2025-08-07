package zingg.common.client.util.writer;

public interface DFWriter<D,R,C> {

    void setMode(String s);
    DFWriter<D,R,C> format(String f);
    DFWriter<D,R,C> option(String k, String v);
    void save(String location);
    void save();
    
}
