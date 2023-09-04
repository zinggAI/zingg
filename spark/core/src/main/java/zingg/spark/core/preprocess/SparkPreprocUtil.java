package zingg.spark.core.preprocess;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.preprocess.PreprocUtil;
import zingg.common.core.util.PipeUtilBase;
import zingg.spark.client.ZSparkSession;

public class SparkPreprocUtil extends PreprocUtil<ZSparkSession, Dataset<Row>, Row, Column,DataType> {
	
	public SparkPreprocUtil(ZSparkSession s, PipeUtilBase pipeUtil) {
		this.session = s;
		this.pipeUtil = pipeUtil;
		this.preprocFactory = new SparkPreprocFactory();
	}

}
