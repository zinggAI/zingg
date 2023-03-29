package zingg.common.client.pipe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

import zingg.common.client.ZFrame;


/**Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries etc
 * 
 * @author sgoyal
 *
 */

@JsonInclude(Include.NON_NULL)
public class Pipe<D,R,C> implements Serializable{ // St:StructType, Sv:SaveMode
	
	public static final String FORMAT_CSV = "csv";
	public static final String FORMAT_PARQUET = "parquet";
	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_TEXT = "text";
	public static final String FORMAT_XLS = "com.crealytics.spark.excel";
	public static final String FORMAT_AVRO = "avro";
	public static final String FORMAT_JDBC = "jdbc";
	public static final String FORMAT_CASSANDRA = "org.apache.spark.sql.cassandra";
	public static final String FORMAT_SNOWFLAKE = "net.snowflake.spark.snowflake";
	public static final String FORMAT_ELASTIC = "org.elasticsearch.spark.sql";
	public static final String FORMAT_BIGQUERY = "bigquery";
	public static final String FORMAT_INMEMORY = "inMemory";

	String name;
	String format;
	String preprocessors;
	Map<String, String> props = new HashMap<String, String>();
	int id;
	protected ZFrame<D, R, C> dataset;
	String schema;
	String mode;

	

	

	public String getSchema() {
		return schema;
	}


	public void setSchema(String schema) {
		this.schema = schema;
	}


	public String getName() {
		return name;
	}
	
	
	@JsonValue
	public void setName(String name) {
		this.name = name;		
	}
	
	public String getFormat() {
		return format;
	}
	
	@JsonValue
	public void setFormat(String sinkType) {
		this.format = sinkType;
	}

	
	@JsonValue
	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	public Map<String, String> getProps() {
		return props;
	}
	
	public void setProp(String k, String v) {
		if (props == null) props = new HashMap<String, String>();
		this.props.put(k, v);
	}
	
	public void clone(Pipe<D,R,C> p) {
		this.name = p.name;
		this.format = p.format;
		this.props = p.props;		
	}
	
	public String get(String key) {
		return props.get(key);
	}
	
	
	public String getPreprocessors() {
		return preprocessors;
	}


	public void setPreprocessors(String preprocessors) {
		this.preprocessors = preprocessors;
	}



	public int getId() {
		return id;
	}


	public void setId(int recId) {
		this.id = recId;
	}

	public ZFrame<D, R, C> getDataset(){
		return this.dataset;
	}

	public void setDataset(ZFrame<D, R, C> ds){
		this.dataset = ds;
	}

	@Override
	public String toString() {
		return "Pipe [name=" + name + ", format=" + format + ", preprocessors="
				+ preprocessors + ", props=" + props + "]";
	}
	
	public String getMode(){
		return mode;
	}

	public void setMode(String s) {
		this.mode = s;
	}

	/* 
	public Pipe clone() {
		Pipe p = new Pipe();
		p.name = name;
		p.format = format;
		p.preprocessors = preprocessors;
		p.props = props;
		p.schema = schema;
		p.mode = mode;
		p.id = id;
		p.dataset = dataset;
		return p;
	}
	*/
	
}