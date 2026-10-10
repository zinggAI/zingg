package zingg.common.core.feature;

import scala.collection.Seq;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;
public class ArrayDoubleFeature extends BaseFeature<Seq<Double>> {

	private static final long serialVersionUID = 1L;

	public ArrayDoubleFeature() {

	}

	public void init(FieldDefinition newParam) {
		setFieldDefinition(newParam);
		if (newParam.getMatchType().contains(MatchTypes.FUZZY)) {
			addSimFunction(new ArrayDoubleSimilarityFunction());
		} 
	}

}
