package zingg;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.junit.jupiter.api.Test;

import zingg.common.core.similarity.function.ArrayDoubleSimilarityFunction;

public class TestImageType {
	
	
	@Test
	public void testImageType() {
		
		Double[] d1 = {0.0,2.0};
		Double[] d2 = {0.0,1.0};
		Double[] d3 = {1.0,0.0};
		Double[] d4 = {-1.0,-1.0};
		Double[] d5 = {0.0,1.0,0.0};
		Double[] d6 = {1.0,0.0,0.0};
		Double[] d7 = {0.0,0.0,0.0};
		Double[] d8 = {1.0,0.0,0.0};
		
		
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d1, d2));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d2, d3));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d4, d3));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d5, d6));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d7, d8));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d8, d7));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d7, d7));
		System.out.println(ArrayDoubleSimilarityFunction.cosineSimilarity(d8, d8));
		
		System.out.print(DataTypes.createArrayType(DataTypes.DoubleType));
		
	}

	
}
