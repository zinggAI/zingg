package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.TrimLastDigitsFloat;

public class TestTrimLastDigitsFloat {
    
	@Test
	public void testTrimLast1DigitFloat() {
	    TrimLastDigitsFloat value = getInstance(1);
		assertEquals(54353f, value.call(543534.677f));
	}

	@Test
	public void testTrimLast2DigitsFloat() {
	    TrimLastDigitsFloat value = getInstance(2);
		assertEquals(5435f, value.call(543534.677f));
	}

	@Test
	public void testTrimLast3DigitsFloat() {
	    TrimLastDigitsFloat value = getInstance(3);
		assertEquals(543f, value.call(543534.677f));
	}

	@Test
	public void testTrimLast1DigitNegativeFloat() {
		TrimLastDigitsFloat value = getInstance(1);
		assertEquals(-54354f, value.call(-543534.677f));
	}

	@Test
	public void testTrimLast2DigitsNegativeFloat() {
		TrimLastDigitsFloat value = getInstance(2);
		assertEquals(-5436f, value.call(-543534.677f));
	}

	@Test
	public void testTrimLast3DigitsNegativeFloat() {
		TrimLastDigitsFloat value = getInstance(3);
		assertEquals(-544f, value.call(-543534.677f));
	}

	@Test
	public void testTrimLast3DigitsFloatNaNValue() {
	    TrimLastDigitsFloat value = getInstance(3);
		assertEquals(Float.NaN, value.call(Float.NaN));
	}

	@Test
	public void testTrimLast3DigitsFloatNullValue() {
	    TrimLastDigitsFloat value = getInstance(3);
		assertEquals(null, value.call(null));
	}
	
	@Test
	public void testTrimLast3DigitsNegativeFloatNaNValue() {
		TrimLastDigitsFloat value = getInstance(3);
		assertEquals(Float.NaN, value.call(Float.NaN));
	}

	@Test
	public void testTrimLast3DigitsNegativeFloatNullValue() {
		TrimLastDigitsFloat value = getInstance(3);
		assertEquals(null, value.call(null));
	}
	
    private TrimLastDigitsFloat getInstance(int num) {
        return new TrimLastDigitsFloat(num);
    }

}
