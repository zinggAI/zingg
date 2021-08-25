/**
 * Copyright Nube Technologies
 * All rights reserved.
 */

package zingg.client;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
	public MatchType matchType;
	@JsonSerialize(using = DataTypeSerializer.class)
	public DataType dataType;
	public String fieldName;
	public String fields;

	public FieldDefinition() {
	}

	public String getFields() { return fields; }

	public void setFields(String fields) { this.fields = fields;}	
	
	/**
	 * Get the field type of the class
	 * 
	 * @return the type
	 */
	public MatchType getMatchType() {
		return matchType;
	}

	/**
	 * Set the field type which defines the kind of matching we want to do
	 * 
	 * @see MatchType
	 * @param type
	 *            the type to set
	 */
	public void setMatchType(MatchType type) {
		this.matchType = type;
	}

	
	public DataType getDataType() {
		return dataType;
	}

	@JsonProperty("dataType")
	public void setDataType(String d) {
		if (d!= null) this.dataType =  DataType.fromJson(d);
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
	
	

}
