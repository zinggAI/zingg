package zingg.domain.model.pipes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.*;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;

import java.util.function.Function;


abstract class PipeBasedDatasetReader implements DatasetReader{

    public static final Log LOG = LogFactory.getLog(PipeBasedDatasetReader.class);
    private final Pipe pipe;

    private PipeBasedDatasetReader(Pipe pipe) {
        this.pipe = pipe;
    }

    protected abstract Dataset<Row> read();

    @Override
    public Dataset<Row> read(boolean addSource) throws ZinggClientException {
        Dataset<Row> result = read();
        if (addSource) {
            result = result.withColumn(ColName.SOURCE_COL, functions.lit(pipe.getName()));
        }
        return result;
    }

    static class InMemoryDatasetReader extends PipeBasedDatasetReader{
        private final InMemoryPipe inMemoryPipe;

        private InMemoryDatasetReader(InMemoryPipe inMemoryPipe) {
            super(inMemoryPipe);
            this.inMemoryPipe = inMemoryPipe;
        }

        @Override
        protected Dataset<Row> read() {
            return this.inMemoryPipe.getRecords();
        }
    }

    static class PathBasedDatasetReader extends PipeBasedDatasetReader{
        private final String path;
        private final DataFrameReader reader;

        private PathBasedDatasetReader(Pipe pipe, String path, DataFrameReader reader) {
            super(pipe);
            this.path = path;
            this.reader = reader;
        }

        @Override
        protected Dataset<Row> read() {
            return reader.load(path);
        }
    }

    static class ObviousDatasetReader extends PipeBasedDatasetReader{

        private final DataFrameReader reader;

        public ObviousDatasetReader(Pipe pipe, DataFrameReader reader) {
            super(pipe);
            this.reader = reader;
        }

        @Override
        protected Dataset<Row> read() {
            return reader.load();
        }
    }

    private static DataFrameReader getReader(SparkSession spark, Pipe p) {
        DataFrameReader reader = spark.read();
        LOG.warn("Reading input " + p.getFormat().type());
        reader = reader.format(p.getFormat().type());
        if (p.getSchema() != null) {
            reader = reader.schema(p.getSchema());
        }
        for (String key : p.getProps().keySet()) {
            reader = reader.option(key, p.get(key));
        }
        reader = reader.option("mode", "PERMISSIVE");
        return reader;
    }


    public static DatasetReader newReader(SparkSession spark, Pipe pipe, Function<DataFrameReader, DataFrameReader> readerCustomizer){
        if (pipe instanceof InMemoryPipe){
            return new InMemoryDatasetReader((InMemoryPipe) pipe);
        }
        else {
            DataFrameReader reader = readerCustomizer.apply(getReader(spark, pipe));
            if(pipe.getProps().containsKey(FilePipe.LOCATION)){
                String path = pipe.get(FilePipe.LOCATION);
                return new PathBasedDatasetReader(pipe, path, reader);
            }
            else{
                return new ObviousDatasetReader(pipe, reader);
            }
        }
    }

}
