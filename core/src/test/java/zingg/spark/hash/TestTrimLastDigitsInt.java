package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTrimLastDigitsInt {
	
	@Test
	public void testTrimLast1Digit() {
	    SparkTrimLast1DigitInt value = new SparkTrimLast1DigitInt();
		assertEquals(54353, value.call(543534));
	}

	@Test
	public void testTrimLast2DigitsInt() {
	    SparkTrimLast2DigitsInt value = new SparkTrimLast2DigitsInt();
		assertEquals(5435, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsInt() {
	    SparkTrimLast3DigitsInt value = new SparkTrimLast3DigitsInt();
		assertEquals(543, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsIntNullValue() {
	    SparkTrimLast3DigitsInt value = new SparkTrimLast3DigitsInt();
		assertEquals(null, value.call(null));
	}

}
