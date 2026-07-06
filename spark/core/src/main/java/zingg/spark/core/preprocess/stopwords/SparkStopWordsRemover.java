package zingg.spark.core.preprocess.stopwords;

import static org.apache.spark.sql.functions.regexp_replace;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.stopwords.StopWordsRemover;
import zingg.spark.client.SparkFrame;
import org.apache.spark.sql.SparkSession;

public class SparkStopWordsRemover extends StopWordsRemover<SparkSession,Dataset<Row>,Row,Column,DataType>  implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.preprocess.SparkStopWordsRemover";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRemover.class);

	public SparkStopWordsRemover(){
    }

	public SparkStopWordsRemover(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context) {
		super(context);
	}

	public SparkStopWordsRemover(IContext<SparkSession, Dataset<Row>, Row, Column,DataType> context, FieldDefinition fd) {
		super(context,fd);
	}
	
 	@Override
	protected ZFrame<Dataset<Row>, Row, Column> removeStopWordsFromDF(ZFrame<Dataset<Row>, Row, Column> ds,
			String fieldName, String pattern) {
		Dataset<Row> dfAfterRemoval = ds.df().withColumn(fieldName, regexp_replace(ds.df().col(fieldName), pattern, ""));

		return new SparkFrame(dfAfterRemoval);
	}

	@Override
	public void init() {
	}

}
