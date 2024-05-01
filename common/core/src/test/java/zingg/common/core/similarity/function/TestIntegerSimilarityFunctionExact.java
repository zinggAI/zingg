package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestIntegerSimilarityFunctionExact {
	
	@Test
	public void testFirstNull() {
		assertEquals(1d, simFunc().call(null, 2));
	}

	@Test
	public void testSecondNull() {
		assertEquals(1d, simFunc().call(1, null));
	}

	@Test
	public void testBothNull() {
		assertEquals(1d, simFunc().call(null, null));
	}

	@Test
	public void testNotEqual() {
		assertEquals(0d, simFunc().call(101, 102));
	}

	@Test
	public void testEqual() {
		assertEquals(1d, simFunc().call(101, 101));
	}

	protected SimilarityFunctionExact<Integer> simFunc() {
		return new SimilarityFunctionExact<Integer>("IntegerSimilarityFunctionExact");
	}
	
}
