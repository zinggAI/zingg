package zingg.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import zingg.common.core.hash.IdentityString;

public class TestIdentityString {
	
	@Test
	public void testIdentityString() {
	    IdentityString value = getInstance();
		assertEquals("unhappy", value.call(" UnHappy "));
	}
    @Test
	public void testIdentityString2() {
		IdentityString value = getInstance();
		assertEquals(null, value.call(null));
	}

    private IdentityString getInstance() {
        return new IdentityString();
    }


}
