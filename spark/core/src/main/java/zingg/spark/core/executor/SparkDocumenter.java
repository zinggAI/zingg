package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.ModelDocumenter;
import zingg.common.core.executor.Documenter;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.documenter.SparkDataDocumenter;
import zingg.spark.core.documenter.SparkModelDocumenter;


public class SparkDocumenter extends Documenter<ZSparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkDocumenter";
	public static final Log LOG = LogFactory.getLog(SparkDocumenter.class);

	public SparkDocumenter() {
		//setZinggOptions(ZinggOptions.GENERATE_DOCS);
		setContext(new ZinggSparkContext());
	}

	@Override
	public void init(Arguments args, IZinggLicense license)  throws ZinggClientException {
		super.init(args, license);
		getContext().init(license);
	}
	
	@Override
	protected ModelDocumenter<ZSparkSession, Dataset<Row>, Row, Column, DataType> getModelDocumenter() {
		return new SparkModelDocumenter(getContext(),getArgs());
	}


	@Override
	protected DataDocumenter<ZSparkSession, Dataset<Row>, Row, Column, DataType> getDataDocumenter() {
		return new SparkDataDocumenter(getContext(),getArgs());
	}

	
}
