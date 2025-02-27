package zingg.common.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


/**
 * This class defines each field that we use in matching We can use this to
 * configure the properties of each field we use for matching in Zingg.
 * 
 * @author sgoyal
 *
 */
public class FieldDefinition implements Named, Serializable {

	private static final long serialVersionUID = 1L;

	public static final Log LOG = LogFactory.getLog(FieldDefinition.class);
	
	@JsonDeserialize(using = MatchTypeDeserializer.class)
	@JsonSerialize(using = MatchTypeSerializer.class)
	public List<? extends IMatchType> matchType;
	
	//@JsonSerialize(using = DataTypeSerializer.class)
	public String dataType;
	public String fieldName;
	public String fields;
	public String stopWords;
	public String abbreviations;

	public FieldDefinition() {
	}

	public String getFields() { 
		return fields; 
	}

	public void setFields(String fields) {
		this.fields = fields;
	}	
	
	/**
	 * Get the field type of the class
	 * 
	 * @return the type
	 */
	public List<? extends IMatchType> getMatchType() {
		return this.matchType;
	}

	/**
	 * Set the field type which defines the kind of matching we want to do
	 * 
	 * @see MatchType
	 * @param type
	 *            the type to set
	*/
	@JsonDeserialize(using = MatchTypeDeserializer.class)	
	public void setMatchType(List<? extends IMatchType> type) {
		this.matchType = type; //MatchTypeDeserializer.getMatchTypeFromString(type);
	}

	
	public void setMatchTypeInternal(IMatchType... type) {
		this.matchType = Arrays.asList(type);
	}
	
	
	public String getDataType() {
		return dataType;
	}

	//@JsonProperty("dataType")
	public void setDataType(String d) {
		this.dataType = d;

		//if (d!= null) this.dataType =  DataType.fromJson(d);
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
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@JsonIgnore
	public boolean isDontUse() {
        return (matchType != null && matchType.contains(MatchTypes.DONT_USE));
    }

	@Override
    public String getName() {
        return getFieldName();
    }

    @Override
    public void setName(String name) {
        setFieldName(name);
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

	/* 
	public static class DataTypeSerializer extends JsonSerializer<DataType>{
		@Override
	    public void serialize(DataType dType, JsonGenerator jsonGenerator, 
	        SerializerProvider serializerProvider) 
	        		throws IOException, JsonProcessingException {
				jsonGenerator.writeString(dType.json());
	        
		}
	}*/

	public static class MatchTypeSerializer extends StdSerializer<List<IMatchType>> {
		public MatchTypeSerializer() {
			this(null);
		}

		public MatchTypeSerializer(Class<List<IMatchType>> t) {
			super(t);
		}

		@Override
		public void serialize(List<IMatchType> matchType, JsonGenerator jsonGen, SerializerProvider provider)
				throws IOException, JsonProcessingException {
			try {
				jsonGen.writeObject(getStringFromMatchType((List<IMatchType>) matchType));
				LOG.debug("Serializing custom type");
			} catch (ZinggClientException e) {
				throw new IOException(e);
			}
		}

		public static String getStringFromMatchType(List<IMatchType> matchType) throws ZinggClientException {
			return String.join(",", matchType.stream()
					.map(p -> p.getName())
					.collect(Collectors.toList()));
		}
	}

	public static class MatchTypeDeserializer extends StdDeserializer<List<IMatchType>> {
		private static final long serialVersionUID = 1L;
		
		public MatchTypeDeserializer() { 
		   this(null); 
		} 
		public MatchTypeDeserializer(Class<String> t) { 
		   super(t); 
		} 
		@Override 
		public List<IMatchType> deserialize(JsonParser parser, DeserializationContext context) 
		   throws IOException, JsonProcessingException { 
			ObjectMapper mapper = new ObjectMapper();
			try{
				mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
				LOG.debug("Deserializing custom type");
				return getMatchTypeFromString(mapper.readValue(parser, String.class)); 
			}
			catch(Exception | ZinggClientException e) {
				throw new IOException(e);
			}
		}   

		public static List<IMatchType> getMatchTypeFromString(String m) throws ZinggClientException, Exception{
			List<IMatchType> matchTypes = new ArrayList<IMatchType>();
		    String[] matchTypeFromConfig = m.split(","); 
			for (String s: matchTypeFromConfig) { 
				IMatchType mt = MatchTypes.getByName(s);
				matchTypes.add(mt);
			}     
		   return matchTypes; 
		}
	}
	
	

}
