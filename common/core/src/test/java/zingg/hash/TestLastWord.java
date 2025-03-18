package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.LastWord;

public class TestLastWord {
	
	@Test
	public void testLastWord1() {
	    LastWord value = getInstance();
		assertEquals("gupta", value.call(" vikas gupta "));
	}

    @Test
    public void testLastWord2() {
        LastWord value = getInstance();
        assertEquals("gupta", value.call("gupta"));
    }
	
    @Test
    public void testLastWord3() {
        LastWord value = getInstance();
        assertEquals(null, value.call(null));
    }

    @Test
    public void testLastWord4() {
        LastWord value = getInstance();
        assertEquals(null, value.call("  "));
    }

    private LastWord getInstance() {
        return new LastWord();
    }


}
