package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestLessThanZeroInt {

	@Test
	public void testLessThanZeroIntForValueZero() {
	    SparkLessThanZeroInt value = new SparkLessThanZeroInt();
		assertFalse(value.call(0));
	}

	@Test
	public void testLessThanZeroIntForValueNull() {
        SparkLessThanZeroInt value = new SparkLessThanZeroInt();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroIntNegativeInteger() {
	    SparkLessThanZeroInt value = new SparkLessThanZeroInt();
		assertTrue(value.call(-5435));
	}

	@Test
	public void testLessThanZeroIntPositiveInteger() {
	    SparkLessThanZeroInt value = new SparkLessThanZeroInt();
		assertFalse(value.call(5435));
	}

}
