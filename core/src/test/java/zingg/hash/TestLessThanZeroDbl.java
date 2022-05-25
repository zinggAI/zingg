package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLessThanZeroDbl {

	@Test
	public void testLessThanZeroDblForValueZero() {
		LessThanZeroDbl value = new LessThanZeroDbl();
		assertFalse(value.call(0d));
	}

	@Test
	public void testLessThanZeroDblForValueNaN() {
		LessThanZeroDbl value = new LessThanZeroDbl();
		assertFalse(value.call(Double.NaN));
	}

	@Test
	public void testLessThanZeroDblForValueNull() {
		LessThanZeroDbl value = new LessThanZeroDbl();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroDblForPositiveDoubleValue() {
		LessThanZeroDbl value = new LessThanZeroDbl();
		assertFalse(value.call(543534.67734));
	}

	@Test
	public void testLessThanZeroDblNegativeDoubleValue() {
		LessThanZeroDbl value = new LessThanZeroDbl();
		assertTrue(value.call(-543534.67734));
	}

}
