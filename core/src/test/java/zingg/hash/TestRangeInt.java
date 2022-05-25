package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRangeInt {

	@Test
	public void testRangeForValueZero() {
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(1, value.call(0));
		value = new RangeBetween10And100Int();
		assertEquals(0, value.call(0));
		value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(0));
		value = new RangeBetween1000And10000Int();
		assertEquals(0, value.call(0));
	}

	@Test
	public void testRangeForNegativeValue() {
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(0, value.call(-100));
		value = new RangeBetween10And100Int();
		assertEquals(0, value.call(-100));
		value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(-100));
		value = new RangeBetween1000And10000Int();
		assertEquals(0, value.call(-100));
	}

	@Test
	public void testRangeForVeryHighValue() {
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(0, value.call(999999));
		value = new RangeBetween10And100Int();
		assertEquals(0, value.call(999999));
		value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(999999));
		value = new RangeBetween1000And10000Int();
		assertEquals(0, value.call(999999));
	}

	@Test
	public void testRangeForValue8() {
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(1, value.call(8));
		value = new RangeBetween10And100Int();
		assertEquals(0, value.call(8));
		value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(8));
		value = new RangeBetween1000And10000Int();
		assertEquals(0, value.call(8));
	}

	@Test
	public void testRangeForValue65() {
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(0, value.call(65));
		value = new RangeBetween10And100Int();
		assertEquals(1, value.call(65));
		value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(65));
		value = new RangeBetween1000And10000Int();
		assertEquals(0, value.call(65));
	}

	@Test
	public void testRangeForValue867() {
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(0, value.call(867));
		value = new RangeBetween10And100Int();
		assertEquals(0, value.call(867));
		value = new RangeBetween100And1000Int();
		assertEquals(1, value.call(867));
		value = new RangeBetween1000And10000Int();
		assertEquals(0, value.call(867));
	}
	@Test
	public void testRangeLessThan10000() {
		Integer input = 8637;
		RangeInt value = new RangeBetween0And10Int();
		assertEquals(0, value.call(input));
		value = new RangeBetween10And100Int();
		assertEquals(0, value.call(input));
		value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(input));
		value = new RangeBetween1000And10000Int();
		assertEquals(1, value.call(input));
	}

}
