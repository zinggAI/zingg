package zingg.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTruncateDouble {
	
	@Test
	public void testTruncateDoubleTo1Place() {
		TruncateDoubleTo1Place value = new TruncateDoubleTo1Place();
		assertEquals(543534.6d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo2Place() {
		TruncateDoubleTo2Places value = new TruncateDoubleTo2Places();
		assertEquals(543534.67d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3Place() {
		TruncateDoubleTo3Places value = new TruncateDoubleTo3Places();
		assertEquals(543534.677d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3PlaceWhenNumberhas2DecimalPlaces() {
		TruncateDoubleTo3Places value = new TruncateDoubleTo3Places();
		assertEquals(543534.670d, value.call(543534.67));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberWithNoDecimalPlaces() {
		TruncateDoubleTo3Places value = new TruncateDoubleTo3Places();
		assertEquals(543534.000d, value.call(543534d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberNAN() {
		TruncateDoubleTo3Places value = new TruncateDoubleTo3Places();
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumber0() {
		TruncateDoubleTo3Places value = new TruncateDoubleTo3Places();
		assertEquals(0d, value.call(0d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNull() {
		TruncateDoubleTo3Places value = new TruncateDoubleTo3Places();
		assertEquals(null, value.call(null));
	}

}
