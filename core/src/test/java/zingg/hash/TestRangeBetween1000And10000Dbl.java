package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRangeBetween1000And10000Dbl {

	@Test
	public void testRangeForValueZero() {
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(0d));
	}

	@Test
	public void testRangeForNegativeValue() {
		Double input = -100d;
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForVeryHighValue() {
		Double input = 999999d;
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(8d));
	}

	@Test
	public void testRangeForValue65() {
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(65d));
	}

	@Test
	public void testRangeForValue867() {
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(867d));
	}
	@Test
	public void testRangeLessThan10000() {
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(1, value.call(8637d));
	}
	@Test
	public void testRangeForNull() {
		RangeDbl value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(null));
	}

}
