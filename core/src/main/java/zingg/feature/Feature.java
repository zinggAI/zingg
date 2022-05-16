package zingg.feature;

import java.io.Serializable;
import java.util.List;

import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.similarity.function.BaseSimilarityFunction;

public interface Feature<T> extends Serializable {

	void setFieldDefinition(FieldDefinition f);
	
	FieldDefinition getFieldDefinition();

	List<MatchType> getMatchType();

	BaseSimilarityFunction<T> getSimFunction(int i);
	
	List<BaseSimilarityFunction<T>> getSimFunctions();

	void init(FieldDefinition newParam);
	
	//String[] getCols();

	void addSimFunction(BaseSimilarityFunction<T> b);
}
