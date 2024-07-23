



package zingg.common.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a container class which holds a list of samples from the test data
 * and the field definitions as suggested by Zingg FieldDefinitions suggested
 * by Zingg are an approximation, clients are free to define their own
 * definitions
 * 
 * @author sgoyal
 *
 */
public class Samples implements Serializable {
	
	private List<String> originalLines;

	private List<ArrayList<String>> samples;
	private List<FieldDefinition> fields;

	public List<String> getOriginalLines() {
		return originalLines;
	}

	public void setOriginalLines(List<String> originalLines) {
		this.originalLines = originalLines;
	}
	
	/**
	 * List of sample records
	 * 
	 * @return sample records from the match data
	 */
	public List<ArrayList<String>> getSamples() {
		return samples;
	}

	/**
	 * Set the samples. Not to be used by client app
	 * 
	 * @param samples
	 *            sample records set by Zingg
	 */
	public void setSamples(List<ArrayList<String>> samples) {
		this.samples = samples;
	}

	/**
	 * Get the field definitions approximated by Zingg
	 * 
	 * @return field definitions of each column field
	 */
	public List<FieldDefinition> getFields() {
		return fields;
	}

	/**
	 * Set the field definitions
	 * 
	 * @param fieldDef
	 *            field definitions denoting field type and class
	 */
	public void setFields(List<FieldDefinition> fieldDef) {
		this.fields = fieldDef;
	}

}
