package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.RangeFloat;

public class TestRangeBetween0And10Float {

    private RangeFloat getInstance() {
        return new RangeFloat(0,10);
    }

	@Test
	public void testRangeForValueZero() {
	    RangeFloat value = getInstance();
		assertEquals(1, value.call(0f));
	}

	@Test
	public void testRangeForNegativeValue() {
		Float input = -100f;
		RangeFloat value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForVeryHighValue() {
		Float input = 99999f;
		RangeFloat value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
	    RangeFloat value = getInstance();
		assertEquals(1, value.call(8f));
	}

	@Test
	public void testRangeForValue65() {
	    RangeFloat value = getInstance();
		assertEquals(0, value.call(65f));
	}

	@Test
	public void testRangeForValue867() {
	    RangeFloat value = getInstance();
		assertEquals(0, value.call(867f));
	}
	@Test
	public void testRangeForValue8637() {
	    RangeFloat value = getInstance();
		assertEquals(0, value.call(8637f));
	}
	@Test
	public void testRangeForNull() {
        RangeFloat value = getInstance();
		assertEquals(0, value.call(null));
	}
	@Test
	public void testRangeForUpperLimit() {
		RangeFloat value = getInstance();
		assertEquals(10, value.getUpperLimit()); 
	}
	@Test
	public void testRangeForLowerLimit() {
		RangeFloat value = getInstance();
		assertEquals(0, value.getLowerLimit()); 
	}
}
