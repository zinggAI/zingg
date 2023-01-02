package zingg.hash;

import org.junit.jupiter.api.Test;

import zingg.hash.unused.TrimLast1DigitInt;
import zingg.hash.unused.TrimLast2DigitsInt;
import zingg.hash.unused.TrimLast3DigitsInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTrimLastDigitsInt {
	
	@Test
	public void testTrimLast1Digit() {
		TrimLast1DigitInt value = new TrimLast1DigitInt();
		assertEquals(54353, value.call(543534));
	}

	@Test
	public void testTrimLast2DigitsInt() {
		TrimLast2DigitsInt value = new TrimLast2DigitsInt();
		assertEquals(5435, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsInt() {
		TrimLast3DigitsInt value = new TrimLast3DigitsInt();
		assertEquals(543, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsIntNullValue() {
		TrimLast3DigitsInt value = new TrimLast3DigitsInt();
		assertEquals(null, value.call(null));
	}

}
