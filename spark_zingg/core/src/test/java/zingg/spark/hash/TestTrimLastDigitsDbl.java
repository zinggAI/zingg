package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTrimLastDigitsDbl {
	
	@Test
	public void testTrimLast1DigitDbl() {
	    SparkTrimLastDigitsDbl value = new SparkTrimLastDigitsDbl(1);
		assertEquals(54353d, value.call(543534.677));
	}

	@Test
	public void testTrimLast2DigitsDbl() {
	    SparkTrimLastDigitsDbl value = new SparkTrimLastDigitsDbl(2);
		assertEquals(5435d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDbl() {
	    SparkTrimLastDigitsDbl value = new SparkTrimLastDigitsDbl(3);
		assertEquals(543d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDblNaNValue() {
	    SparkTrimLastDigitsDbl value = new SparkTrimLastDigitsDbl(3);
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTrimLast3DigitsDblNullValue() {
	    SparkTrimLastDigitsDbl value = new SparkTrimLastDigitsDbl(3);
		assertEquals(null, value.call(null));
	}

}
