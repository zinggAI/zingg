package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRangeBetween100And1000Int {

	@Test
	public void testRangeForValueZero() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(0));
	}

	@Test
	public void testRangeForNegativeValue() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(-100));
	}

	@Test
	public void testRangeForVeryHighValue() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(999999));
	}

	@Test
	public void testRangeForValue8() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(8));
	}

	@Test
	public void testRangeForValue65() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(65));
	}

	@Test
	public void testRangeForValue867() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(1, value.call(867));
	}
	@Test
	public void testRangeForValue8637() {
		RangeInt value = new RangeBetween100And1000Int();
		assertEquals(0, value.call(8637));
	}

}
