package zingg.domain.model.records;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import zingg.client.ZinggClientException;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Pipe;
import zingg.domain.model.pipes.DatasetReader;
import zingg.domain.model.pipes.PipeWrapper;

import java.util.List;

public class RecordSampler {

    public static final Log LOG = LogFactory.getLog(RecordSampler.class);
    private final Pipe pipe;
    private final SparkSession sparkSession;

    public RecordSampler(Pipe pipe, SparkSession sparkSession) {
        this.pipe = pipe;
        this.sparkSession = sparkSession;
    }

    private static DataFrameReader customize(DataFrameReader reader){
        reader.option("inferSchema", true);
        reader.option("mode", "DROPMALFORMED");
        return reader;
    }

    public static Dataset<Row> sample(SparkSession spark, Pipe p) throws ZinggClientException {
        PipeWrapper pipeWrapper = new PipeWrapper(p);
        DatasetReader reader = pipeWrapper.getReader(spark, RecordSampler::customize);
        LOG.info("reader is ready to sample with inferring " + p.get(FilePipe.LOCATION));
        LOG.warn("Reading input of type " + p.getFormat().type());
        Dataset<Row> input = reader.read (false);
        // LOG.warn("inferred schema " + input.schema());
        List<Row> values = input.takeAsList(10);
        values.forEach(r -> LOG.info(r));
        Dataset<Row> ret = spark.createDataFrame(values, input.schema());
        return ret;

    }

}
