package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTrimLastDigitsInt {
	
	@Test
	public void testTrimLast1Digit() {
	    SparkTrimLastDigitsInt value = new SparkTrimLastDigitsInt(1);
		assertEquals(54353, value.call(543534));
	}

	@Test
	public void testTrimLast2DigitsInt() {
	    SparkTrimLastDigitsInt value = new SparkTrimLastDigitsInt(2);
		assertEquals(5435, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsInt() {
	    SparkTrimLastDigitsInt value = new SparkTrimLastDigitsInt(3);
		assertEquals(543, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsIntNullValue() {
	    SparkTrimLastDigitsInt value = new SparkTrimLastDigitsInt(3);
		assertEquals(null, value.call(null));
	}

}
