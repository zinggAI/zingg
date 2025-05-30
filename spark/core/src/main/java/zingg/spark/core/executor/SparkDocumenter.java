package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ClientOptions;
import zingg.common.client.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.ModelDocumenter;
import zingg.common.core.executor.Documenter;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.documenter.SparkDataDocumenter;
import zingg.spark.core.documenter.SparkModelDocumenter;
import zingg.spark.core.context.ZinggSparkContext;


public class SparkDocumenter extends Documenter<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkDocumenter";
	public static final Log LOG = LogFactory.getLog(SparkDocumenter.class);

	public SparkDocumenter() {
		this(new ZinggSparkContext());
	}

	public SparkDocumenter(ZinggSparkContext sparkContext) {
		setZinggOption(ZinggOptions.GENERATE_DOCS);
		setContext(sparkContext);
	}	
	
	@Override
	public void init(IZArgs args, SparkSession s, ClientOptions options)  throws ZinggClientException {
		super.init(args,s,options);
		getContext().init(s);
	}
	
	@Override
	public ModelDocumenter<SparkSession, Dataset<Row>, Row, Column, DataType> getModelDocumenter() {
		return new SparkModelDocumenter(getContext(),getArgs(), getClientOptions());
	}


	@Override
	public DataDocumenter<SparkSession, Dataset<Row>, Row, Column, DataType> getDataDocumenter() {
		return new SparkDataDocumenter(getContext(),getArgs(), getClientOptions());
	}

	
}
