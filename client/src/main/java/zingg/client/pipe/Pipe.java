package zingg.client.pipe;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// import org.apache.spark.sql.Dataset;
// import org.apache.spark.sql.SaveMode;
// import org.apache.spark.sql.types.DataType;
// import org.apache.spark.sql.types.StructType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
// import org.apache.spark.sql.Row;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


/**Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries etc
 * 
 * @author sgoyal
 *
 */

@JsonInclude(Include.NON_NULL)
public class Pipe<D,R,C,T, St, Sv> implements Serializable{ // St:StructType, Sv:SaveMode
	
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
	@JsonSerialize(using = CustomSchemaSerializer.class)
	St schema = null;
	Sv mode;
	int id;
	D dataset;

	public Sv getMode() {
		return mode;
	}


	public void setMode(Sv mode) {
		this.mode = mode;
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
	public Map<String, String> getProps() {
		return props;
	}
	@JsonValue
	public void setProps(Map<String, String> props) {
		this.props = props;
	}
	
	public void setProp(String k, String v) {
		if (props == null) props = new HashMap<String, String>();
		this.props.put(k, v);
	}
	
	public void clone(Pipe p) {
		this.name = p.name;
		this.format = p.format;
		this.props = p.props;		
	}
	
	@JsonProperty("schema")
	public void setSchema(String s) {
		if (s!= null) this.schema = (St) D.fromJson(s);
		//schema = DataTypes.createStructType(s);
	}
	
	/*
	public void setSchema(JsonNode s) {
		System.out.println("reached json node");
		if (s!= null) this.schema = (StructType) DataType.fromJson(s.toString());
	}*/
	
	
	public St getSchema() {
		return schema;
	}
	
	public String get(String key) {
		return props.get(key);
	}
	
	public void setSchemaStruct(St s) {
		this.schema = s;
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

	public D getDataset(){
		return this.dataset;
	}

	public void setDataset(D ds){
		this.dataset = ds;
	}

	@Override
	public String toString() {
		return "Pipe [name=" + name + ", format=" + format + ", preprocessors="
				+ preprocessors + ", props=" + props + ", schema=" + schema + "]";
	}
	
	public void nullifySchema() {
		this.schema = null;
	}
	
	static class CustomSchemaSerializer extends StdSerializer<St> {
		 
	     public CustomSchemaSerializer() { 
	        this(null); 
	    } 
	 
	    public CustomSchemaSerializer(Class<St> t) {
	        super(t); 
	    }
	 
	    @Override
	    public void serialize(
	    		St value, JsonGenerator gen, SerializerProvider arg2) 
	      throws IOException, JsonProcessingException {
	        gen.writeObject(value.json());
	    }
	}

	

	
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
	
}