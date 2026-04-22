package zingg.spark.client.util.reader;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.ReadStrategy;
import zingg.common.client.util.reader.ReadStrategyFactory;
import zingg.common.client.util.reader.impl.DefaultReadStrategy;
import zingg.spark.client.util.reader.impl.UnityCatalogReadStrategy;

public class SparkReadStrategyFactory extends ReadStrategyFactory<Dataset<Row>, Row, Column> {

    @Override
    public ReadStrategy<Dataset<Row>, Row, Column> getStrategy(Pipe<Dataset<Row>, Row, Column> pipe) {
        if (Pipe.FORMAT_UNITYCATALOG.equals(pipe.getFormat()) && pipe.getProps().containsKey(FilePipe.TABLE)) {
            return new UnityCatalogReadStrategy();
        } else {
            return new DefaultReadStrategy<>();
        }
    }
}
