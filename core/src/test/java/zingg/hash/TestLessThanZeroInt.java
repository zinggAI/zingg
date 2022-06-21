package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLessThanZeroInt {

	@Test
	public void testLessThanZeroIntForValueZero() {
		LessThanZeroInt value = new LessThanZeroInt();
		assertFalse(value.call(0));
	}

	@Test
	public void testLessThanZeroIntForValueNull() {
		LessThanZeroInt value = new LessThanZeroInt();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroIntNegativeInteger() {
		LessThanZeroInt value = new LessThanZeroInt();
		assertTrue(value.call(-5435));
	}

	@Test
	public void testLessThanZeroIntPositiveInteger() {
		LessThanZeroInt value = new LessThanZeroInt();
		assertFalse(value.call(5435));
	}

}
