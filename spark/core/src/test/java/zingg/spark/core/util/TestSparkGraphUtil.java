package zingg.spark.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import zingg.spark.client.SparkFrame;
import zingg.spark.core.TestSparkBaseLite;

@ExtendWith(TestSparkBaseLite.class)
public class TestSparkGraphUtil {

    private final SparkSession spark;

    public TestSparkGraphUtil(SparkSession spark) {
        this.spark = spark;
    }

    @Test
    public void repeatedGraphBuildsReleaseInternalCaches() {
        spark.catalog().clearCache();
        StructType vertexSchema = new StructType(new StructField[] {
                DataTypes.createStructField("z_zid", DataTypes.LongType, false),
                DataTypes.createStructField("id", DataTypes.StringType, false)
        });
        StructType edgeSchema = new StructType(new StructField[] {
                DataTypes.createStructField("z_zid", DataTypes.LongType, false),
                DataTypes.createStructField("z_z_zid", DataTypes.LongType, false)
        });
        Dataset<Row> vertices = spark.createDataFrame(Arrays.asList(
                RowFactory.create(1L, "one"), RowFactory.create(2L, "two"), RowFactory.create(3L, "three")), vertexSchema);
        Dataset<Row> edges = spark.createDataFrame(Arrays.asList(
                RowFactory.create(1L, 2L), RowFactory.create(2L, 3L)), edgeSchema);
        SparkGraphUtil graphUtil = new SparkGraphUtil();

        int baseline = spark.sharedState().cacheManager().cachedData().size();
        for (int i = 0; i < 3; i++) {
            Dataset<Row> result = ((SparkFrame) graphUtil.buildGraph(new SparkFrame(vertices), new SparkFrame(edges))).df();
            result.count();
            result.unpersist(false);
        }

        assertEquals(baseline, spark.sharedState().cacheManager().cachedData().size());
    }
}
