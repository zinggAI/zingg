package zingg.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.executor.ZinggSparkTester;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.core.util.SparkHashUtil;

public class TestBlock  extends ZinggSparkTester {
	
	@Test
	public void testTree() throws Throwable {
		
		ZFrame<Dataset<Row>, Row, Column> testData = getTestData();

		ZFrame<Dataset<Row>, Row, Column> posDf = getPosData();

		Arguments args = getArguments();

		// form tree
		SparkBlockingTreeUtil blockingTreeUtil = new SparkBlockingTreeUtil(zSession, zsCTX.getPipeUtil());
		SparkHashUtil hashUtil = new SparkHashUtil(zSession);

		Tree<Canopy<Row>> blockingTree = blockingTreeUtil.createBlockingTreeFromSample(testData, posDf, 0.5, -1,
				args, hashUtil.getHashFunctionList());
				
		// primary deciding is unique year so identityInteger should have been picked
		Canopy<Row> head = blockingTree.getHead();
		assertEquals("identityInteger", head.getFunction().getName());
				
	}

	StructType testDataSchema = new StructType(new StructField[] {
			new StructField("id", DataTypes.IntegerType, false, Metadata.empty()),
			new StructField("year", DataTypes.IntegerType, false, Metadata.empty()), 
			new StructField("event", DataTypes.StringType, false, Metadata.empty()),
			new StructField("comment", DataTypes.StringType, false, Metadata.empty())}
		);
	
	StructType schemaPos = new StructType(new StructField[] {
			new StructField("id", DataTypes.IntegerType, false, Metadata.empty()),
			new StructField("year", DataTypes.IntegerType, false, Metadata.empty()), 
			new StructField("event", DataTypes.StringType, false, Metadata.empty()),
			new StructField("comment", DataTypes.StringType, false, Metadata.empty()), 
			new StructField("z_year", DataTypes.IntegerType, false, Metadata.empty()), 
			new StructField("z_event", DataTypes.StringType, false, Metadata.empty()),
			new StructField("z_comment", DataTypes.StringType, false, Metadata.empty()),
			new StructField("z_zid", DataTypes.StringType, false, Metadata.empty())}
		);
	
	
	
	
	private Arguments getArguments() throws ZinggClientException {
		String configFilePath = getClass().getResource("../../testFebrl/config.json").getFile();
		
		Arguments args = Arguments.createArgumentsFromJSON(configFilePath, "trainMatch");

		List<FieldDefinition> fdList = getFieldDefList();

		args.setFieldDefinition(fdList);
		return args;
	}

	private List<FieldDefinition> getFieldDefList() {
		List<FieldDefinition> fdList = new ArrayList<FieldDefinition>(4);

		FieldDefinition idFD = new FieldDefinition();
		idFD.setDataType("integer");
		idFD.setFieldName("id");
		ArrayList<MatchType> matchTypelistId = new ArrayList<MatchType>();
		matchTypelistId.add(MatchType.DONT_USE);
		idFD.setMatchType(matchTypelistId);
		fdList.add(idFD);
		
		ArrayList<MatchType> matchTypelistFuzzy = new ArrayList<MatchType>();
		matchTypelistFuzzy.add(MatchType.FUZZY);

		
		FieldDefinition yearFD = new FieldDefinition();
		yearFD.setDataType("integer");
		yearFD.setFieldName("year");
		yearFD.setMatchType(matchTypelistFuzzy);
		fdList.add(yearFD);
		
		FieldDefinition eventFD = new FieldDefinition();
		eventFD.setDataType("string");
		eventFD.setFieldName("event");
		eventFD.setMatchType(matchTypelistFuzzy);
		fdList.add(eventFD);
		
		FieldDefinition commentFD = new FieldDefinition();
		commentFD.setDataType("string");
		commentFD.setFieldName("comment");
		commentFD.setMatchType(matchTypelistFuzzy);
		fdList.add(commentFD);
		return fdList;
	}
	
	public SparkFrame getTestData() {
		int row_id = 1;
		// Create a DataFrame containing test data
		Row[] data = { 
				RowFactory.create(row_id++, new Integer(1942), "quit India", "Mahatma Gandhi"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"), 
				RowFactory.create(row_id++, new Integer(1930), "Civil Disob", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit India", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidience", "India"),
				RowFactory.create(row_id++, new Integer(1942), "Quit Bharat", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidence", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit Hindustan", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JW", "Amritsar"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Dis", "India") ,
				RowFactory.create(row_id++, new Integer(1942), "quit Nation", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm"), 
				RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama"),
				RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"), 
				RowFactory.create(row_id++, new Integer(1930), "Civil Disob", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit India", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidience", "India"),
				RowFactory.create(row_id++, new Integer(1942), "Quit Bharat", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidence", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit Hindustan", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JW", "Amritsar"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Dis", "India") ,
				RowFactory.create(row_id++, new Integer(1942), "quit Nation", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm"), 
				RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama"),
				RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"), 
				RowFactory.create(row_id++, new Integer(1930), "Civil Disob", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit India", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidience", "India"),
				RowFactory.create(row_id++, new Integer(1942), "Quit Bharat", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidence", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit Hindustan", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JW", "Amritsar"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Dis", "India") ,
				RowFactory.create(row_id++, new Integer(1942), "quit Nation", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm"), 
				RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama"),
				RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"), 
				RowFactory.create(row_id++, new Integer(1930), "Civil Disob", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit India", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidience", "India"),
				RowFactory.create(row_id++, new Integer(1942), "Quit Bharat", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JallianWala", "Punjab"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Disobidence", "India"),
				RowFactory.create(row_id++, new Integer(1942), "quit Hindustan", "Mahatma Gandhi"), 
				RowFactory.create(row_id++, new Integer(1919), "JW", "Amritsar"),
				RowFactory.create(row_id++, new Integer(1930), "Civil Dis", "India") ,
				RowFactory.create(row_id++, new Integer(1942), "quit Nation", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma"),
				RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb"), 
				RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm"), 
				RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama"),
				RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma")				
		};
		
		return new SparkFrame(
				spark.createDataFrame(Arrays.asList(data), 
				testDataSchema));
		
	}

	private SparkFrame getPosData() {
		int row_id = 1000;
		// Create positive matching data
		Row[] posData = { 
					RowFactory.create(row_id++, new Integer(1942), "quit Nation", "Mahatma",new Integer(1942), "quit Nation", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma",new Integer(1942), "quit N", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm",new Integer(1942), "quit ", "Mahatm", "1"), 
					RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama",new Integer(1942), "quit Ntn", "Mahama", "1"),
					RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma",new Integer(1942), "quit Natin", "Mahaatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma",new Integer(1942), "quit N", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm",new Integer(1942), "quit ", "Mahatm", "1"), 
					RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama",new Integer(1942), "quit Ntn", "Mahama", "1"),
					RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma",new Integer(1942), "quit Natin", "Mahaatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma",new Integer(1942), "quit N", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm",new Integer(1942), "quit ", "Mahatm", "1"), 
					RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama",new Integer(1942), "quit Ntn", "Mahama", "1"),
					RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma",new Integer(1942), "quit Natin", "Mahaatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma",new Integer(1942), "quit N", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm",new Integer(1942), "quit ", "Mahatm", "1"), 
					RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama",new Integer(1942), "quit Ntn", "Mahama", "1"),
					RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma",new Integer(1942), "quit Natin", "Mahaatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma",new Integer(1942), "quit N", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm",new Integer(1942), "quit ", "Mahatm", "1"), 
					RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama",new Integer(1942), "quit Ntn", "Mahama", "1"),
					RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma",new Integer(1942), "quit Natin", "Mahaatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit N", "Mahatma",new Integer(1942), "quit N", "Mahatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2"), 
					RowFactory.create(row_id++, new Integer(1942), "quit ", "Mahatm",new Integer(1942), "quit ", "Mahatm", "1"), 
					RowFactory.create(row_id++, new Integer(1942), "quit Ntn", "Mahama",new Integer(1942), "quit Ntn", "Mahama", "1"),
					RowFactory.create(row_id++, new Integer(1942), "quit Natin", "Mahaatma",new Integer(1942), "quit Natin", "Mahaatma", "1"),
					RowFactory.create(row_id++, new Integer(1919), "JallianWal", "Punjb",new Integer(1919), "JallianWal", "Punjb", "2")				
				};
		return new SparkFrame(spark.createDataFrame(Arrays.asList(posData), schemaPos));
	}
	
	
}
