package zingg.common.client.util.reader.impl;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.DFReader;
import zingg.common.client.util.reader.ReadStrategy;

public class FileReadStrategy<D, R, C> implements ReadStrategy<D, R, C> {
    @Override
    public ZFrame<D, R, C> read(DFReader<D, R, C> reader, Pipe<D, R, C> pipe) throws Exception, ZinggClientException {
        if (pipe.getProps().containsKey(FilePipe.LOCATION)) {
            return reader.load(pipe.get(FilePipe.LOCATION));
        } else {
            return reader.load();
        }
    }
}

