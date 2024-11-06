package zingg.spark.core.feature;

import java.util.HashMap;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.feature.ArrayDoubleFeature;
import zingg.common.core.feature.BooleanFeature;
import zingg.common.core.feature.DateFeature;
import zingg.common.core.feature.DoubleFeature;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.feature.FloatFeature;
import zingg.common.core.feature.IntFeature;
import zingg.common.core.feature.LongFeature;
import zingg.common.core.feature.StringFeature;

public class SparkFeatureFactory extends FeatureFactory<DataType>{

	private static final long serialVersionUID = 1L;
    
    @Override
    public void init() {
            map = new HashMap<DataType, Class>();
            map.put(DataTypes.StringType, StringFeature.class);
            map.put(DataTypes.IntegerType, IntFeature.class);
            map.put(DataTypes.DateType, DateFeature.class);
            map.put(DataTypes.DoubleType, DoubleFeature.class);
            map.put(DataTypes.FloatType, FloatFeature.class);
            map.put(DataTypes.LongType, LongFeature.class);
            map.put(DataTypes.createArrayType(DataTypes.DoubleType), ArrayDoubleFeature.class);
            map.put(DataTypes.BooleanType,BooleanFeature.class);
    }

    @Override
    public DataType getDataTypeFromString(String t) {
        return DataType.fromDDL(t);
    }
    
}
