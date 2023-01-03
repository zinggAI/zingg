package zingg.feature;

import java.io.Serializable;
import java.util.List;

import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.similarity.function.SimFunction;

public interface Feature<T> extends Serializable {

	void setFieldDefinition(FieldDefinition f);
	
	FieldDefinition getFieldDefinition();

	List<MatchType> getMatchType();

	SimFunction<T> getSimFunction(int i);
	
	List<SimFunction<T>> getSimFunctions();

	void init(FieldDefinition newParam);
	
	//String[] getCols();

	void addSimFunction(SimFunction<T> b);
}
