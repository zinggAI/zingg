package zingg.spark.core.feature;

import org.apache.spark.sql.types.DataType;

import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.feature.FieldDataTypeTester;

public class SparkFieldDataTypeTester extends FieldDataTypeTester<DataType> {

    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }
}
