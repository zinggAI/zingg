package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRangeDbl {

	@Test
	public void testRangeForValueZero() {
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(1, value.call((double) 0));
		value = new RangeBetween10And100Dbl();
		assertEquals(0, value.call((double) 0));
		value = new RangeBetween100And1000Dbl();
		assertEquals(0, value.call((double) 0));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call((double) 0));
	}

	@Test
	public void testRangeForNegativeValue() {
		Double input = -100d;
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween10And100Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween100And1000Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForVeryHighValue() {
		Double input = 999999d;
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween10And100Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween100And1000Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(1, value.call((double) 8));
		value = new RangeBetween10And100Dbl();
		assertEquals(0, value.call((double) 8));
		value = new RangeBetween100And1000Dbl();
		assertEquals(0, value.call((double) 8));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call((double) 8));
	}

	@Test
	public void testRangeForValue65() {
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(0, value.call((double) 65));
		value = new RangeBetween10And100Dbl();
		assertEquals(1, value.call((double) 65));
		value = new RangeBetween100And1000Dbl();
		assertEquals(0, value.call((double) 65));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call((double) 65));
	}

	@Test
	public void testRangeForValue867() {
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(0, value.call((double) 867));
		value = new RangeBetween10And100Dbl();
		assertEquals(0, value.call((double) 867));
		value = new RangeBetween100And1000Dbl();
		assertEquals(1, value.call((double) 867));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(0, value.call((double) 867));
	}
	@Test
	public void testRangeLessThan10000() {
		Double input = 8637d;
		RangeDbl value = new RangeBetween0And10Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween10And100Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween100And1000Dbl();
		assertEquals(0, value.call(input));
		value = new RangeBetween1000And10000Dbl();
		assertEquals(1, value.call(input));
	}

}
