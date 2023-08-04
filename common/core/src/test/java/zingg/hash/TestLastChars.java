package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.LastChars;

public class TestLastChars {
	
	@Test
	public void testLastChars() {
	    LastChars value = getInstance(5);
		assertEquals("happy", value.call("unhappy"));
	}

	@Test
	public void testLastChars2() {
		LastChars value = getInstance(3);
		assertEquals(null,value.call(null));
	}

    private LastChars getInstance(int endIndex) {
        return new LastChars(endIndex);
    }


}
