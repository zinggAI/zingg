package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.TrimLastDigitsInt;

public class TestTrimLastDigitsInt {
	
	@Test
	public void testTrimLast1Digit() {
	    TrimLastDigitsInt value = getInstance(1);
		assertEquals(54353, value.call(543534));
	}

	@Test
	public void testTrimLast2DigitsInt() {
	    TrimLastDigitsInt value = getInstance(2);
		assertEquals(5435, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsInt() {
	    TrimLastDigitsInt value = getInstance(3);
		assertEquals(543, value.call(543534));
	}

	@Test
	public void testTrimLast3DigitsIntNullValue() {
	    TrimLastDigitsInt value = getInstance(3);
		assertEquals(null, value.call(null));
	}

    private TrimLastDigitsInt getInstance(int num) {
        return new TrimLastDigitsInt(num);
    }
	
}
