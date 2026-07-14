package zingg.spark.core.documenter;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import freemarker.template.Version;
import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.core.context.IContext;
import zingg.common.core.documenter.ModelDocumenter;
import zingg.common.core.documenter.RowWrapper;
import org.apache.spark.sql.SparkSession;

/**
 * Spark specific implementation of ModelDocumenter
 *
 */
public class SparkModelDocumenter extends ModelDocumenter<SparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;

	public SparkModelDocumenter(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context, IArguments args, ClientOptions c) {
		super(context, args, c);
		super.modelColDoc = new SparkModelColDocumenter(context,args, c);
	}

	@Override
	public RowWrapper<Row> getRowWrapper(Version v) {
		return new SparkRowWrapper(v);
	}

}
