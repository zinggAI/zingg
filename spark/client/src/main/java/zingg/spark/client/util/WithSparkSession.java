package zingg.spark.client.util;

import org.apache.spark.sql.SparkSession;
import zingg.common.client.util.WithSession;

public class WithSparkSession implements WithSession<SparkSession> {

    private SparkSession sparkSession;

    @Override
    public void setSession(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    @Override
    public SparkSession getSession() {
        return this.sparkSession;
    }
}
