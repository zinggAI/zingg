package zingg.common.core.deterministicmatching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import zingg.common.client.DeterministicMatching;
import zingg.common.client.ZinggClientException;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestDeterministicMatchingFilter extends ZinggSparkTester {
	
	DeterministicMatchingFilter<Dataset<Row>,Row,Column> deterministicMatchingFilter = new DeterministicMatchingFilter<Dataset<Row>,Row,Column>();
	
	@Test
	public void testGetDeterministicMatchingFilter() throws ZinggClientException {	
	
		SparkFrame posDF = getPosPairDF();
				
		Column filter = deterministicMatchingFilter.getDeterministicMatchingFilter(posDF,getdeterministicMatchingCond(),null);
		
		String expectedCond = "(((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL))))";
		
		assertEquals(expectedCond,filter.toString());
		
	}

	@Test
	public void testGetDeterministicMatchingFilterWithExtraCond() throws ZinggClientException {	
		SparkFrame posDF = getPosPairDF();
		Column gtCond = posDF.gt("z_zid");
		
		Column filter = deterministicMatchingFilter.getDeterministicMatchingFilter(posDF,getdeterministicMatchingCond(),gtCond);
		
		System.out.println(filter.toString());
		
		String expectedCond = "((((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL)))) AND (z_zid > z_z_zid))";
		
		assertEquals(expectedCond,filter.toString());
		
	}
	
	@Test
	public void testGetReverseDeterministicMatchingFilter() throws ZinggClientException {	

		SparkFrame posDF = getPosPairDF();
		DeterministicMatching[] deterministicMatching = getdeterministicMatchingCond();		
				
				
		Column filter = deterministicMatchingFilter.getReverseDeterministicMatchingFilter(posDF,deterministicMatching,null);
		
		String expectedCond = "(NOT (((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL)))))";
		
		assertEquals(expectedCond,filter.toString());
		
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
				RowFactory.create( ++row_id, "52",   "nameDeterministicMatching1"     ,"def"    ,"geh"    ,1900,   new Integer(1900), 0,++row_id, "410",   "nameDeterministicMatching1",    "lmn",    "opq",       2001,       new Integer(1900), 0),
				RowFactory.create( ++row_id, "53",   "nameDeterministicMatching2"     ,"eventDeterministicMatching2"    ,"commentDeterministicMatching2"    ,1900,   new Integer(1900), 0,++row_id, "54",   "nameDeterministicMatching2",    "eventDeterministicMatching2",    "commentDeterministicMatching2",       2001,       new Integer(1901), 0),
				RowFactory.create( ++row_id, "53",   "nameDeterministicMatching3"     ,"eventDeterministicMatching3"    ,"commentDeterministicMatching3"    ,1900,   new Integer(1901), 0,++row_id, "54",   "nameDeterministicMatching3",    "eventDeterministicMatching3",    "commentDeterministicMatching3",       2001,       new Integer(1901), 0),
				RowFactory.create( ++row_id, "53",   "nameDeterministicMatching3"     ,"eventDeterministicMatching3"    ,"commentDeterministicMatching3"    ,1900,   new Integer(1901), 0,++row_id, "54",   null,    "eventDeterministicMatching3",    "commentDeterministicMatching3",       2001,       new Integer(1901), 0)
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
	
	protected DeterministicMatching getDeterministicMatching(String field) {
		return getDeterministicMatching(new String[] {field});
	}

	protected DeterministicMatching getDeterministicMatching(String[] fields) {
		HashMap<String, String>[]  matchCondition = new HashMap[fields.length];		
		for (int i = 0; i < fields.length; i++) {
			matchCondition[i] = new HashMap<String, String>();
			matchCondition[i].put(DeterministicMatching.fieldName,fields[i]);
		}
		return new DeterministicMatching(matchCondition);
	}
	
	public DeterministicMatching[] getdeterministicMatchingCond() {
		DeterministicMatching deterministicMatching1 = getDeterministicMatching(new String[]{"name","event","comment"});
		DeterministicMatching deterministicMatching2 = getDeterministicMatching("dob");
		DeterministicMatching deterministicMatching3 = getDeterministicMatching(new String[]{"comment","year"});
		DeterministicMatching[] deterministicMatching = new DeterministicMatching[] {deterministicMatching1,deterministicMatching2,deterministicMatching3};
		return deterministicMatching;
	}


}

