package zingg.spark.core;

import static org.apache.spark.sql.functions.callUDF;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.apache.spark.SparkException;
import org.apache.spark.SparkException$;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;
import zingg.spark.core.util.SparkFnRegistrar;

@ExtendWith(TestSparkBase.class)
public class TestImageType {
	
	
	private static final double SMALL_DELTA = 0.0000000001;
	private final SparkSession sparkSession;

	public TestImageType(SparkSession sparkSession) {
		this.sparkSession = sparkSession;
	}


	@Test
	public void testImageType() {
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataTypes.createArrayType(DataTypes.DoubleType)));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataTypes.createArrayType(DataTypes.StringType)));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromJson("{\"type\":\"array\",\"elementType\":\"double\",\"containsNull\":true}")));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataType.fromJson("{\"type\":\"array\",\"elementType\":\"string\",\"containsNull\":true}")));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromDDL("ARRAY<DOUBLE>")));
		assertEquals("ArrayType(StringType,true)",String.valueOf(DataType.fromDDL("ARRAY<STRING>")));
		assertEquals("ArrayType(DoubleType,true)",String.valueOf(DataType.fromDDL("array<double>")));
		
//		DataType arrDbl2 = DataType.fromDDL("array<double>");
//		DataType string1 = DataType.fromDDL("STRING");
//		DataType string2 = DataType.fromDDL("String");
//		DataType string3 = DataType.fromDDL("string");
//		
//		System.out.println(string1);
//		System.out.println(string2);
//		System.out.println(string3);
//		System.out.println(arrDbl2);
	}

	@Test
	public void testCosine() {
		Double[] d1 = {0.0,2.0};
		Double[] d2 = {0.0,1.0};
		Double[] d3 = {1.0,0.0};
		Double[] d4 = {-1.0,-1.0};
		Double[] d5 = {0.0,1.0,0.0};
		Double[] d6 = {1.0,0.0,0.0};
		Double[] d7 = {0.0,0.0,0.0};
		Double[] d8 = {1.0,0.0,0.0};
		
		
		assertEquals(1.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d1, d2));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d2, d3));
		double diff = 0.7071067811865475-ArrayDoubleSimilarityFunction.cosineSimilarity(d4, d3);
		assertTrue(Math.abs(diff) <SMALL_DELTA);
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d5, d6));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d7, d8));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d8, d7));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d7, d7));
		assertEquals(1.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d8, d8));
		
		System.out.println("edge cases , all should be 0: ");
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(null, d2));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(null, null));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(null, new Double[] {}));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(new Double[] {},null));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(new Double[] {}, new Double[] {}));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d1,d5));
		assertEquals(0.0,ArrayDoubleSimilarityFunction.cosineSimilarity(d5,d1));
	}
	
	@Test
	public void testUDFArray() {
		// create a DF with double array as a column
		Dataset<Row> df = createSampleDataset();
		df.show();
		// check the schema of DF
		df.printSchema();
		// register ArrayDoubleSimilarityFunction as a UDF
		TestUDFDoubleArr testUDFDoubleArr = new TestUDFDoubleArr();
		SparkFnRegistrar.registerUDF2(sparkSession, "testUDFDoubleArr", testUDFDoubleArr, DataTypes.DoubleType);
		// call the UDF from select clause of DF
		df = df.withColumn("cosine",
				callUDF("testUDFDoubleArr", df.col("image_embedding"), df.col("image_embedding")));
		// see if error is reproduced
		assertThrows(SparkException.class, df::show);
	}

	@Test
	public void testUDFList() {
		// create a DF with double array as a column
		Dataset<Row> df = createSampleDataset();
		df.show();
		// check the schema of DF
		df.printSchema();

		// register ArrayDoubleSimilarityFunction as a UDF
		TestUDFDoubleList testUDFDoubleList = new TestUDFDoubleList();
		SparkFnRegistrar.registerUDF2(sparkSession, "testUDFDoubleList", testUDFDoubleList, DataTypes.DoubleType);

		// call the UDF from select clause of DF
		df = df.withColumn("cosine", callUDF("testUDFDoubleList",df.col("image_embedding"),df.col("image_embedding")));
		// see if error is reproduced
		assertThrows(SparkException.class, df::show);
	}

	@Test
	public void testUDFSeq() {
		// create a DF with double array as a column
		Dataset<Row> df = createSampleDataset();
		df.show();
		// check the schema of DF
		df.printSchema();

		// register ArrayDoubleSimilarityFunction as a UDF
		TestUDFDoubleSeq testUDFDoubleSeq = new TestUDFDoubleSeq();
		SparkFnRegistrar.registerUDF2(sparkSession, "testUDFDoubleSeq", testUDFDoubleSeq, DataTypes.DoubleType);

		// call the UDF from select clause of DF
		df = df.withColumn("cosine", callUDF("testUDFDoubleSeq",df.col("image_embedding"),df.col("image_embedding")));
		// see if error is reproduced
		assertThrows(SparkException.class, df::show);
	}

	@Test
	public void testUDFWrappedArr() {
		// create a DF with double array as a column
		Dataset<Row> df = createSampleDataset();
		df.show();
		// check the schema of DF
		df.printSchema();
		
		// register ArrayDoubleSimilarityFunction as a UDF
		TestUDFDoubleWrappedArr testUDFDoubleWrappedArr = new TestUDFDoubleWrappedArr();
		SparkFnRegistrar.registerUDF2(sparkSession, "testUDFDoubleWrappedArr", testUDFDoubleWrappedArr, DataTypes.DoubleType);

		// call the UDF from select clause of DF
		df = df.withColumn("cosine", callUDF("testUDFDoubleWrappedArr",df.col("image_embedding"),df.col("image_embedding")));
		// see if error is reproduced
		df.show();
		
		Row r = df.head();
		
		Double cos = (Double)r.getAs("cosine");
		double diff = 1-cos;
		System.out.println("cos diff "+diff+ " "+cos.getClass());
		
		assertTrue(Math.abs(diff) <SMALL_DELTA);
	}
	
	@Test
	public void testUDFObj() {
		// create a DF with double array as a column
		Dataset<Row> df = createSampleDataset();
		df.show();
		// check the schema of DF
		df.printSchema();
		
		// register ArrayDoubleSimilarityFunction as a UDF
		TestUDFDoubleObj testUDFDoubleObj = new TestUDFDoubleObj();
		SparkFnRegistrar.registerUDF2(sparkSession, "testUDFDoubleObj", testUDFDoubleObj, DataTypes.DoubleType);

		// call the UDF from select clause of DF
		df = df.withColumn("cosine", callUDF("testUDFDoubleObj",df.col("image_embedding"),df.col("image_embedding")));
		// see if error is reproduced
		df.show();		
		
		Row r = df.head();
		
		Double cos = (Double)r.getAs("cosine");
		assertEquals(0.3, cos);
		System.out.println(""+cos+ " "+cos.getClass());
	}
	
	
	protected Dataset<Row> createSampleDataset() {
		
		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField("recid", DataTypes.StringType, false, Metadata.empty()),
				new StructField("givenname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("surname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("suburb", DataTypes.StringType, false, Metadata.empty()),
				new StructField("postcode", DataTypes.StringType, false, Metadata.empty()),
				new StructField("image_embedding", DataTypes.createArrayType(DataTypes.DoubleType), false, Metadata.empty())		
		});

		
		Dataset<Row> sample = sparkSession.createDataFrame(Arrays.asList(
				RowFactory.create("07317257", "erjc", "henson", "hendersonville", "2873g",new Double[]{0.1123,10.456,110.789}),
				RowFactory.create("03102490", "jhon", "kozak", "henders0nville", "28792",new Double[]{0.2123,20.456,220.789}),
				RowFactory.create("02890805", "david", "pisczek", "durham", "27717",new Double[]{0.3123,30.456,330.789}),
				RowFactory.create("04437063", "e5in", "bbrown", "greenville", "27858",new Double[]{0.4123,40.456,440.789}),
				RowFactory.create("03211564", "susan", "jones", "greenjboro", "274o7",new Double[]{0.5123,50.456,550.789}),
				RowFactory.create("04155808", "jerome", "wilkins", "battleborn", "2780g",new Double[]{0.6123,60.456,660.789}),
				RowFactory.create("05723231", "clarinw", "pastoreus", "elizabeth city", "27909",new Double[]{0.7123,70.456,770.789}),
				RowFactory.create("06087743", "william", "craven", "greenshoro", "27405",new Double[]{0.8123,80.456,880.789}),
				RowFactory.create("00538491", "marh", "jackdon", "greensboro", "27406",new Double[]{0.9123,90.456,990.789}),
				RowFactory.create("01306702", "vonnell", "palmer", "siler sity", "273q4",new Double[]{0.0123,100.456,11110.789})),
				schemaOfSample);

		return sample;
	}
	
	
	
	
}
