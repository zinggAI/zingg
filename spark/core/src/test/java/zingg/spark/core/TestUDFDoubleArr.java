package zingg.spark.core;

import org.apache.spark.sql.api.java.UDF2;

import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestUDFDoubleArr implements UDF2<Double[],Double[], Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(Double[] t1, Double[] t2) throws Exception {
		return ArrayDoubleSimilarityFunction.cosineSimilarity(t1, t2);
	}
	
}
