package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.RangeLong;

public class TestRangeBetween100And1000Long {

    private RangeLong getInstance() {
        return new RangeLong(100L,1000L);
    }

	@Test
	public void testRangeForValueZero() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(0L));
	}

	@Test
	public void testRangeForNegativeValue() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(-100L));
	}

	@Test
	public void testRangeForVeryHighValue() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(999999L));
	}

	@Test
	public void testRangeForValue8() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(8L));
	}

	@Test
	public void testRangeForValue65() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(65L));
	}

	@Test
	public void testRangeForValue867() {
	    RangeLong value = getInstance();
		assertEquals(1, value.call(867L));
	}
	@Test
	public void testRangeForValue8637() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(8637L));
	}
	@Test
	public void testRangeForNull() {
	    RangeLong value = getInstance();
		assertEquals(0, value.call(null));
	}
	@Test
	public void testRangeForUpperLimit() {
		RangeLong value = getInstance();
		assertEquals(1000, value.getUpperLimit()); 
	}
	@Test
	public void testRangeForLowerLimit() {
		RangeLong value = getInstance();
		assertEquals(100, value.getLowerLimit()); 
	}

}
