package zingg.spark.preprocess;

import static org.apache.spark.sql.functions.callUDF;

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
import zingg.preprocess.StopWords;

public class SparkStopWords extends StopWords<SparkSession,Dataset<Row>,Row,Column,DataType>  implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.preprocess.SparkStopWords";
	public static final Log LOG = LogFactory.getLog(SparkStopWords.class);
	
	public SparkStopWords(Context<SparkSession, Dataset<Row>, Row, Column,DataType> context,Arguments args) {
		super(context,args);
	}
	
 	@Override
	protected ZFrame<Dataset<Row>, Row, Column> removeStopWordsFromDF(ZFrame<Dataset<Row>, Row, Column> ds,
			String fieldName, String pattern) {
		
		RemoveStopWordsUDF removeStopWordsUDF = new RemoveStopWordsUDF(pattern.toLowerCase());
		// Each field will have different pattern
		String udfName = removeStopWordsUDF.getName()+"_"+fieldName;
		// register the UDF
		getContext().getSession().udf().register(udfName, removeStopWordsUDF, DataTypes.StringType);
		
		Dataset<Row> dfAfterRemoval = ds.df().withColumn(fieldName,callUDF(udfName, ds.df().col(fieldName)));
		
		return new SparkFrame(dfAfterRemoval);
	}

}
