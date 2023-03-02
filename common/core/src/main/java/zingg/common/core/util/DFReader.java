package zingg.common.core.util;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface DFReader<D, R, C> {

    public DFReader<D,R,C> getReader();

    public DFReader<D,R,C> format(String f);

    public DFReader<D,R,C> option(String k, String v);

    public DFReader<D,R,C> setSchema(String s);

    public ZFrame<D,R,C> load() throws ZinggClientException;

    public ZFrame<D,R,C> load(String location) throws ZinggClientException;

    
}
