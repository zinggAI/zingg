package zingg.infrastructure.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.*;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Format;
import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.domain.model.records.RecordRepository;
import zingg.util.PipeUtil;

public class SparkRecordRepository implements RecordRepository {
    private final SparkSession spark;
    public static final Log LOG = LogFactory.getLog(SparkRecordRepository.class);

    public SparkRecordRepository(SparkSession spark) {
        this.spark = spark;
    }

    @Override
    public Dataset<Row> getRecords(boolean addLineNo,
                                   boolean addSource, Pipe pipe) {
        return null;
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

    private static Dataset<Row> readInternal(SparkSession spark, Pipe p, boolean addSource) throws ZinggClientException {
        DataFrameReader reader = getReader(spark, p);
        return read(reader, p, addSource);
    }

    private static Dataset<Row> read(DataFrameReader reader, Pipe p, boolean addSource) throws ZinggClientException{
        Dataset<Row> input;
        LOG.warn("Reading " + p);
        try {

            if (p.getFormat() == Format.INMEMORY) {
                input = ((InMemoryPipe) p).getRecords();
            }
            else {
                if (p.getProps().containsKey(FilePipe.LOCATION)) {
                    input = reader.load(p.get(FilePipe.LOCATION));
                }
                else {
                    input = reader.load();
                }
            }
            if (addSource) {
                input = input.withColumn(ColName.SOURCE_COL, functions.lit(p.getName()));
            }
        } catch (Exception ex) {
            throw new ZinggClientException("Could not read data.", ex);
        }
        return input;
    }


    private Dataset<Row> readInternal(boolean addLineNo,
                                             boolean addSource, Pipe... pipes) throws ZinggClientException {
        Dataset<Row> input = null;

        for (Pipe p : pipes) {
            if (input == null) {
                input = readInternal(spark, p, addSource);
                LOG.debug("input size is " + input.count());
            } else {
                if (p.get("type") != null && p.get("type").equals("join")) {
                    LOG.warn("joining inputs");
                    Dataset<Row> input1 = readInternal(spark, p, addSource);
                    LOG.warn("input now size is " + input1.count());
                    input = joinTrainingSetstoGetLabels(input, input1 );
                }
                else {
                    input = input.union(readInternal(spark, p, addSource));
                }
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
