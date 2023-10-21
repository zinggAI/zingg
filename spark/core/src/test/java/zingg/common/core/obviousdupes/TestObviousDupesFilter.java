package zingg.common.core.obviousdupes;

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

import zingg.common.client.ObviousDupes;
import zingg.common.client.ZinggClientException;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.executor.ZinggSparkTester;

public class TestObvDupeUtil extends ZinggSparkTester {
	
	ObvDupeUtil<Dataset<Row>,Row,Column> obvDupeUtil = new ObvDupeUtil<Dataset<Row>,Row,Column>();
	
	@Test
	public void testGetObviousDupesFilter() throws ZinggClientException {	
	
		SparkFrame posDF = getPosPairDF();
				
		Column filter = obvDupeUtil.getObviousDupesFilter(posDF,getObvDupeCond(),null);
		
		String expectedCond = "(((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL))))";
		
		assertEquals(expectedCond,filter.toString());
		
	}

	@Test
	public void testGetObviousDupesFilterWithExtraCond() throws ZinggClientException {	
		SparkFrame posDF = getPosPairDF();
		Column gtCond = posDF.gt("z_zid");
		
		Column filter = obvDupeUtil.getObviousDupesFilter(posDF,getObvDupeCond(),gtCond);
		
		System.out.println(filter.toString());
		
		String expectedCond = "((((((((name = z_name) AND (name IS NOT NULL)) AND (z_name IS NOT NULL)) AND (((event = z_event) AND (event IS NOT NULL)) AND (z_event IS NOT NULL))) AND (((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL))) OR (((dob = z_dob) AND (dob IS NOT NULL)) AND (z_dob IS NOT NULL))) OR ((((comment = z_comment) AND (comment IS NOT NULL)) AND (z_comment IS NOT NULL)) AND (((year = z_year) AND (year IS NOT NULL)) AND (z_year IS NOT NULL)))) AND (z_zid > z_z_zid))";
		
		assertEquals(expectedCond,filter.toString());
		
	}
	
	@Test
	public void testGetReverseObviousDupesFilter() throws ZinggClientException {	

		SparkFrame posDF = getPosPairDF();
		ObviousDupes[] obvDupe = getObvDupeCond();		
				
				
		Column filter = obvDupeUtil.getReverseObviousDupesFilter(posDF,obvDupe,null);
		
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

