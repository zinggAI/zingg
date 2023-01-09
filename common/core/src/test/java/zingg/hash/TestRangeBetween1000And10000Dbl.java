package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestRangeBetween1000And10000Dbl {

    private RangeDbl getInstance() {
        return new RangeDbl(1000,10000);
    }
    
	@Test
	public void testRangeForValueZero() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(0d));
	}

	@Test
	public void testRangeForNegativeValue() {
		Double input = -100d;
		RangeDbl value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForVeryHighValue() {
		Double input = 999999d;
		RangeDbl value = getInstance();
		assertEquals(0, value.call(input));
	}

	@Test
	public void testRangeForValue8() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(8d));
	}

	@Test
	public void testRangeForValue65() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(65d));
	}

	@Test
	public void testRangeForValue867() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(867d));
	}
	@Test
	public void testRangeLessThan10000() {
	    RangeDbl value = getInstance();
		assertEquals(1, value.call(8637d));
	}
	@Test
	public void testRangeForNull() {
	    RangeDbl value = getInstance();
		assertEquals(0, value.call(null));
	}

}
