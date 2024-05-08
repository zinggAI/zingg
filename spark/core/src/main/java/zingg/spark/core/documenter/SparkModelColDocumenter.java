package zingg.spark.core.documenter;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import freemarker.template.Version;
import zingg.common.client.Arguments;
import zingg.common.core.context.Context;
import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.core.documenter.ModelColDocumenter;
import zingg.common.core.documenter.RowWrapper;
import org.apache.spark.sql.SparkSession;

/**
 * Spark specific implementation of ModelColDocumenter
 * 
 *
 */
public class SparkModelColDocumenter extends ModelColDocumenter<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;

	public SparkModelColDocumenter(Context<SparkSession, Dataset<Row>, Row, Column,DataType> context, IZArgs args) {
		super(context, args);
	}

	@Override
	public RowWrapper<Row> getRowWrapper(Version v) {
		return new SparkRowWrapper(v);
	}

}
