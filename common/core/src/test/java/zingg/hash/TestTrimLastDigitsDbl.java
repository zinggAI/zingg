package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTrimLastDigitsDbl {
	
	@Test
	public void testTrimLast1DigitDbl() {
	    TrimLastDigitsDbl value = getInstance(1);
		assertEquals(54353d, value.call(543534.677));
	}

	@Test
	public void testTrimLast2DigitsDbl() {
	    TrimLastDigitsDbl value = getInstance(2);
		assertEquals(5435d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDbl() {
	    TrimLastDigitsDbl value = getInstance(3);
		assertEquals(543d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDblNaNValue() {
	    TrimLastDigitsDbl value = getInstance(3);
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTrimLast3DigitsDblNullValue() {
	    TrimLastDigitsDbl value = getInstance(3);
		assertEquals(null, value.call(null));
	}
	
    private TrimLastDigitsDbl getInstance(int num) {
        return new TrimLastDigitsDbl(num);
    }


}
