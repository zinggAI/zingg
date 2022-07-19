package zingg.domain.model.pipes;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SparkSession;
import zingg.client.pipe.Pipe;
import zingg.domain.model.datasets.Concatenate;
import zingg.domain.model.datasets.DatasetCombiner;
import zingg.domain.model.datasets.Join;

import java.util.function.Function;

public class PipeWrapper {
    private final Pipe pipe;

    public PipeWrapper(Pipe pipe) {
        this.pipe = pipe;
    }

    public DatasetCombiner combiner(){
        if (this.pipe.get("type") != null && this.pipe.get("type").equals("join")){
            return new Join();
        }
        else{
            return new Concatenate();
        }
    }

    public DatasetReader getReader(SparkSession sparkSession){
        return PipeBasedDatasetReader.newReader(sparkSession, this.pipe, Function.identity());
    }

    public DatasetReader getReader(SparkSession sparkSession, Function<DataFrameReader, DataFrameReader> readerCustomizer){
        return PipeBasedDatasetReader.newReader(sparkSession, this.pipe, readerCustomizer);
    }
}
