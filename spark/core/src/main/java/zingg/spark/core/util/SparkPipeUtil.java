package zingg.spark.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
//import zingg.scala.DFUtil;
import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.spark.client.SparkFrame;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.pipe.FilePipe;
//import zingg.common.client.pipe.InMemoryPipe;
import zingg.common.client.pipe.Pipe;
import zingg.spark.client.pipe.SparkPipe;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

//import com.datastax.driver.core.ProtocolVersion;
//import com.datastax.driver.core.ResultSet;
//import com.datastax.driver.core.Session;
//import com.datastax.spark.connector.DataFrameFunctions;
// import com.datastax.spark.connector.cql.CassandraConnector;
// import com.datastax.spark.connector.cql.ClusteringColumn;
// import com.datastax.spark.connector.cql.ColumnDef;
// import com.datastax.spark.connector.cql.TableDef;

// import com.datastax.spark.connector.cql.*;
import zingg.scala.DFUtil;
import zingg.util.PipeUtil;
import zingg.util.DFReader;
import zingg.util.DFWriter;


//import com.datastax.spark.connector.cql.*;
//import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;
//import zingg.scala.DFUtil;

public class SparkPipeUtil extends PipeUtil<SparkSession, Dataset<Row>, Row, Column>{

	
	public  final Log LOG = LogFactory.getLog(SparkPipeUtil.class);
	//private SparkDFReader reader;
	
	public SparkPipeUtil(SparkSession spark) {
		super(spark);
		
	}
	
	public SparkSession getSession(){
		return this.session;
	}

	public void setSession(SparkSession session){
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