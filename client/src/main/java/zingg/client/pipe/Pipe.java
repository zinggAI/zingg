package zingg.client.pipe;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang3.Validate;
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
@JsonDeserialize(builder = Pipe.Builder.class)
public class Pipe implements Serializable{
	
	public final String name;
	public final Format format;
	public final String preprocessors;
	public final Map<String, String> props;
	@JsonSerialize(using = CustomSchemaSerializer.class)
	public final StructType schema;
	public final Map<String, String> sparkProps;
	public final Map<String, String> addProps;
	public final SaveMode mode;
	public final int id;


	protected Pipe(
				String name,
				Format format,
				String preprocessors,
				StructType schema,
				Map<String, String> props,
				Map<String, String> sparkProps,
				Map<String, String> addProps,
				SaveMode mode,
				int id) {
		this.name = name;
		this.format = format;
		this.preprocessors = preprocessors;
		this.schema = schema;
		this.props = props;
		this.sparkProps = sparkProps;
		this.addProps = addProps;
		this.mode = mode;
		this.id = id;
	}

	public SaveMode getMode() {
		return mode;
	}

	public String getName() {
		return name;
	}
	
	public Format getFormat() {
		return format;
	}

	public Map<String, String> getProps() {
		return props;
	}
	
	public StructType getSchema() {
		return schema;
	}
	
	public String get(String key) {
		return props.get(key);
	}

	public String getPreprocessors() {
		return preprocessors;
	}

	public Map<String, String> getSparkProps() {
		return sparkProps;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Pipe [name=" + name + ", format=" + format + ", preprocessors="
				+ preprocessors + ", props=" + props + ", schema=" + schema + "]";
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


	@JsonPOJOBuilder
	static class Builder implements PipeBuilder{
		String name;
		Format format;
		String preprocessors;
		Map<String, String> props = new HashMap<>();
		StructType schema = null;
		Map<String, String> sparkProps = new HashMap<>();
		Map<String, String> addProps = new HashMap<>();
		SaveMode mode;
		Integer id;

		public Pipe build(){
			Validate.notNull(format, "The format must be specified to create a pipe");
			Validate.notNull(id, "The id must be specified to create a pipe");
			Validate.notNull(mode, "The savemode must be specified to create a pipe");
			Validate.notNull(id, "The id must be specified to create a pipe");
			return new Pipe(
					name,
					format,
					preprocessors,
					schema,
					Collections.unmodifiableMap(props),
					Collections.unmodifiableMap(sparkProps),
					Collections.unmodifiableMap(addProps),
					mode,
					id
			);
		}

	}


	

	
	
}
