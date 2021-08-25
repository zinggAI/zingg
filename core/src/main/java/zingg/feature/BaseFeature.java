package zingg.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.similarity.function.BaseSimilarityFunction;

public abstract class BaseFeature<T> implements Feature<T> {

	public static final Log LOG = LogFactory.getLog(BaseFeature.class);

	List<BaseSimilarityFunction<T>> simFunctions;
	FieldDefinition fieldDefinition;
	
	public FieldDefinition getFieldDefinition() {
		return fieldDefinition;
	}

	public BaseFeature() {
		simFunctions = new ArrayList<BaseSimilarityFunction<T>>();
	}

	public BaseFeature(FieldDefinition fieldDefinition) {
		this();
		this.fieldDefinition = fieldDefinition;

	}

	/**
	 * @return the fieldType
	 */
	public MatchType getMatchType() {
		return fieldDefinition.getMatchType();
	}

	/**
	 * @param fieldType
	 *            the fieldType to set
	 */
	public void setFieldDefinition(FieldDefinition fieldType) {
		this.fieldDefinition = fieldType;
	}

	

	public BaseSimilarityFunction<T> getSimFunction(int i) {
		return simFunctions.get(i);
	}

	/**
	 * @return the simFunctions
	 */
	public List<BaseSimilarityFunction<T>> getSimFunctions() {
		return simFunctions;
	}

	/**
	 * @param simFunctions
	 *            the simFunctions to set
	 */
	public void setSimFunctions(List<BaseSimilarityFunction<T>> simFunctions) {
		this.simFunctions = simFunctions;
	}

	@Override
	public void addSimFunction(BaseSimilarityFunction<T> b) {
		//LOG.debug("Adding " + b + " with " + b.getNumFeatures() + " and "
		//		+ b.getNorm());
		this.simFunctions.add(b);
	}

	

	
}
