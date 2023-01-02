package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestSparkLessThanZeroDbl {

	@Test
	public void testLessThanZeroDblForValueZero() {
	    SparkLessThanZeroDbl value = new SparkLessThanZeroDbl();
		assertFalse(value.call(0d));
	}

	@Test
	public void testLessThanZeroDblForValueNaN() {
	    SparkLessThanZeroDbl value = new SparkLessThanZeroDbl();
		assertFalse(value.call(Double.NaN));
	}

	@Test
	public void testLessThanZeroDblForValueNull() {
	    SparkLessThanZeroDbl value = new SparkLessThanZeroDbl();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroDblForPositiveDoubleValue() {
	    SparkLessThanZeroDbl value = new SparkLessThanZeroDbl();
		assertFalse(value.call(543534.67734));
	}

	@Test
	public void testLessThanZeroDblNegativeDoubleValue() {
	    SparkLessThanZeroDbl value = new SparkLessThanZeroDbl();
		assertTrue(value.call(-543534.67734));
	}

}
