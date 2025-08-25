package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.IDFReader;
import zingg.common.client.util.reader.ReadStrategy;
import zingg.common.client.util.reader.ReadStrategyFactory;
import zingg.spark.client.SparkFrame;
import org.apache.spark.sql.SparkSession;

public class SparkDFReader implements IDFReader<Dataset<Row>, Row, Column> {

    protected final DataFrameReader reader;

    public SparkDFReader(SparkSession s) {
        this.reader = s.read();
    }

    @Override
    public IDFReader<Dataset<Row>, Row, Column> getReader() {
        return this;
    }

    @Override
    public IDFReader<Dataset<Row>, Row, Column> format(String f) {
        this.reader.format(f);
        return this;
    }

    @Override
    public IDFReader<Dataset<Row>, Row, Column> option(String k, String v){
        this.reader.option(k,v);
        return this;
    }

    @Override
    public IDFReader<Dataset<Row>, Row, Column> setSchema(String s) {
        this.reader.schema(StructType.fromDDL(s));
        return this;
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> load() {
        return new SparkFrame(this.reader.load());
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> read(Pipe<Dataset<Row>, Row, Column> pipe) throws ZinggClientException, Exception {
        ReadStrategy<Dataset<Row>, Row, Column> readStrategy = getReadStrategy(pipe);
        return readStrategy.read(this, pipe);
    }

    protected ReadStrategy<Dataset<Row>, Row, Column> getReadStrategy(Pipe<Dataset<Row>, Row, Column> pipe) {
        return new ReadStrategyFactory<Dataset<Row>, Row, Column>().getStrategy(pipe);
    }

}
