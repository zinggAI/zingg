package zingg.common.client.util.writer;

import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.DFWriter;
import zingg.common.client.util.writer.impl.CassandraWriterStrategy;
import zingg.common.client.util.writer.impl.FileWriterStrategy;
import zingg.common.client.util.writer.impl.InMemoryWriterStrategy;
import zingg.common.client.util.writer.impl.JdbcWriterStrategy;

import java.rmi.NoSuchObjectException;

public class WriterStrategyFactory<D, R, C> {
    private final DFWriter<D, R, C> dfWriter;

    public WriterStrategyFactory(DFWriter<D, R, C> dfWriter) {
        this.dfWriter = dfWriter;
    }

    public WriterStrategy<D, R, C> getStrategy(Pipe<D, R, C> pipe) throws NoSuchObjectException {
        String format = pipe.getFormat();
        if (Pipe.FORMAT_INMEMORY.equals(format)) {
            return new InMemoryWriterStrategy<>();
        } else if (Pipe.FORMAT_JDBC.equals(format)) {
            return new JdbcWriterStrategy<>(dfWriter);
        } else if (Pipe.FORMAT_CASSANDRA.equals(format)) {
            return new CassandraWriterStrategy<>();
        } else if (pipe.getProps().containsKey("location")) {
            return new FileWriterStrategy<>(dfWriter);
        }
        throw new NoSuchObjectException("No such writing strategy exists");
    }
}

