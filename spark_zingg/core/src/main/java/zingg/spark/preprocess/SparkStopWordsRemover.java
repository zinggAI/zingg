package zingg.spark.preprocess;

import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.lit;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.Arguments;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.common.Context;
import zingg.preprocess.StopWordsRemover;

public class SparkStopWordsRemover extends StopWordsRemover<SparkSession,Dataset<Row>,Row,Column,DataType>  implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.preprocess.SparkStopWordsRemover";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRemover.class);
	
	private String udfName;
	
	public SparkStopWordsRemover(Context<SparkSession, Dataset<Row>, Row, Column,DataType> context,Arguments args) {
		super(context,args);
		this.udfName = registerUDF();
	}
	
 	@Override
	protected ZFrame<Dataset<Row>, Row, Column> removeStopWordsFromDF(ZFrame<Dataset<Row>, Row, Column> ds,
			String fieldName, String pattern) {
 		Dataset<Row> dfAfterRemoval = ds.df().withColumn(fieldName,callUDF(udfName, ds.df().col(fieldName),lit(pattern)));
		return new SparkFrame(dfAfterRemoval);
	}

	protected String registerUDF() {
		RemoveStopWordsUDF removeStopWordsUDF = new RemoveStopWordsUDF();
		// Each field will have different pattern
		String udfName = removeStopWordsUDF.getName();
		// register the UDF
		getContext().getSession().udf().register(udfName, removeStopWordsUDF, DataTypes.StringType);
		return udfName;
	}

}
