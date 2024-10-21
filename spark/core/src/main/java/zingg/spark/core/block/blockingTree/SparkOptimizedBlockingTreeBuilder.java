package zingg.spark.core.block.blockingTree;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import zingg.common.core.block.blockingTree.OptimizedBlockingTreeBuilder;
import zingg.common.core.feature.FeatureFactory;
import zingg.spark.core.feature.SparkFeatureFactory;

public class SparkOptimizedBlockingTreeBuilder extends OptimizedBlockingTreeBuilder<Dataset<Row>, Row, Column, DataType> {
    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }

}
