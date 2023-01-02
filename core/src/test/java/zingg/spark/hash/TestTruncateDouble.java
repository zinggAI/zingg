package zingg.spark.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTruncateDouble {
	
	@Test
	public void testTruncateDoubleTo1Place() {
	    SparkTruncateDoubleTo1Place value = new SparkTruncateDoubleTo1Place();
		assertEquals(543534.6d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo2Place() {
	    SparkTruncateDoubleTo2Places value = new SparkTruncateDoubleTo2Places();
		assertEquals(543534.67d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3Place() {
	    SparkTruncateDoubleTo3Places value = new SparkTruncateDoubleTo3Places();
		assertEquals(543534.677d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3PlaceWhenNumberhas2DecimalPlaces() {
	    SparkTruncateDoubleTo3Places value = new SparkTruncateDoubleTo3Places();
		assertEquals(543534.670d, value.call(543534.67));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberWithNoDecimalPlaces() {
	    SparkTruncateDoubleTo3Places value = new SparkTruncateDoubleTo3Places();
		assertEquals(543534.000d, value.call(543534d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberNAN() {
	    SparkTruncateDoubleTo3Places value = new SparkTruncateDoubleTo3Places();
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumber0() {
	    SparkTruncateDoubleTo3Places value = new SparkTruncateDoubleTo3Places();
		assertEquals(0d, value.call(0d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNull() {
	    SparkTruncateDoubleTo3Places value = new SparkTruncateDoubleTo3Places();
		assertEquals(null, value.call(null));
	}

}
