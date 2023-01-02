package zingg.hash;

import org.junit.jupiter.api.Test;

import zingg.hash.unused.TrimLast1DigitDbl;
import zingg.hash.unused.TrimLast2DigitsDbl;
import zingg.hash.unused.TrimLast3DigitsDbl;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTrimLastDigitsDbl {
	
	@Test
	public void testTrimLast1DigitDbl() {
		TrimLast1DigitDbl value = new TrimLast1DigitDbl();
		assertEquals(54353d, value.call(543534.677));
	}

	@Test
	public void testTrimLast2DigitsDbl() {
		TrimLast2DigitsDbl value = new TrimLast2DigitsDbl();
		assertEquals(5435d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDbl() {
		TrimLast3DigitsDbl value = new TrimLast3DigitsDbl();
		assertEquals(543d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDblNaNValue() {
		TrimLast3DigitsDbl value = new TrimLast3DigitsDbl();
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTrimLast3DigitsDblNullValue() {
		TrimLast3DigitsDbl value = new TrimLast3DigitsDbl();
		assertEquals(null, value.call(null));
	}

}
