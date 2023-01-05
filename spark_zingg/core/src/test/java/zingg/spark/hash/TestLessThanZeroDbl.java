package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestLessThanZeroDbl {

	@Test
	public void testLessThanZeroDblForValueZero() {
	    SparkLessThanZeroDbl value = getInstance();
		assertFalse(value.call(0.0));
	}

	@Test
	public void testLessThanZeroDblForValueNull() {
	    SparkLessThanZeroDbl value = getInstance();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroDblNegativeInteger() {
	    SparkLessThanZeroDbl value = getInstance();
		assertTrue(value.call(-5435.01));
	}

	@Test
	public void testLessThanZeroDblPositiveInteger() {
	    SparkLessThanZeroDbl value = getInstance();
		assertFalse(value.call(5435.01));
	}

    private SparkLessThanZeroDbl getInstance() {
        return new SparkLessThanZeroDbl();
    }

}
