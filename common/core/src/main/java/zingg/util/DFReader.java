package zingg.util;
import zingg.client.ZFrame;

public interface DFReader<D, R, C> {

    public DFReader<D,R,C> getReader();

    public DFReader<D,R,C> format(String f);

    public DFReader<D,R,C> option(String k, String v);

    public DFReader<D,R,C> setSchema(String s);

    public ZFrame<D,R,C> load();

    public ZFrame<D,R,C> load(String location);

    
}
