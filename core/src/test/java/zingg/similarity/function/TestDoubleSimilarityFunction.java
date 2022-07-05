package zingg.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestDoubleSimilarityFunction {
	
	
	@Test
	public void testFirstNumsimFn() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(null, 1d));
	}

	@Test
	public void testFirstNumIsNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(Double.NaN, 1d));
	}

	@Test
	public void testSecondNumsimFn() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(1d, null));
	}

	@Test
	public void testSecondNumIsNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(1d, Double.NaN));
	}
	@Test
	public void testBothNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(Double.NaN, Double.NaN));
	}

	@Test
	public void testBothNull() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(null, null));
	}

	@Test
	public void testBothNotNullNorNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(0d, simFn.call(1d, 1d));
	}

	@Test
	public void testValues0And0() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(0d, simFn.call(0d, 0d));
	}

	@Test
	public void testValues100And1() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(0.9705882352941176d, simFn.call(100d, 1d));
	}

	@Test
	public void testValues10And1() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(0d, simFn.call(10.0, 10d));
	}

	@Test
	public void testValues34And54() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		Double score = simFn.call(34d, 54.0);
		assertEquals(0.2247191011235955d, score);
	}
}
