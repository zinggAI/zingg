package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTruncateDouble {
	
	@Test
	public void testTruncateDoubleTo1Place() {
	    TruncateDouble value =  getInstance(1);
		assertEquals(543534.6d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo2Place() {
	    TruncateDouble value =  getInstance(2);
		assertEquals(543534.67d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3Place() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(543534.677d, value.call(543534.67734));
	}

	@Test
	public void testTruncateDoubleTo3PlaceWhenNumberhas2DecimalPlaces() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(543534.670d, value.call(543534.67));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberWithNoDecimalPlaces() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(543534.000d, value.call(543534d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumberNAN() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(Double.NaN, value.call(Double.NaN));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNumber0() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(0d, value.call(0d));
	}

	@Test
	public void testTruncateDoubleTo3PlaceForNull() {
	    TruncateDouble value =  getInstance(3);
		assertEquals(null, value.call(null));
	}
	
    private TruncateDouble getInstance(int num) {
        return new TruncateDouble(num);
    }

}
