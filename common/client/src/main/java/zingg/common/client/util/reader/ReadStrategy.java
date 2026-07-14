package zingg.common.client.util.reader;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

public interface ReadStrategy<D, R, C> {
    ZFrame<D, R, C> read(IDFReader<D, R, C> reader, Pipe<D, R, C> pipe) throws Exception, ZinggClientException;
}

