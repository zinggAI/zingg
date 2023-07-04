package zingg.spark.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import zingg.common.client.ZFrame;
//import zingg.common.client.pipe.InMemoryPipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.core.util.DFReader;
import zingg.common.core.util.DFWriter;
import zingg.common.core.util.PipeUtil;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;


//import com.datastax.spark.connector.cql.*;
//import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;
//import zingg.scala.DFUtil;

public class SparkPipeUtil extends PipeUtil<ZSparkSession, Dataset<Row>, Row, Column>{

	
	public  final Log LOG = LogFactory.getLog(SparkPipeUtil.class);
	//private SparkDFReader reader;
	
	public SparkPipeUtil(ZSparkSession spark) {
		super(spark);
		
	}
	
	public ZSparkSession getSession(){
		return this.session;
	}

	public void setSession(ZSparkSession session){
		this.session = session;
	}

	public DFReader<Dataset<Row>, Row, Column> getReader() {
		SparkDFReader reader = new SparkDFReader(this.session);
		return reader;
	}

	public DFWriter<Dataset<Row>, Row, Column> getWriter(ZFrame<Dataset<Row>, Row, Column> toWrite){
		return new SparkDFWriter(toWrite);
	}

	
	public ZFrame<Dataset<Row>, Row, Column> addLineNo (ZFrame<Dataset<Row>, Row, Column> input) {
		return new SparkFrame(new SparkDSUtil(getSession()).addRowNumber(input).df());

	}

	public ZFrame<Dataset<Row>, Row, Column> getZFrame(ZFrame<Dataset<Row>, Row, Column> z) {
		return new SparkFrame(z.df());
	}

	public Pipe<Dataset<Row>, Row, Column> setOverwriteMode(Pipe<Dataset<Row>, Row, Column> p) {
		p.setMode(SaveMode.Overwrite.toString());
		return p;
	}

	
}