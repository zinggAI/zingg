package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestLongSimilarityFunctionExact {
	
	
	@Test
	public void testFirstNull() {
		LongSimilarityFunctionExact exact = new LongSimilarityFunctionExact();
		assertEquals(1d, exact.call(null, 2l));
	}


	@Test
	public void testSecondNull() {
		LongSimilarityFunctionExact exact = new LongSimilarityFunctionExact();
		assertEquals(1d, exact.call(1l, null));
	}

	@Test
	public void testBothNull() {
		LongSimilarityFunctionExact exact = new LongSimilarityFunctionExact();
		assertEquals(1d, exact.call(null, null));
	}

	@Test
	public void testNotEqual() {
		LongSimilarityFunctionExact exact = new LongSimilarityFunctionExact();
		assertEquals(0d, exact.call(101l, 102l));
	}

	@Test
	public void testEqual() {
		LongSimilarityFunctionExact exact = new LongSimilarityFunctionExact();
		assertEquals(1d, exact.call(101l, 101l));
	}
	
}
