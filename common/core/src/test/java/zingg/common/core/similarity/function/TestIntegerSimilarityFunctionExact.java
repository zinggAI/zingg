package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestIntegerSimilarityFunctionExact {
	
	
	@Test
	public void testFirstNull() {
		IntegerSimilarityFunctionExact exact = new IntegerSimilarityFunctionExact();
		assertEquals(1d, exact.call(null, 2));
	}


	@Test
	public void testSecondNull() {
		IntegerSimilarityFunctionExact exact = new IntegerSimilarityFunctionExact();
		assertEquals(1d, exact.call(1, null));
	}

	@Test
	public void testBothNull() {
		IntegerSimilarityFunctionExact exact = new IntegerSimilarityFunctionExact();
		assertEquals(1d, exact.call(null, null));
	}

	@Test
	public void testNotEqual() {
		IntegerSimilarityFunctionExact exact = new IntegerSimilarityFunctionExact();
		assertEquals(0d, exact.call(101, 102));
	}

	@Test
	public void testEqual() {
		IntegerSimilarityFunctionExact exact = new IntegerSimilarityFunctionExact();
		assertEquals(1d, exact.call(101, 101));
	}
	
}
