package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTrimLastDigitsDbl {
	
	@Test
	public void testTrimLast1DigitDbl() {
		SparkTrimLast1DigitDbl value = new SparkTrimLast1DigitDbl();
		assertEquals(54353d, value.call(543534.677));
	}

	@Test
	public void testTrimLast2DigitsDbl() {
	    SparkTrimLast2DigitsDbl value = new SparkTrimLast2DigitsDbl();
		assertEquals(5435d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDbl() {
	    SparkTrimLast3DigitsDbl value = new SparkTrimLast3DigitsDbl();
		assertEquals(543d, value.call(543534.677));
	}

	@Test
	public void testTrimLast3DigitsDblNaNValue() {
	    SparkTrimLast3DigitsDbl value = new SparkTrimLast3DigitsDbl();
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTrimLast3DigitsDblNullValue() {
	    SparkTrimLast3DigitsDbl value = new SparkTrimLast3DigitsDbl();
		assertEquals(null, value.call(null));
	}

}
