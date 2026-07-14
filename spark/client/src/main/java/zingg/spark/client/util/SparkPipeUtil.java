package zingg.spark.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.util.PipeUtil;
import org.apache.spark.sql.SparkSession;
import zingg.spark.client.util.reader.SparkPipeUtilReader;
import zingg.spark.client.util.writer.SparkPipeUtilWriter;

public class SparkPipeUtil extends PipeUtil<SparkSession, Dataset<Row>, Row, Column>{

	public  final Log LOG = LogFactory.getLog(SparkPipeUtil.class);

	public SparkPipeUtil(SparkSession sparkSession) {
		super(sparkSession, new SparkPipeUtilWriter(), new SparkPipeUtilReader(sparkSession));
		
	}
	
	public SparkSession getSession(){
		return this.session;
	}

	public void setSession(SparkSession session){
		this.session = session;
	}

}