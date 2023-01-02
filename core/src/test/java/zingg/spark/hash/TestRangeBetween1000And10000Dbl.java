package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRangeBetween1000And10000Dbl {

	@Test
	public void testRangeForValueZero() {
		SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(0d));
	}

	@Test
	public void testRangeForNegativeValue() {
		Double input = -100d;
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForVeryHighValue() {
		Double input = 999999d;
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(8d));
	}

	@Test
	public void testRangeForValue65() {
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(65d));
	}

	@Test
	public void testRangeForValue867() {
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(867d));
	}
	@Test
	public void testRangeLessThan10000() {
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(1, value.call(8637d));
	}
	@Test
	public void testRangeForNull() {
        SparkRangeDbl value = new SparkRangeBetween1000And10000Dbl();
		assertEquals(0, value.call(null));
	}

}
