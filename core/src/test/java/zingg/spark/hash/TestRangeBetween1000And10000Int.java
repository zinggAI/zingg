package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRangeBetween1000And10000Int {

	@Test
	public void testRangeForValueZero() {
		SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(0));
	}

	@Test
	public void testRangeForNegativeValue() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(-100));
	}

	@Test
	public void testRangeForVeryHighValue() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(999999));
	}

	@Test
	public void testRangeForValue8() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(8));
	}

	@Test
	public void testRangeForValue65() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(65));
	}

	@Test
	public void testRangeForValue867() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(867));
	}

	@Test
	public void testRangeForValue8637() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(1, value.call(8637));
	}
	@Test
	public void testRangeForNull() {
        SparkRangeInt value = new SparkRangeBetween1000And10000Int();
		assertEquals(0, value.call(null));
	}

}
