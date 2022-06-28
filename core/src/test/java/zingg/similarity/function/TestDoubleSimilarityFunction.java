package zingg.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestDoubleSimilarityFunction {
	
	
	@Test
	public void testFirstNumIsNull() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(1d, isNull.call(null, 1d));
	}

	@Test
	public void testFirstNumIsNAN() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(1d, isNull.call(Double.NaN, 1d));
	}

	@Test
	public void testSecondNumIsNull() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(1d, isNull.call(1d, null));
	}

	@Test
	public void testSecondNumIsNAN() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(1d, isNull.call(1d, Double.NaN));
	}
	@Test
	public void testBothNAN() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(1d, isNull.call(Double.NaN, Double.NaN));
	}

	@Test
	public void testBothNull() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(1d, isNull.call(null, null));
	}

	@Test
	public void testBothNotNullNorNAN() {
		DoubleSimilarityFunction isNull = new DoubleSimilarityFunction();
		assertEquals(0d, isNull.call(1d, 1d));
	}

}
