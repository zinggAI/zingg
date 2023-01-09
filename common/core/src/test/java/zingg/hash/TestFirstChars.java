package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestFirstChars {
	
	@Test
	public void testFirstChars1() {
	    FirstChars value = getInstance(2);
		assertEquals("un", value.call("unhappy"));
	}

    @Test
    public void testFirstChars2() {
        FirstChars value = getInstance(12);
        assertEquals("unhappy", value.call("unhappy"));
    }
	
    private FirstChars getInstance(int endIndex) {
        return new FirstChars(endIndex);
    }


}
