package zingg.spark.core.feature;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.feature.DateFeature;
import zingg.feature.DoubleFeature;
import zingg.feature.FeatureFactory;
import zingg.feature.IntFeature;
import zingg.feature.StringFeature;

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
