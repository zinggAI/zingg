package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRangeBetween10And100Dbl {

    private SparkRangeDbl getInstance() {
        return new SparkRangeDbl(10,100);
    }

	@Test
	public void testRangeForValueZero() {
		SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(0d));
	}

	@Test
	public void testRangeForNegativeValue() {
		Double input = -100d;
		SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(input));

	}

	@Test
	public void testRangeForVeryHighValue() {
		Double input = 999999d;
		SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
	    SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(8d));
	}

	@Test
	public void testRangeForValue65() {
	    SparkRangeDbl value = getInstance();
		assertEquals(1, value.call(65d));
	}

	@Test
	public void testRangeForValue867() {
	    SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(867d));
	}
	@Test
	public void testRangeForValue8637() {
	    SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(8637d));
	}
	@Test
	public void testRangeForNull() {
	    SparkRangeDbl value = getInstance();
		assertEquals(0, value.call(null));
	}

}
