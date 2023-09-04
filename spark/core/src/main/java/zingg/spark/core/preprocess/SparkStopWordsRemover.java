package zingg.spark.core.preprocess;

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

import zingg.common.client.ZFrame;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;

public class SparkStopWordsRemover extends StopWordsRemover<ZSparkSession,Dataset<Row>,Row,Column,DataType>  implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.core.preprocess.SparkStopWordsRemover";
	public static final Log LOG = LogFactory.getLog(SparkStopWordsRemover.class);
	
 	@Override
	protected ZFrame<Dataset<Row>, Row, Column> removeStopWordsFromDF(ZSparkSession zSession, ZFrame<Dataset<Row>, Row, Column> ds,
			String fieldName, String pattern) {
 		String udfName = registerUDF(zSession);
 		Dataset<Row> dfAfterRemoval = ds.df().withColumn(fieldName,callUDF(udfName, ds.df().col(fieldName),lit(pattern)));
		return new SparkFrame(dfAfterRemoval);
	}

	protected String registerUDF(ZSparkSession zSession) {
		RemoveStopWordsUDF removeStopWordsUDF = new RemoveStopWordsUDF();
		// Each field will have different pattern
		String udfName = removeStopWordsUDF.getName();
		// register the UDF if not already present
		if (!zSession.getSession().catalog().functionExists(udfName)) {
			zSession.getSession().udf().register(udfName, removeStopWordsUDF, DataTypes.StringType);
		}
		return udfName;
	}

}
