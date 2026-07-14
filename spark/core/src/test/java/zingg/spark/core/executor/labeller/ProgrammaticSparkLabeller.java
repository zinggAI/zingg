package zingg.spark.core.executor.labeller;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.executor.labeller.ProgrammaticLabeller;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.SparkLabeller;
import zingg.spark.core.preprocess.ISparkPreprocMapSupplier;

public class ProgrammaticSparkLabeller extends SparkLabeller implements ISparkPreprocMapSupplier {

	private static final long serialVersionUID = 1L;

	ProgrammaticLabeller<SparkSession,Dataset<Row>,Row,Column,DataType> programmaticLabeller;
	
	public ProgrammaticSparkLabeller() {
		this(new ZinggSparkContext());
	}

	public ProgrammaticSparkLabeller(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.LABEL);
		setContext(sparkContext);
		programmaticLabeller = new ProgrammaticLabeller<SparkSession,Dataset<Row>,Row,Column,DataType>(sparkContext);
	}
	
	@Override
	public void setArgs(IZArgs args) {
		super.setArgs(args);
		programmaticLabeller.setArgs(args);
	}
	
	@Override
	public ZFrame<Dataset<Row>,Row,Column> processRecordsCli(ZFrame<Dataset<Row>,Row,Column> lines)
			throws ZinggClientException {
		return programmaticLabeller.processRecordsCli(lines);
	}
}

