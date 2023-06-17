package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.RangeInt;

public class TestRangeBetween1000And10000Int {

    private RangeInt getInstance() {
        return new RangeInt(1000,10000);
    }

	@Test
	public void testRangeForValueZero() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(0));
	}

	@Test
	public void testRangeForNegativeValue() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(-100));
	}

	@Test
	public void testRangeForVeryHighValue() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(999999));
	}

	@Test
	public void testRangeForValue8() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(8));
	}

	@Test
	public void testRangeForValue65() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(65));
	}

	@Test
	public void testRangeForValue867() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(867));
	}

	@Test
	public void testRangeForValue8637() {
	    RangeInt value = getInstance();
		assertEquals(1, value.call(8637));
	}
	@Test
	public void testRangeForNull() {
	    RangeInt value = getInstance();
		assertEquals(0, value.call(null));
	}
	@Test
	public void testRangeForUpperLimit() {
		RangeInt value = getInstance();
		assertEquals(10000, value.getUpperLimit()); 
	}
	@Test
	public void testRangeForLowerLimit() {
		RangeInt value = getInstance();
		assertEquals(1000, value.getLowerLimit()); 
	}

}
