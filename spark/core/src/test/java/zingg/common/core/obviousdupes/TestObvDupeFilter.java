package zingg.common.core.obviousdupes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ObviousDupes;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.core.obviousdupes.ObvDupeFilter;
import zingg.common.core.obviousdupes.ObvDupeFilterHelper;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;
import zingg.spark.client.pipe.SparkPipe;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestObvDupeFilter extends ZinggSparkTester {
	
	@Test
	public void testGetObvDupePairs() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, getArgs());
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.getObvDupePairs(getBlockedDF());
		assertEquals(1, pairs.count());
		Row r = pairs.head();
		assertEquals(23, pairs.getAsInt(r,ColName.ID_COL));
		assertEquals(3, pairs.getAsInt(r,ColName.COL_PREFIX + ColName.ID_COL));
	}
	
	@Test
	public void testGetObvDupePairsNull() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, new Arguments());
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.getObvDupePairs(getBlockedDF());
		assertNull(pairs);
	}

	@Test
	public void testRemoveObvDupesFromBlocks() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, getArgs());
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.removeObvDupesFromBlocks(getBlocksDF());
		assertEquals(1, pairs.count());
		Row r = pairs.head();
		assertEquals(11, pairs.getAsInt(r,ColName.ID_COL));
		assertEquals(19, pairs.getAsInt(r,ColName.COL_PREFIX + ColName.ID_COL));
	}
	
	@Test
	public void testRemoveObvDupesFromBlocks2() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, getArgs());
		// obv dupe df is null => don't remove dupes
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.removeObvDupesFromBlocks(getBlocksDF(), null);
		assertEquals(2, pairs.count());
	}

	@Test
	public void testRemoveObvDupesFromBlocks3() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, getArgs());
		// as long as obv dupe df is not empty => remove dupes
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.removeObvDupesFromBlocks(getBlocksDF(),getBlocksDF());
		assertEquals(1, pairs.count());
		Row r = pairs.head();
		assertEquals(11, pairs.getAsInt(r,ColName.ID_COL));
		assertEquals(19, pairs.getAsInt(r,ColName.COL_PREFIX + ColName.ID_COL));
	}
	
	@Test
	public void testRemoveObvDupesFromBlocks4() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, getArgs());
		
		ZFrame<Dataset<Row>, Row, Column> emptyDF = getBlocksDF().filterNullCond(ColName.ID_COL);
		
		// obv dupe df is empty => don't remove dupes
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.removeObvDupesFromBlocks(getBlocksDF(), emptyDF);
		assertEquals(2, pairs.count());
	}
	
	@Test
	public void testRemoveObvDupesFromBlocksNull() throws ZinggClientException {
		ObvDupeFilter obvDupeFilter = new ObvDupeFilter(zsCTX, new Arguments());
		ZFrame<Dataset<Row>, Row, Column> pairs = obvDupeFilter.removeObvDupesFromBlocks(getBlocksDF());
		assertEquals(2, pairs.count());
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
			
			args.setObviousDupes(new ObviousDupes[]{getObviousDupes("dob")});

			return args;
			
	}
	
	@Test
	public void testGetObviousDupesFilter() throws ZinggClientException {	
		ObvDupeFilterHelper<ZSparkSession,Dataset<Row>,Row,Column,DataType> obvDupeFilter = new ObvDupeFilterHelper<ZSparkSession,Dataset<Row>,Row,Column,DataType>();

		SparkFrame posDF = getPosPairDF();
				
		Column filter = obvDupeFilter.getObviousDupesFilter(posDF,getObvDupeCond(),null);
		
		String expectedCond = "(((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL))))";
		
		assertEquals(expectedCond,filter.toString());
		
	}

	@Test
	public void testGetObviousDupesFilterWithExtraCond() throws ZinggClientException {	
		ObvDupeFilterHelper<ZSparkSession,Dataset<Row>,Row,Column,DataType> obvDupeFilter = new ObvDupeFilterHelper<ZSparkSession,Dataset<Row>,Row,Column,DataType>();
		SparkFrame posDF = getPosPairDF();
		Column gtCond = posDF.gt("z_zid");
		
		Column filter = obvDupeFilter.getObviousDupesFilter(posDF,getObvDupeCond(),gtCond);
		
		System.out.println(filter.toString());
		
		String expectedCond = "((((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL)))) AND (z_zid > z_z_zid))";
		
		assertEquals(expectedCond,filter.toString());
		
	}
	
	@Test
	public void testGetReverseObviousDupesFilter() throws ZinggClientException {	
		ObvDupeFilterHelper<ZSparkSession,Dataset<Row>,Row,Column,DataType> obvDupeFilter = new ObvDupeFilterHelper<ZSparkSession,Dataset<Row>,Row,Column,DataType>();

		SparkFrame posDF = getPosPairDF();
		ObviousDupes[] obvDupe = getObvDupeCond();		
				
				
		Column filter = obvDupeFilter.getReverseObviousDupesFilter(posDF,obvDupe,null);
		
		String expectedCond = "(NOT (((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL)))))";
		
		assertEquals(expectedCond,filter.toString());
		
	}
	
	
	
	protected SparkFrame getBlockedDF() {
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

	protected SparkFrame getBlocksDF() {
		Row[] rows = { 
				RowFactory.create(3, "Érik", "Guay", 19830807, -798, "customers",23, "Érika", "Charles", 19830807, -798, "customers"),
				RowFactory.create(11, "xani", "green", 19390410, 890, "customers",19, "x", "g", 19461101, 890, "customers")
				 
				};
		StructType schema = new StructType(
				new StructField[] { 
						new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
						new StructField("fname", DataTypes.StringType, false, Metadata.empty()),
						new StructField("lname", DataTypes.StringType, false, Metadata.empty()),
						new StructField("dob", DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.HASH_COL, DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty()),
						new StructField(ColName.COL_PREFIX + ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.COL_PREFIX + "fname", DataTypes.StringType, false, Metadata.empty()),
						new StructField(ColName.COL_PREFIX + "lname", DataTypes.StringType, false, Metadata.empty()),
						new StructField(ColName.COL_PREFIX + "dob", DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.COL_PREFIX + ColName.HASH_COL, DataTypes.IntegerType, false, Metadata.empty()),
						new StructField(ColName.COL_PREFIX + ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty()) 						
						});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}	
	
	private SparkFrame getPosPairDF() {
		Row[] posData = getPosPairRows();	
		StructType schema = getPairSchema();
		SparkFrame posDF = new SparkFrame(spark.createDataFrame(Arrays.asList(posData), schema));
		return posDF;
	}
	
	private Row[] getPosPairRows() {
		int row_id = 1;
		// Create a DataFrame containing test data
		Row[] posData = { 
				RowFactory.create( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Integer(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Integer(101),1),
				RowFactory.create( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Integer(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Integer(201),1  ),
				RowFactory.create(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Integer(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Integer(301),1 ),
				RowFactory.create( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Integer(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Integer(101),1),
				RowFactory.create( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Integer(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Integer(201),1  ),
				RowFactory.create(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Integer(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Integer(301),1 ),
				RowFactory.create( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Integer(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Integer(101),1),
				RowFactory.create( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Integer(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Integer(201),1  ),
				RowFactory.create(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Integer(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Integer(301),1 ),
				RowFactory.create( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Integer(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Integer(101),1),
				RowFactory.create( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Integer(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Integer(201),1  ),
				RowFactory.create(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Integer(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Integer(301),1 ),
				RowFactory.create( ++row_id, "52",   "nameObvDupe1"     ,"def"    ,"geh"    ,1900,   new Integer(1900), 0,++row_id, "410",   "nameObvDupe1",    "lmn",    "opq",       2001,       new Integer(1900), 0),
				RowFactory.create( ++row_id, "53",   "nameObvDupe2"     ,"eventObvDupe2"    ,"commentObvDupe2"    ,1900,   new Integer(1900), 0,++row_id, "54",   "nameObvDupe2",    "eventObvDupe2",    "commentObvDupe2",       2001,       new Integer(1901), 0),
				RowFactory.create( ++row_id, "53",   "nameObvDupe3"     ,"eventObvDupe3"    ,"commentObvDupe3"    ,1900,   new Integer(1901), 0,++row_id, "54",   "nameObvDupe3",    "eventObvDupe3",    "commentObvDupe3",       2001,       new Integer(1901), 0),
				RowFactory.create( ++row_id, "53",   "nameObvDupe3"     ,"eventObvDupe3"    ,"commentObvDupe3"    ,1900,   new Integer(1901), 0,++row_id, "54",   null,    "eventObvDupe3",    "commentObvDupe3",       2001,       new Integer(1901), 0)
		};
		return posData;
	}
	
	private StructType getPairSchema() {
		StructType schema = new StructType(new StructField[] {
				new StructField("z_zid", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("z_cluster", DataTypes.StringType, true, Metadata.empty()), 	
				new StructField("name", DataTypes.StringType, true, Metadata.empty()),
				new StructField("event", DataTypes.StringType, true, Metadata.empty()),
				new StructField("comment", DataTypes.StringType, true, Metadata.empty()),
				new StructField("year", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("dob", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("z_isMatch", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("z_z_zid", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("z_z_cluster", DataTypes.StringType, true, Metadata.empty()), 
				new StructField("z_name", DataTypes.StringType, true, Metadata.empty()),
				new StructField("z_event", DataTypes.StringType, true, Metadata.empty()),
				new StructField("z_comment", DataTypes.StringType, true, Metadata.empty()),
				new StructField("z_year", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("z_dob", DataTypes.IntegerType, true, Metadata.empty()),
				new StructField("z_z_isMatch", DataTypes.IntegerType, true, Metadata.empty())}
			);
		return schema;
	}
	
	protected ObviousDupes getObviousDupes(String field) {
		return getObviousDupes(new String[] {field});
	}

	protected ObviousDupes getObviousDupes(String[] fields) {
		HashMap<String, String>[]  matchCondition = new HashMap[fields.length];		
		for (int i = 0; i < fields.length; i++) {
			matchCondition[i] = new HashMap<String, String>();
			matchCondition[i].put(ObviousDupes.fieldName,fields[i]);
		}
		return new ObviousDupes(matchCondition);
	}
	
	public ObviousDupes[] getObvDupeCond() {
		ObviousDupes obvDupe1 = getObviousDupes(new String[]{"name","event","comment"});
		ObviousDupes obvDupe2 = getObviousDupes("dob");
		ObviousDupes obvDupe3 = getObviousDupes(new String[]{"comment","year"});
		ObviousDupes[] obvDupe = new ObviousDupes[] {obvDupe1,obvDupe2,obvDupe3};
		return obvDupe;
	}


}

