package zingg.spark.core.feature;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.feature.DateFeature;
import zingg.common.core.feature.DoubleFeature;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.feature.IntFeature;
import zingg.common.core.feature.StringFeature;

public class SparkFeatureFactory extends FeatureFactory<DataType>{

   

    @Override
    public void init() {
            System.out.println("init");
            map = new HashMap<DataType, Class>();
            map.put(DataTypes.StringType, StringFeature.class);
            map.put(DataTypes.IntegerType, IntFeature.class);
            map.put(DataTypes.DateType, DateFeature.class);
            map.put(DataTypes.DoubleType, DoubleFeature.class);
        
        
    }

    @Override
    public DataType getDataTypeFromString(String t) {
        System.out.println("getDataType");
        return DataType.fromJson(t);
    }

    

    
    
}
