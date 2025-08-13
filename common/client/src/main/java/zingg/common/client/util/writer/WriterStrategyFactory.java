package zingg.common.client.util.writer;

import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.impl.CassandraWriterStrategy;
import zingg.common.client.util.writer.impl.InMemoryWriterStrategy;
import zingg.common.client.util.writer.impl.DefaultWriterStrategy;

public class WriterStrategyFactory<D, R, C> {
    protected final IDFWriter<D, R, C> dfWriter;

    public WriterStrategyFactory(IDFWriter<D, R, C> dfWriter) {
        this.dfWriter = dfWriter;
    }

    public WriterStrategy<D, R, C> getStrategy(Pipe<D, R, C> pipe) {
        String format = pipe.getFormat();
        if (Pipe.FORMAT_INMEMORY.equals(format)) {
            return new InMemoryWriterStrategy<>();
        } else if (Pipe.FORMAT_CASSANDRA.equals(format)) {
            return new CassandraWriterStrategy<>();
        }
        return new DefaultWriterStrategy<>(dfWriter);
    }
}

