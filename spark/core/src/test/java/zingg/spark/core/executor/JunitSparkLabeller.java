package zingg.spark.core.executor;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.executor.JunitLabeller;
import zingg.spark.core.context.ZinggSparkContext;

public class JunitSparkLabeller extends SparkLabeller {

	private static final long serialVersionUID = 1L;

	JunitLabeller<SparkSession,Dataset<Row>,Row,Column,DataType> junitLabeller;
	
	public JunitSparkLabeller() {
		this(new ZinggSparkContext());
	}

	public JunitSparkLabeller(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.LABEL);
		setContext(sparkContext);
		junitLabeller = new JunitLabeller<SparkSession,Dataset<Row>,Row,Column,DataType>(sparkContext);
	}
	
	@Override
	public void setArgs(IArguments args) {
		super.setArgs(args);
		junitLabeller.setArgs(args);
	}
	
	@Override
	public ZFrame<Dataset<Row>,Row,Column> processRecordsCli(ZFrame<Dataset<Row>,Row,Column> lines)
			throws ZinggClientException {
		return junitLabeller.processRecordsCli(lines);
	}
}

