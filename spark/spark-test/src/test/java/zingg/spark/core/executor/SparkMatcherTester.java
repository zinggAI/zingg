package zingg.spark.core.executor;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.executor.Matcher;
import zingg.common.core.executor.MatcherTester;

public class SparkMatcherTester extends MatcherTester<SparkSession,Dataset<Row>,Row,Column,DataType> {

	public SparkMatcherTester(Matcher<SparkSession, Dataset<Row>, Row, Column, DataType> executor) {
		super(executor);
	}

}
