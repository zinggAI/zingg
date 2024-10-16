package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.LessThanZeroFloat;

public class TestLessThanZeroFloat {
    
	@Test
	public void testLessThanZeroFloatForValueZero() {
	    LessThanZeroFloat value = getInstance();
		assertFalse(value.call(0.0f));
	}

	@Test
	public void testLessThanZeroFloatForValueNull() {
	    LessThanZeroFloat value = getInstance();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroFloatNegativeValue() {
	    LessThanZeroFloat value = getInstance();
		assertTrue(value.call(-5435.45f));
	}

	@Test
	public void testLessThanZeroFloatPositiveValue() {
	    LessThanZeroFloat value = getInstance();
		assertFalse(value.call(876.457f));
	}
	
    private LessThanZeroFloat getInstance() {
        LessThanZeroFloat value = new LessThanZeroFloat();
        return value;
    }
}
