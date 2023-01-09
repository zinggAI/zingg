package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestLastChars {
	
	@Test
	public void testLastChars() {
	    LastChars value = getInstance(5);
		assertEquals("happy", value.call("unhappy"));
	}

    private LastChars getInstance(int endIndex) {
        return new LastChars(endIndex);
    }


}
