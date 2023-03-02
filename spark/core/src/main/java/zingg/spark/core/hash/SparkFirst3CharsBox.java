package zingg.spark.core.hash;

import org.apache.spark.sql.types.DataTypes;

import zingg.hash.First3CharsBox;

public class SparkFirst3CharsBox extends SparkHashFunction<String, Integer>{

	public SparkFirst3CharsBox() {
	    setBaseHash(new First3CharsBox());
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.IntegerType);
	}
 
}