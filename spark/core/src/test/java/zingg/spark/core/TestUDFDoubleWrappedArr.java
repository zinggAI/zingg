package zingg.spark.core;

import org.apache.spark.sql.api.java.UDF2;

import scala.collection.Seq;
import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestUDFDoubleWrappedArr implements UDF2<Seq<Double>,Seq<Double>, Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(Seq<Double> t1, Seq<Double> t2) throws Exception {
		System.out.println("TestUDFDoubleWrappedArr class" +t1.getClass());
		
		Double[] t1Arr = new Double[t1.length()];
		for (int i = 0; i < t1.length(); i++) t1Arr[i] = t1.apply(i);
		Double[] t2Arr = new Double[t2.length()];
		for (int i = 0; i < t2.length(); i++) t2Arr[i] = t2.apply(i);
		return ArrayDoubleSimilarityFunction.cosineSimilarity(t1Arr, t2Arr);
	}
	
}
