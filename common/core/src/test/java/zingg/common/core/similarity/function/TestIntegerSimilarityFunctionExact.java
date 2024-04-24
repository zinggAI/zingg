package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestIntegerSimilarityFunctionExact {
	
	
	@Test
	public void testFirstNull() {
		SimilarityFunctionExact<Integer> exact = new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact");
		assertEquals(1d, exact.call(null, 2));
	}


	@Test
	public void testSecondNull() {
		SimilarityFunctionExact<Integer> exact = new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact");
		assertEquals(1d, exact.call(1, null));
	}

	@Test
	public void testBothNull() {
		SimilarityFunctionExact<Integer> exact = new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact");
		assertEquals(1d, exact.call(null, null));
	}

	@Test
	public void testNotEqual() {
		SimilarityFunctionExact<Integer> exact = new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact");
		assertEquals(0d, exact.call(101, 102));
	}

	@Test
	public void testEqual() {
		SimilarityFunctionExact<Integer> exact = new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact");
		assertEquals(1d, exact.call(101, 101));
	}
	
}
