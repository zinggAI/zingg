package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestLongSimilarityFunctionExact {
	
	
	@Test
	public void testFirstNull() {
		assertEquals(1d, simFunc().call(null, 2l));
	}

	@Test
	public void testSecondNull() {
		assertEquals(1d, simFunc().call(1l, null));
	}

	@Test
	public void testBothNull() {
		assertEquals(1d, simFunc().call(null, null));
	}

	@Test
	public void testNotEqual() {
		assertEquals(0d, simFunc().call(101l, 102l));
	}

	@Test
	public void testEqual() {
		assertEquals(1d, simFunc().call(101l, 101l));
	}
	
	protected SimilarityFunctionExact<Long> simFunc() {
		return new SimilarityFunctionExact<Long>("LongSimilarityFunctionExact");
	}	
	
}
