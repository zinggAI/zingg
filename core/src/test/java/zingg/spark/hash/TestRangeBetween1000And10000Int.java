package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRangeBetween1000And10000Int {

    private SparkRangeInt getInstance() {
        return new SparkRangeInt(1000,10000);
    }

	@Test
	public void testRangeForValueZero() {
		SparkRangeInt value = getInstance();
		assertEquals(0, value.call(0));
	}

	@Test
	public void testRangeForNegativeValue() {
        SparkRangeInt value = getInstance();
		assertEquals(0, value.call(-100));
	}

	@Test
	public void testRangeForVeryHighValue() {
        SparkRangeInt value = getInstance();
		assertEquals(0, value.call(999999));
	}

	@Test
	public void testRangeForValue8() {
        SparkRangeInt value = getInstance();
		assertEquals(0, value.call(8));
	}

	@Test
	public void testRangeForValue65() {
        SparkRangeInt value = getInstance();
		assertEquals(0, value.call(65));
	}

	@Test
	public void testRangeForValue867() {
        SparkRangeInt value = getInstance();
		assertEquals(0, value.call(867));
	}

	@Test
	public void testRangeForValue8637() {
        SparkRangeInt value = getInstance();
		assertEquals(1, value.call(8637));
	}
	@Test
	public void testRangeForNull() {
        SparkRangeInt value = getInstance();
		assertEquals(0, value.call(null));
	}

}
