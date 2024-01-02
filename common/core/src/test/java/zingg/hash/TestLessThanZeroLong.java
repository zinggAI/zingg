package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.LessThanZeroLong;

public class TestLessThanZeroLong {
    
	@Test
	public void testLessThanZeroLongForValueZero() {
	    LessThanZeroLong value = getInstance();
		assertFalse(value.call(0L));
	}

	@Test
	public void testLessThanZeroLongForValueNull() {
	    LessThanZeroLong value = getInstance();
		assertFalse(value.call(null));
	}

	@Test
	public void testLessThanZeroLongNegativeValue() {
	    LessThanZeroLong value = getInstance();
		assertTrue(value.call(-543545L));
	}

	@Test
	public void testLessThanZeroLongPositiveValue() {
	    LessThanZeroLong value = getInstance();
		assertFalse(value.call(876457L));
	}
	
    private LessThanZeroLong getInstance() {
        LessThanZeroLong value = new LessThanZeroLong();
        return value;
    }
}
