package zingg.spark.client.util.writer;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.IDFWriter;
import zingg.common.client.util.writer.WriterStrategy;
import zingg.common.client.util.writer.WriterStrategyFactory;
import zingg.common.client.util.writer.impl.CassandraWriterStrategy;
import zingg.common.client.util.writer.impl.DefaultWriterStrategy;
import zingg.spark.client.util.writer.impl.UnityCatalogWriterStrategy;

public class SparkWriterStrategyFactory extends WriterStrategyFactory<Dataset<Row>, Row, Column> {
    private static final String TABLE = "table";

    public SparkWriterStrategyFactory(IDFWriter<Dataset<Row>, Row, Column> dfWriter) {
        super(dfWriter);
    }

    @Override
    public WriterStrategy<Dataset<Row>, Row, Column> getStrategy(Pipe<Dataset<Row>, Row, Column> pipe) {
        String format = pipe.getFormat();
         if (Pipe.FORMAT_CASSANDRA.equals(format)) {
            return new CassandraWriterStrategy<>();
        } else if (Pipe.FORMAT_UNITYCATALOG.equals(pipe.getFormat()) && pipe.getProps().containsKey(TABLE)) {
            return new UnityCatalogWriterStrategy(this.dfWriter);
        } else {
            return new DefaultWriterStrategy<>(this.dfWriter);
        }
    }
}
