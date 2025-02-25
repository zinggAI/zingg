package zingg.spark.core.preprocess.stopwords;

import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.lit;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.stopwords.StopWordsRemover;
import zingg.spark.client.SparkFrame;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.util.SparkFnRegistrar;

public class SparkStopWordsRemover extends StopWordsRemover<SparkSession,Dataset<Row>,Row,Column,DataType>  implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.preprocess.SparkStopWordsRemover";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRemover.class);
	
	private String udfName;

	public SparkStopWordsRemover(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context) {
		super(context);
		registerUDF();
	}

	public SparkStopWordsRemover(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context, FieldDefinition fd) {
		super(context,fd);
		registerUDF();
	}
	
 	@Override
	public ZFrame<Dataset<Row>, Row, Column> removeStopWordsFromDF(ZFrame<Dataset<Row>, Row, Column> ds,
			String fieldName, String pattern) {
		Dataset<Row> dfAfterRemoval = ds.df().withColumn(fieldName,callUDF(udfName, ds.df().col(fieldName),lit(pattern)));

		return new SparkFrame(dfAfterRemoval);
	}

	protected void registerUDF() {
		RemoveStopWordsUDF removeStopWordsUDF = new RemoveStopWordsUDF();
		// Each field will have different pattern
		this.udfName = removeStopWordsUDF.getName();
		// register the UDF
		SparkSession zSession = getContext().getSession();

		SparkFnRegistrar.registerUDF2(zSession, udfName, removeStopWordsUDF, DataTypes.StringType);
	}

	@Override
	public void init() {
		registerUDF();
	}

}
