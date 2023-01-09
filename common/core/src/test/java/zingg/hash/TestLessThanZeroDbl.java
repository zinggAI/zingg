package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestLessThanZeroDbl {

	@Test
	public void testLessThanZeroDblForValueZero() {
	    LessThanZeroDbl value = getInstance();
		assertFalse(value.call(0.0));
	}

	@Test
	public void testLessThanZeroDblForValueNull() {
	    LessThanZeroDbl value = getInstance();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroDblNegativeInteger() {
	    LessThanZeroDbl value = getInstance();
		assertTrue(value.call(-5435.01));
	}

	@Test
	public void testLessThanZeroDblPositiveInteger() {
	    LessThanZeroDbl value = getInstance();
		assertFalse(value.call(5435.01));
	}

    private LessThanZeroDbl getInstance() {
        return new LessThanZeroDbl();
    }

}
