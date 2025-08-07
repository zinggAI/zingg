package zingg.common.client.util.reader;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface DFReader<D, R, C> {

    DFReader<D,R,C> getReader();

    DFReader<D,R,C> format(String f);

    DFReader<D,R,C> option(String k, String v);

    DFReader<D,R,C> setSchema(String s);

    ZFrame<D,R,C> load() throws ZinggClientException;

    ZFrame<D,R,C> load(String location) throws ZinggClientException;

    
}
