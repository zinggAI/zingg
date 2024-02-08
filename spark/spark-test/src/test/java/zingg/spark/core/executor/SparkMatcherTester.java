package zingg.spark.core.executor;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZFrame;
import zingg.common.core.executor.Matcher;
import zingg.common.core.executor.MatcherTester;
import zingg.spark.client.SparkFrame;

public class SparkMatcherTester extends MatcherTester<SparkSession,Dataset<Row>,Row,Column,DataType> {

	public SparkMatcherTester(Matcher<SparkSession, Dataset<Row>, Row, Column, DataType> executor) {
		super(executor);
	}

	@Override
	public ZFrame<Dataset<Row>, Row, Column> intersect(ZFrame<Dataset<Row>, Row, Column> df1,
			ZFrame<Dataset<Row>, Row, Column> df2) {
		return new SparkFrame(df1.df().intersect(df2.df()));
	}

	@Override
	public Column substr(Column col, int startPos, int len) {
		return col.substr(startPos,len);
	}

	@Override
	public Column gt(Column column1, Column column2) {
		return column1.gt(column2);
	}

}
