package zingg;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.junit.jupiter.api.Test;

import zingg.spark.core.executor.ZinggSparkTester;

public class TestDataType {
	
	@Test
	public void testDataType() {
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataTypes.createArrayType(DataTypes.DoubleType)));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataTypes.createArrayType(DataTypes.StringType)));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromJson("{\"type\":\"array\",\"elementType\":\"double\",\"containsNull\":true}")));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataType.fromJson("{\"type\":\"array\",\"elementType\":\"string\",\"containsNull\":true}")));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromDDL("ARRAY<DOUBLE>")));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataType.fromDDL("ARRAY<STRING>")));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromDDL("array<double>")));
		
		assertEquals("StringType",String.valueOf(DataType.fromDDL("STRING")));
		assertEquals("StringType",String.valueOf(DataType.fromDDL("String")));
		assertEquals("StringType",String.valueOf(DataType.fromDDL("string")));
		assertEquals("DateType",String.valueOf(DataType.fromDDL("date")));
		
	}
	
}
