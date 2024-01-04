package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.TrimLastDigitsLong;

public class TestTrimLastDigitsLong {
    
	@Test
	public void testTrimLast1Digit() {
	    TrimLastDigitsLong value = getInstance(1);
		assertEquals(54353L, value.call(543534L));
	}

	@Test
	public void testTrimLast2DigitsInt() {
	    TrimLastDigitsLong value = getInstance(2);
		assertEquals(5435L, value.call(543534L));
	}

	@Test
	public void testTrimLast3DigitsInt() {
	    TrimLastDigitsLong value = getInstance(3);
		assertEquals(543L, value.call(543534L));
	}

	@Test
	public void testTrimLast3DigitsIntNullValue() {
	    TrimLastDigitsLong value = getInstance(3);
		assertEquals(null, value.call(null));
	}

    private TrimLastDigitsLong getInstance(int num) {
        return new TrimLastDigitsLong(num);
    }
	
}