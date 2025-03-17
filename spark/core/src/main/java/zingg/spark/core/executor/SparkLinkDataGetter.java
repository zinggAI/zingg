package zingg.spark.core.executor;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import zingg.common.client.ZFrame;
import zingg.common.core.executor.LinkDataGetter;

//TODO revisit this class
public class SparkLinkDataGetter extends LinkDataGetter<SparkSession, Dataset<Row>, Row, Column> {

    @Override
    protected ZFrame<Dataset<Row>, Row, Column> getLineAdjustedDF(ZFrame<Dataset<Row>, Row, Column> df) {
        return df.withColumn("z_zid", df.col("z_zid").plus(100000));
    }
}
