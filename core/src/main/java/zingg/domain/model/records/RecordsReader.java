package zingg.domain.model.records;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.*;
import org.apache.spark.storage.StorageLevel;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Format;
import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.domain.model.datasets.DatasetCombiner;
import zingg.domain.model.pipes.DatasetReader;
import zingg.domain.model.pipes.PipeWrapper;
import zingg.util.PipeUtil;

import java.util.List;

public class RecordsReader {

    public static final Log LOG = LogFactory.getLog(RecordsReader.class);

    private final List<Pipe> pipes;

    private final SparkSession spark;

    public RecordsReader(List<Pipe> pipes, SparkSession spark) {
        this.pipes = pipes;
        this.spark = spark;
    }

    public Dataset<Row> read(boolean addLineNo, boolean addSource) throws ZinggClientException {
        Dataset<Row> rows = readInternal(addLineNo, addSource);
        rows = rows.persist(StorageLevel.MEMORY_ONLY());
        return rows;
    }

    private Dataset<Row> readInternal(boolean addLineNo, boolean addSource) throws ZinggClientException {
        Dataset<Row> input = null;
        for (Pipe p : pipes) {
            PipeWrapper pipeWrapper = new PipeWrapper(p);
            DatasetReader reader = pipeWrapper.getReader(spark);
            if (input == null) {
                input = reader.read(addSource);
                LOG.debug("input size is " + input.count());
            } else {

                DatasetCombiner combiner = pipeWrapper.combiner();
                Dataset<Row> input1 = reader.read(addSource);
                input = combiner.combine(input,input1);
            }
        }
        // we will probably need to create row number as string with pipename/id as
        // suffix
        if (addLineNo)
            input = zingg.scala.DFUtil.addRowNumber(input, spark);
        // we need to transform the input here by using stop words
        return input;
    }


}
