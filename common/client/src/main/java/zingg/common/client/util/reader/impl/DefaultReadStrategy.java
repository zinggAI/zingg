package zingg.common.client.util.reader.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.IDFReader;
import zingg.common.client.util.reader.ReadStrategy;

public class DefaultReadStrategy<D, R, C> implements ReadStrategy<D, R, C> {
    @Override
    public ZFrame<D, R, C> read(IDFReader<D, R, C> reader, Pipe<D, R, C> pipe) throws Exception, ZinggClientException {
        return reader.load();
    }
}

