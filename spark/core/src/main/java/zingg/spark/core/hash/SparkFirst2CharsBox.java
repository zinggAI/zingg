package zingg.spark.core.hash;

import org.apache.spark.sql.types.DataTypes;

import zingg.hash.First2CharsBox;

public class SparkFirst2CharsBox extends SparkHashFunction<String, Integer>{

 	public SparkFirst2CharsBox() {
	    setBaseHash(new First2CharsBox());
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.IntegerType);
	}
	
}
