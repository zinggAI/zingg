package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestLessThanZeroInt {

	@Test
	public void testLessThanZeroIntForValueZero() {
	    LessThanZeroInt value = getInstance();
		assertFalse(value.call(0));
	}

	@Test
	public void testLessThanZeroIntForValueNull() {
	    LessThanZeroInt value = getInstance();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroIntNegativeInteger() {
	    LessThanZeroInt value = getInstance();
		assertTrue(value.call(-5435));
	}

	@Test
	public void testLessThanZeroIntPositiveInteger() {
	    LessThanZeroInt value = getInstance();
		assertFalse(value.call(5435));
	}
	
    private LessThanZeroInt getInstance() {
        LessThanZeroInt value = new LessThanZeroInt();
        return value;
    }

}
