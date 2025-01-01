package zingg.spark.core.documenter;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import freemarker.template.Version;
import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.core.context.IContext;
import zingg.common.client.IZArgs;
import zingg.common.core.context.Context;
import zingg.common.client.IArguments;
import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.RowWrapper;
import org.apache.spark.sql.SparkSession;

/**
 * Spark specific implementation of DataDocumenter
 *
 */
public class SparkDataDocumenter extends DataDocumenter<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;

	public SparkDataDocumenter(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context, IArguments args, ClientOptions options) {
		super(context, args, options);
	}

	@Override
	public RowWrapper<Row> getRowWrapper(Version v) {
		return new SparkRowWrapper(v);
	}

	

}
