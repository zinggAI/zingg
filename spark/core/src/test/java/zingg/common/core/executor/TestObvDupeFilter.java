package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.pipe.SparkPipe;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestObvDupeFilter extends ZinggSparkTester {
	
	@Test
	public void testGetObvDupePairs() throws ZinggClientException {
		ZFrame<Dataset<Row>, Row, Column> pairs = getObvDupeFilter().getObvDupePairs(getInputData());
		assertEquals(1, pairs.count());
		Row r = pairs.head();
		assertEquals(23, pairs.getAsInt(r,ColName.ID_COL));
		assertEquals(3, pairs.getAsInt(r,ColName.COL_PREFIX + ColName.ID_COL));
	}
	
	public ObvDupeFilter getObvDupeFilter() throws ZinggClientException {
		return new ObvDupeFilter(zsCTX, null, null, getArgs());
	}
	
	
	public Arguments getArgs() throws ZinggClientException {
		Arguments args = new Arguments();
			FieldDefinition fname = new FieldDefinition();
			fname.setFieldName("fname");
			fname.setDataType("string");
			fname.setMatchType(Arrays.asList(MatchType.EXACT, MatchType.FUZZY));
			fname.setFields("fname");
			
			FieldDefinition lname = new FieldDefinition();
			lname.setFieldName("lname");
			lname.setDataType("string");
			lname.setMatchType(Arrays.asList(MatchType.FUZZY));
			lname.setFields("lname");

			FieldDefinition dob = new FieldDefinition();
			lname.setFieldName("dob");
			lname.setDataType("long");
			lname.setMatchType(Arrays.asList(MatchType.FUZZY));
			lname.setFields("dob");
			
			args.setFieldDefinition(Arrays.asList(fname, lname, dob));
			
			Pipe inputPipe = new SparkPipe();
			inputPipe.setName("test");
			inputPipe.setFormat(Pipe.FORMAT_CSV);
			inputPipe.setProp("location", "examples/febrl/test.csv");
			args.setData(new Pipe[] {inputPipe});

			Pipe outputPipe = new SparkPipe();
			outputPipe.setName("output");
			outputPipe.setFormat(Pipe.FORMAT_CSV);
			outputPipe.setProp("location", "examples/febrl/output.csv");
			args.setOutput(new Pipe[] {outputPipe});

			args.setBlockSize(400L);
			args.setCollectMetrics(true);
			args.setModelId("500");
			
			args.setObviousDupeCondition("dob");

			return args;
			
	}
	
	protected SparkFrame getInputData() {
		Row[] rows = { 
				RowFactory.create(3, "Érik", "Guay", 19830807, -798, "customers"),
				RowFactory.create(11, "xani", "green", 19390410, 890, "customers"),
				RowFactory.create(19, "sachin", "step", 19461101, 700, "customers"),
				RowFactory.create(23, "Érika", "Charles", 19830807, 991, "customers") 
				};
		StructType schema = new StructType(
				new StructField[] { 
						new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
						new StructField("fname", DataTypes.StringType, false, Metadata.empty()),
						new StructField("lname", DataTypes.StringType, false, Metadata.empty()),
						new StructField("dob", DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.HASH_COL, DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty()) 
						});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}	

	
	
}

