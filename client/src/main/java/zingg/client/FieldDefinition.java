package zingg.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.annotation.JsonDeserialize; 
import com.fasterxml.jackson.databind.deser.std.StdDeserializer; 


/**
 * This class defines each field that we use in matching We can use this to
 * configure the properties of each field we use for matching in Zingg.
 * 
 * @author sgoyal
 *
 */
public class FieldDefinition implements 
		Serializable {

	public static final Log LOG = LogFactory.getLog(FieldDefinition.class);
	
	@JsonDeserialize(using = MatchTypeDeserializer.class) public List<MatchType> matchType;
	@JsonSerialize(using = DataTypeSerializer.class)
	public DataType dataType;
	public String fieldName;
	public String fields;
	public String stopWords;
	public String abbreviations;

	public FieldDefinition() {
	}

	public String getFields() { return fields; }

	public void setFields(String fields) { this.fields = fields;}	
	
	/**
	 * Get the field type of the class
	 * 
	 * @return the type
	 */
	public List<MatchType> getMatchType() {
		return matchType;
	}

	/**
	 * Set the field type which defines the kind of matching we want to do
	 * 
	 * @see MatchType
	 * @param type
	 *            the type to set
	*/
	@JsonDeserialize(using = MatchTypeDeserializer.class)	
	public void setMatchType(List<MatchType> type) {
		this.matchType = type; //MatchTypeDeserializer.getMatchTypeFromString(type);
	}

	
	public void setMatchTypeInternal(MatchType... type) {
		this.matchType = Arrays.asList(type);
	}
	
	
	public DataType getDataType() {
		return dataType;
	}

	@JsonProperty("dataType")
	public void setDataType(String d) {
		if (d!= null) this.dataType =  DataType.fromJson(d);
	}
	
	

	public String getStopWords() {
		return stopWords;
	}

	public void setStopWords(String stopWords) {
		this.stopWords = stopWords;
	}

	public String getAbbreviations() {
		return abbreviations;
	}

	public void setAbbreviations(String abbreviations) {
		this.abbreviations = abbreviations;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime
				* result;
		result = prime * result
				+ ((matchType == null) ? 0 : matchType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldDefinition other = (FieldDefinition) obj;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (matchType != other.matchType)
			return false;
		return true;
	}

	public static class DataTypeSerializer extends JsonSerializer<DataType>{
		@Override
	    public void serialize(DataType dType, JsonGenerator jsonGenerator, 
	        SerializerProvider serializerProvider) 
	        		throws IOException, JsonProcessingException {
				jsonGenerator.writeString(dType.json());
	        
		}
	}

	public static class MatchTypeDeserializer extends StdDeserializer<List<MatchType>> {
		private static final long serialVersionUID = 1L;
		
		public MatchTypeDeserializer() { 
		   this(null); 
		} 
		public MatchTypeDeserializer(Class<String> t) { 
		   super(t); 
		} 
		@Override 
		public List<MatchType> deserialize(JsonParser parser, DeserializationContext context) 
		   throws IOException, JsonProcessingException { 
			ObjectMapper mapper = new ObjectMapper();
			try{
				mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
				LOG.debug("Deserializing custom type");
				return getMatchTypeFromString(mapper.readValue(parser, String.class)); 
			}
			catch(ZinggClientException e) {
				throw new IOException(e);
			}
		}   

		public static List<MatchType> getMatchTypeFromString(String m) throws ZinggClientException{
			List<MatchType> matchTypes = new ArrayList<MatchType>();
		    String[] matchTypeFromConfig = m.split(","); 
			for (String s: matchTypeFromConfig) { 
				MatchType mt = MatchType.getMatchType(s);
				matchTypes.add(mt);
			}     
		   return matchTypes; 
		}
	}
	
	

}
