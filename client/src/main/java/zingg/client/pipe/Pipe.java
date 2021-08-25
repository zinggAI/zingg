package zingg.client.pipe;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

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
public class Pipe implements Serializable{
	
	String name;
	Format format;
	String preprocessors;
	Map<String, String> props;
	@JsonSerialize(using = CustomSchemaSerializer.class)
	StructType schema = null;
	Map<String, String> sparkProps;
	Map<String, String> addProps;
	SaveMode mode;
	int id;
	
	public SaveMode getMode() {
		return mode;
	}


	public void setMode(SaveMode mode) {
		this.mode = mode;
	}


	public String getName() {
		return name;
	}
	
	
	@JsonValue
	public void setName(String name) {
		this.name = name;		
	}
	
	public Format getFormat() {
		return format;
	}
	
	@JsonValue
	public void setFormat(Format sinkType) {
		this.format = sinkType;
		PipeFactory.register(name, this);
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
		this.sparkProps = p.sparkProps;
	}
	
	@JsonProperty("schema")
	public void setSchema(String s) {
		if (s!= null) this.schema = (StructType) DataType.fromJson(s);
		//schema = DataTypes.createStructType(s);
	}
	
	/*
	public void setSchema(JsonNode s) {
		System.out.println("reached json node");
		if (s!= null) this.schema = (StructType) DataType.fromJson(s.toString());
	}*/
	
	
	public StructType getSchema() {
		return schema;
	}
	
	public String get(String key) {
		return props.get(key);
	}
	
	public void setSchemaStruct(StructType s) {
		this.schema = s;
	}
	
	public String getPreprocessors() {
		return preprocessors;
	}


	public void setPreprocessors(String preprocessors) {
		this.preprocessors = preprocessors;
	}


	public Map<String, String> getSparkProps() {
		return sparkProps;
	}


	public void setSparkProps(Map<String, String> sparkProps) {
		this.sparkProps = sparkProps;
	}

	

	public int getId() {
		return id;
	}


	public void setId(int recId) {
		this.id = recId;
	}


	@Override
	public String toString() {
		return "Pipe [name=" + name + ", format=" + format + ", preprocessors="
				+ preprocessors + ", props=" + props + ", schema=" + schema + "]";
	}
	
	public void nullifySchema() {
		this.schema = null;
	}
	
	static class CustomSchemaSerializer extends StdSerializer<StructType> {
		 
	     public CustomSchemaSerializer() { 
	        this(null); 
	    } 
	 
	    public CustomSchemaSerializer(Class<StructType> t) {
	        super(t); 
	    }
	 
	    @Override
	    public void serialize(
	    		StructType value, JsonGenerator gen, SerializerProvider arg2) 
	      throws IOException, JsonProcessingException {
	        gen.writeObject(value.json());
	    }
	}

	public Map<String, String> getAddProps() {
		return addProps;
	}


	public void setAddProps(Map<String, String> addProps) {
		this.addProps = addProps;
	}

	
	public Pipe clone() {
		Pipe p = new Pipe();
		p.name = name;
		p.format = format;
		p.preprocessors = preprocessors;
		p.props = props;
		p.schema = schema;
		p.sparkProps = sparkProps;
		p.addProps = addProps;
		p.mode = mode;
		p.id = id;
		return p;
	}
	
	
}
