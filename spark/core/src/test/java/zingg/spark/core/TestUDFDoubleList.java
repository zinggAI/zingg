package zingg.spark.core;

import java.util.List;

import org.apache.spark.sql.api.java.UDF2;

import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestUDFDoubleList implements UDF2<List<Double>,List<Double>, Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(List<Double> t1, List<Double> t2) throws Exception {
		return ArrayDoubleSimilarityFunction.cosineSimilarity(t1.toArray(new Double[] {}), t2.toArray(new Double[] {}));
	}
	
}
