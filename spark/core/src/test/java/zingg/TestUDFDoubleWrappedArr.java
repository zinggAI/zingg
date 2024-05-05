package zingg;

import org.apache.spark.sql.api.java.UDF2;

import scala.collection.mutable.ArraySeq;
import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestUDFDoubleWrappedArr implements UDF2<ArraySeq<Double>,ArraySeq<Double>, Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(ArraySeq<Double> t1, ArraySeq<Double> t2) throws Exception {
		System.out.println("TestUDFDoubleWrappedArr class" +t1.getClass());
		
		Double[] t1Arr = new Double[t1.length()];
		if (t1!=null) {
			t1.copyToArray(t1Arr);
		}
		Double[] t2Arr = new Double[t2.length()];
		if (t2!=null) {
			t2.copyToArray(t2Arr);
		}
		return ArrayDoubleSimilarityFunction.cosineSimilarity(t1Arr, t2Arr);
	}
	
}
