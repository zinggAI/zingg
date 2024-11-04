package zingg.common.core.feature;

import org.apache.spark.sql.types.DataType;
import zingg.spark.core.feature.SparkFeatureFactory;

public class SparkFieldDataTypeTester extends FieldDataTypeTester<DataType> {

    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }
}
