package zingg.spark.core.documenter;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import freemarker.template.Version;
import zingg.common.client.Arguments;
import zingg.common.core.Context;
import zingg.common.core.documenter.ModelColDocumenter;
import zingg.common.core.util.RowWrapper;
import zingg.spark.core.util.SparkRowWrapper;

/**
 * Spark specific implementation of ModelColDocumenter
 * 
 * @author vikasgupta
 *
 */
public class SparkModelColDocumenter extends ModelColDocumenter<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;

	public SparkModelColDocumenter(Context<SparkSession, Dataset<Row>, Row, Column,DataType> context, Arguments args) {
		super(context, args);
	}

	@Override
	public RowWrapper<Row> getRowWrapper(Version v) {
		return new SparkRowWrapper(v);
	}

}
